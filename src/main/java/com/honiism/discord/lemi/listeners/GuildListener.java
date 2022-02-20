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

package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildTimeoutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildListener extends ListenerAdapter{

    private static final Logger log = LoggerFactory.getLogger(GuildListener.class);

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    log.info("Successfully loaded members for Honey's Picnic server.");
                    
                    guild.getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage("Successfully loaded members for Honey's Picnic server.")
                        .queue();
                })
                .onError((error) -> {
                    log.error("Failed to load members for Honey's Picnic server.", error);

                    guild.getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage("Failed to load members for Honey's Picnic server.\r\n"
                                + "--------------------------\r\n"
                                + "Message : " + error.getMessage() + "\r\n"
                                + "--------------------------\r\n"
                                + "Cause : " + error.getCause().getMessage() + "\r\n"
                                + "--------------------------\r\n"
                                + "Stack trace : " + error.getStackTrace().toString())
                        .queue();
                });
        }

        LemiDbManager.INS.insertGuildSettings(guild);
        CurrencyTools.guildAddCurrProfs(guild);

        if (BaseListener.getJDA() == null) {
            System.out.println("null jda instance");
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))) {
            return;
        }

        String joinedLogMsg = "--------------------------\r\n"
                +  "**LEMI JOINED A SERVER!**\r\n"
                + "**Guild id :** " + guild.getIdLong() + "\r\n"
                + "**Guild name :** " + guild.getName() + "\r\n";

        log.info(joinedLogMsg.toString());

        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(joinedLogMsg)
	    .queue();

        LemiDbManager.INS.insertGuildSettings(guild);
        CurrencyTools.guildAddCurrProfs(guild);

        BaseListener.getJDA().retrieveCommands().queue(
            (cmds) -> {
                Lemi.getInstance().updateCmdPrivileges(guild, cmds);
            }
        );
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))) {
            return;
        }

        String leaveLogMsg = "--------------------------\r\n"
                +  "**LEMI LEFT A SERVER!**\r\n"
                + "**Guild id :** " + guild.getIdLong() + "\r\n"
                + "**Guild name :** " + guild.getName() + "\r\n";

        log.info(leaveLogMsg.toString());

        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(leaveLogMsg)
	    .queue();
    }

    @Override
    public void onGuildTimeout(GuildTimeoutEvent event) {
        String logMessage = "A guild has failed to load and timeout.\r\n"
                + "Guild id : " + event.getGuildId() + "\r\n"
                + "Response number : " + event.getResponseNumber();

        log.error(logMessage);
        
        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(logMessage)
	    .queue();
    }
}