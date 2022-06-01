/**
 * Copyright (C) 2022 Honiism
 * 
 * This file is part of Lemi-Bot.
 * 
 * Lemi-Bot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Lemi-Bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lemi-Bot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.honiism.discord.lemi.commands.text.staff.dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.buttons.Paginator;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ModifyMods extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyMods() {
        setName("modifymods");
        setDesc("Add/remove/view user(s) to/from the moderator database.");
        setUsage("modifymods add <user_id> <key>\r\n"
                + "modifymods remove <user_id>\r\n"
                + "modifymods view");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }
    
    @Override
    public void action(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        
        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            List<String> args = ctx.getArgs();

            if (args.isEmpty()) {
                event.getMessage().reply(":blueberries: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            String actionName = args.get(0);
            long targetId;

            switch (actionName) {
                case "add":
                    if (args.size() < 2) {
                        event.getMessage().reply(":blueberries: Usage: `l.modifymods add <user_id> <key>`!").queue();
                        return;
                    }

                    if (!Tools.isLong(args.get(1))) {
                        event.getMessage().reply(":crescent_moon: `<user_id>` must be a valid user id number.").queue();
                        return;
                    }

                    if (!args.get(2).contains("mod")) {
                        event.getMessage().reply(":strawberry: `<key>` must contain the word \"mod\".").queue();
                        return;
                    }

                    targetId = Long.parseLong(args.get(1));
                    String modKey = args.get(2);
                    
                    guild.retrieveMemberById(targetId).queue(
                        (targetMember) -> {
                            LemiDbManager.INS.addModId(guild, targetMember, modKey, event);
                        },
                        (empty) -> {
                            event.getMessage().reply(":grapes: That user doesn't exist in the guild.").queue();
                        }
                    );
                    break;

                case "remove":
                    if (args.isEmpty()) {
                        event.getMessage().reply(":blueberries: Usage: `l.modifymods remove <user_id>`!").queue();
                        return;
                    }

                    if (!Tools.isLong(args.get(1))) {
                        event.getMessage().reply(":umbrella2: `<user_id>` must be a valid user id number.").queue();
                        return;
                    }

                    targetId = Long.parseLong(args.get(1));
                    
                    guild.retrieveMemberById(targetId).queue(
                        (targetMember) -> {
                            LemiDbManager.INS.removeModId(guild, targetMember, event);
                        },
                        (empty) -> {
                            event.getMessage().reply(":grapes: That user doesn't exist in the guild.").queue();
                        }
                    );
                    break;

                case "view":
                    viewAllIds(event);
                    break;
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            event.getMessage().replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }
    }

    private void viewAllIds(MessageReceivedEvent event) {
        List<String> modDetails = new ArrayList<>();
        List<Long> modIds = LemiDbManager.INS.getModIds();
        List<String> modKeys = LemiDbManager.INS.getModKeys();

        for (int i = 0; i < modIds.size(); i++) {
            modDetails.add("<@" + modIds.get(i) + "> `" 
                    + modIds.get(i) + " | key :` ||" 
                    + modKeys.get(i) + "||");
        }

        if (Tools.isEmpty(modDetails)) {
            event.getMessage().reply(":fish_cake: There's no mods.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **MODS!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(modDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getAuthor().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        event.getAuthor().openPrivateChannel().queue((msg) -> {
            msg.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                .queue(message -> builder.build().paginate(message, page));
        });

        event.getMessage().reply(":blueberries: Sent you the details.").queue();
    }
}