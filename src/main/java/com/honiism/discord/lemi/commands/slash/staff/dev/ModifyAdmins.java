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
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyAdmins extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyAdmins() {
        setCommandData(Commands.slash("modifyadmins", "Add/remove/view user(s) to/from the administrator database.")
                .addSubcommands(
                        new SubcommandData("add", "Add a user's key in the official administrator list.")
                                .addOption(OptionType.USER, "user", "The user you want to add.", true)
                                .addOption(OptionType.STRING, "key", "The key that will be assigned for this user.", true),
  
                        new SubcommandData("remove", "Remove a user from the official administrator list.")
                                .addOption(OptionType.USER, "user", "The user you want to remove.", true),

                        new SubcommandData("view", "View all details from the official administrator list.")
                )
        );

        setUsage("/dev modifyadmins ((subcommands))");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
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
                    Member memberToAdd = event.getOption("user", OptionMapping::getAsMember);
                    String keyToAdd = event.getOption("key", OptionMapping::getAsString);

                    if (memberToAdd == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.addAdminId(guild, memberToAdd, keyToAdd, event);
                    break;

                case "remove":
                    Member memberToRemove = event.getOption("user", OptionMapping::getAsMember);

                    if (memberToRemove == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.removeAdminId(guild, memberToRemove, event);
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
                        + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void viewAllIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> adminDetails = new ArrayList<>();
        List<Long> adminIds = LemiDbManager.INS.getAdminIds();
        List<String> adminKeys = LemiDbManager.INS.getAdminKeys();

        for (int i = 0; i < adminIds.size(); i++) {
            adminDetails.add("<@" + adminIds.get(i) + "> `" 
                    + adminIds.get(i) + " | key :` ||" 
                    + adminKeys.get(i) + "||");
        }

        if (Tools.isEmpty(adminDetails)) {
            hook.editOriginal(":tulip: There's no admins.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setEmbedDesc("‧₊੭ :cake: **ADMINS!** ♡ ⋆｡˚")
            .setItemsPerPage(10)
            .setItems(adminDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getUser().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        event.getUser().openPrivateChannel().queue((privChannel) -> {
            privChannel.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":seedling: Loading..."))
                .queue(message -> builder.build().paginate(message, page));
        });

        hook.editOriginal(":snowflake: Sent you the details.").queue();
    }
}