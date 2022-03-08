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

package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildTimeoutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(GuildListener.class);

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Guild guild = event.getGuild();
        LemiDbManager.INS.insertGuildSettings(guild);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Config.getLong("honeys_sweets_id"))
                || guildId.equals(Config.getLong("test_server"))) {
            return;
        }

        String joinedLogMsg = "--------------------------\r\n"
                +  "**LEMI JOINED A SERVER!**\r\n"
                + "**Guild id :** " + guildId + "\r\n"
                + "**Guild name :** " + guild.getName() + "\r\n";

        log.info(joinedLogMsg.toString());

        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(joinedLogMsg)
	    .queue();

        guild.leave().queue();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Config.getLong("honeys_sweets_id"))) {
            return;
        }

        String leaveLogMsg = "--------------------------\r\n"
                +  "**LEMI LEFT A SERVER!**\r\n"
                + "**Guild id :** " + guildId + "\r\n"
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
        if (event.getGuildIdLong() == Config.getLong("honeys_sweets_id")) {
            BaseListener.getJDA().retrieveUserById(Config.getLong("dev_id")).queue(
                (dev) -> {
                    dev.openPrivateChannel().queue(
                        (channel) -> {
                            channel.sendMessage("HONEY'S SWEETS GUILD HAS FAILED TO LOAD!").queue();
                            log.error("HONEY'S SWEETS GUILD HAS FAILED TO LOAD!");
                        },
                        (error) -> {
                            log.error("HONEY'S SWEETS GUILD HAS FAILED TO LOAD!");
                        }
                    );
                }
            );
        } else {
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
}