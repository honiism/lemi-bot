/*
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

package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyMods extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyMods() {
        setCommandData(Commands.slash("modifymods", "Add/remove/view user(s) to/from the moderator database.")
                .addSubcommands(
                        new SubcommandData("add", "Add a user to the official moderator list.")
                                .addOption(OptionType.USER, "user", "The user you want to add.", true)
                                .addOption(OptionType.STRING, "key", "The key that will be assigned for this user.", true),

                        new SubcommandData("remove", "Remove a user from the official moderator list.")
                                .addOption(OptionType.USER, "user", "The user you want to remove.", true),

                        new SubcommandData("view", "View all details from the official moderator list.")
                )
        );

        setUsage("/dev modifymods ((subcommands))");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        setGlobal(true);
    }
    
    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
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

            String subCmdName = event.getSubcommandName();

            switch (subCmdName) {
                case "add":
                    Member memberToAdd = event.getOption("user").getAsMember();
                    String keyToAdd = event.getOption("key").getAsString();
                    
                    if (memberToAdd == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.addModId(guild, memberToAdd, keyToAdd, event);
                    break;

                case "remove":
                    Member memberToRemove = event.getOption("user").getAsMember();

                    if (memberToRemove == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.removeModId(guild, memberToRemove, event);
                    break;

                case "view":
                    viewAllIds(event);
                    break;
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void viewAllIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> modDetails = new ArrayList<>();
        List<String> modIds = LemiDbManager.INS.getModIds();
        List<String> modKeys = LemiDbManager.INS.getModKeys();

        for (int i = 0; i < modIds.size(); i++) {
            modDetails.add("<@" + modIds.get(i) + "> `" 
                    + modIds.get(i) + " | key :` ||" 
                    + modKeys.get(i) + "||");
        }

        if (Tools.isEmpty(modDetails)) {
            hook.editOriginal(":fish_cake: There's no mods.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **MODS!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(modDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getUser().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        event.getUser().openPrivateChannel().queue((msg) -> {
            msg.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                .queue(message -> builder.build().paginate(message, page));
        });

        hook.editOriginal(":blueberries: Sent you the details.").queue();
    }
}