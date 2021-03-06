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
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            this.onGuildMessageReceived(event);
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            this.onPrivateMessageReceived(event);
        }
    }

    private void onPrivateMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();

        if (author.isBot() || event.isWebhookMessage() || Tools.isAuthorDev(author)) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        log.info("{DM} " + author.getAsTag() + "(" + author.getIdLong() + "): \"" + message + "\"");
        	
        Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(author.getAsTag() + "(" + author.getIdLong() + "): \"" + message + "\"")
            .queue();
    }

    private void onGuildMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (!guildId.equals(Config.getLong("honeys_hive"))
                && !guildId.equals(Config.getLong("test_server"))) {
            return;
        }

        if (member == null || member.getUser().isBot() || event.isWebhookMessage()) {
            return;
        }

        String raw = event.getMessage().getContentRaw();
        String prefix = Config.get("prefix");

        if (raw.startsWith(prefix)) {
            Lemi.getInstance().getTextCmdManager().handle(event, prefix);
        } else if (raw.toLowerCase().startsWith(Config.get("prefix"))) {
            Lemi.getInstance().getTextCmdManager().handle(event, Config.get("prefix"));
        } else if (raw.startsWith(guild.getSelfMember().getAsMention())) {
            Lemi.getInstance().getTextCmdManager().handle(event, guild.getSelfMember().getAsMention());
        }
    }
}