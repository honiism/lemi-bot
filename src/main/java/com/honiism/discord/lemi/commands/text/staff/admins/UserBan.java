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

package com.honiism.discord.lemi.commands.text.staff.admins;

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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserBan extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public UserBan() {
        setName("userban");
        setDesc("Bans a user from using Lemi bot.");
        setUsage("userban ((add <user_id> <reason>|remove <user_id>|view))");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
        User author = event.getAuthor();

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
                event.getMessage().reply(":butterfly: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            String subCmdName = args.get(0);
            String targetId;

            switch (subCmdName) {
                case "add":
                    if (args.size() < 2) {
                        event.getMessage().reply(":dango: Usage: `" + getUsage() + "`!").queue();
                        return;
                    }

                    targetId = args.get(1);

                    Lemi.getInstance().getJDA().retrieveUserById(targetId)
                        .queue(
                            (target) -> {
                                String reason = String.join(" ", args.subList(2, args.size()));
                                LemiDbManager.INS.addBannedUserId(target.getIdLong(), reason, event);
                            },
                            (empty) -> {
                                event.getMessage().reply(":grapes: That user doesn't exist.").queue();
                            }
                        );
                    break;

                case "remove":
                    if (args.size() < 2) {
                        event.getMessage().reply(":octopus: Usage: `" + getUsage() + "`!").queue();
                        return;
                    }

                    targetId = args.get(1);

                    Lemi.getInstance().getJDA().retrieveUserById(targetId)
                        .queue(
                            (target) -> {
                                LemiDbManager.INS.removeBannedUserId(target.getIdLong(), event);
                            },
                            (empty) -> {
                                event.getMessage().reply(":grapes: That user doesn't exist.").queue();
                            }
                        );
                    break;

                case "view":
                    viewAllBans(event);
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

    private void viewAllBans(MessageReceivedEvent event) {
        List<String> banDetails = new ArrayList<>();
        List<Long> authorIds = LemiDbManager.INS.getBannerAuthorIds(event);
        List<Long> bannedUserIds = LemiDbManager.INS.getBannedUserIds(event);
        List<String> reasons = LemiDbManager.INS.getBannedReasons(event);

        for (int i = 0; i < bannedUserIds.size(); i++) {
            banDetails.add("Admin : <@" + authorIds.get(i) + ">" 
                    + " | Banned user : <@" + bannedUserIds.get(i) + ">"
                    + " | Reason : `" + reasons.get(i) + "`");
        }

        if (Tools.isEmpty(banDetails)) {
            event.getMessage().reply(":fish_cake: There's no banned users.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **BANNED LIST!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(banDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getAuthor().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        event.getMessage().replyEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
            .queue(message -> builder.build().paginate(message, page));
    }
}