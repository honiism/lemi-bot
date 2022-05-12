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

import java.util.Arrays;
import java.util.List;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
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

        // TODO: temporary, delete later when converted to cmds
        String[] split = raw.replaceFirst(Config.get("prefix"), "").split("\\s+");
        List<String> args = Arrays.asList(split).subList(1, split.length);
        MessageChannel channel = event.getChannel();

        if (raw.equalsIgnoreCase(Config.get("prefix") + "shutdown") && Tools.isAuthorDev(member)) {
            log.info(member.getUser().getAsTag() + "(" + member.getIdLong() + ") initiated emergency shutdown!");
			
            channel.sendMessage("Shutting down. . .").queue();
        	
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage(member.getAsMention() + " **received emergency shutdown request. :bell:**")
        	.queue();
        	
            Lemi.getInstance().shutdown();

        } else if (raw.equalsIgnoreCase(Config.get("prefix") + "reload") && Tools.isAuthorDev(member)) {
            Lemi.getInstance().getSlashCmdManager().reloadGlobalCmds();
            channel.sendMessage("Commands are now reloading, they should appear within an hour or so.").queue();

        } else if (raw.equalsIgnoreCase(Config.get("prefix") + "greload") && Tools.isAuthorDev(member)) {
            if (args.isEmpty()) {
                Lemi.getInstance().getSlashCmdManager().reloadGuildCmds(guild);
                channel.sendMessage("Commands are now reloaded for this guild only!").queue();
            } else {
                String id = args.get(0);
                Guild target = Lemi.getInstance().getShardManager().getGuildById(id);

                Lemi.getInstance().getSlashCmdManager().reloadGuildCmds(target);

                channel.sendMessage("Commands are now reloaded!").queue();
            }

        } else if (raw.equalsIgnoreCase(Config.get("prefix") + "upsertcmd") && Tools.isAuthorDev(member)) {
            if (args.isEmpty()) {
                channel.sendMessage("Please include a command name.").queue();
                return;
            }

            String cmdName = args.get(0);
            SlashCmd cmd = Lemi.getInstance().getSlashCmdManager().getCmdByName(cmdName);

            if (cmd == null) {
                channel.sendMessage("Invalid command name").queue();
                return;
            }
            
            Lemi.getInstance().getSlashCmdManager().upsertGlobal(cmd.getCommandData());
            channel.sendMessage("Upserted a global command.").queue();

        } else if (raw.equalsIgnoreCase(Config.get("prefix") + "gupsertcmd") && Tools.isAuthorDev(member)) {
            if (args.isEmpty()) {
                channel.sendMessage("Please include a command name.").queue();
                return;
            }

            String cmdName = args.get(0);
            SlashCmd cmd = Lemi.getInstance().getSlashCmdManager().getCmdByName(cmdName);

            if (cmd == null) {
                channel.sendMessage("Invalid command name").queue();
                return;
            }

            if (args.get(1).equals(null)) {
                Lemi.getInstance().getSlashCmdManager().upsertGuild(guild, cmd.getCommandData());
                channel.sendMessage("Upserted a guild cmd to this guild.").queue();
            } else {
                String id = args.get(1);
                Guild target = Lemi.getInstance().getShardManager().getGuildById(id);

                Lemi.getInstance().getSlashCmdManager().upsertGuild(target, cmd.getCommandData());
                channel.sendMessage("Upserted a guild cmd to target guild.").queue();
            }

        } else if (raw.equalsIgnoreCase(Config.get("prefix") + "gclearcmds") && Tools.isAuthorDev(member)) {
            if (args.isEmpty()) {
                Lemi.getInstance().getSlashCmdManager().clearGuildCmds(guild);
                channel.sendMessage("Cleared this guild's commads").queue();
            } else {
                String id = args.get(0);
                Guild target = Lemi.getInstance().getShardManager().getGuildById(id);

                Lemi.getInstance().getSlashCmdManager().clearGuildCmds(target);
                channel.sendMessage("Cleared target guild's commads").queue();
            }
        }
    }
}