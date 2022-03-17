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

package com.honiism.discord.lemi.commands.slash.staff.admins;

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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class UserBan extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public UserBan() {
        setCommandData(Commands.slash("userban", "Bans a user from using Lemi bot.")
                .addSubcommands(
                        new SubcommandData("add", "Ban a user from using Lemi.")
                                .addOption(OptionType.USER, "user", "The user you want to ban.", true)
                                .addOption(OptionType.STRING, "reason", "The reason why they're getting banned.", true),

                        new SubcommandData("remove", "Unban a previously banned user.")
                                .addOption(OptionType.USER, "user", "The user you want to unban.", true),

                        new SubcommandData("view", "View all details from the ban list.")
                )
        );

        setUsage("/userban ((subcommands))");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();

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
                    Member targetMember = event.getOption("user", OptionMapping::getAsMember);
                    String reason = event.getOption("reason", OptionMapping::getAsString);
                    
                    if (targetMember == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.addBannedUserId(targetMember, reason, event);
                    break;

                case "remove":
                    targetMember = event.getOption("user", OptionMapping::getAsMember);

                    if (targetMember == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.removeBannedUserId(targetMember, event);
                    break;

                case "view":
                    viewAllBans(event);
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

    private void viewAllBans(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
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
            hook.editOriginal(":fish_cake: There's no banned users.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **BANNED LIST!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(banDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getUser().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
            .queue(message -> builder.build().paginate(message, page));
    }
}