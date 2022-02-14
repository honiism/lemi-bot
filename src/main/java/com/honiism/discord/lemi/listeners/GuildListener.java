package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.database.managers.LemiDbManager;

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
        LemiDbManager.INS.onGuildReady(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))
                || guildId.equals(Long.parseLong(Config.get("test_server")))) {
            return;
        }

        StringBuilder bannedJoinLog = new StringBuilder();

        bannedJoinLog.append("--------------------------\r\n"
                + "**LEMI JOINED A SERVER THAT'S NOT IN THE WHITELIST! :warning:**\r\n" 
                + "**Guild id :** " + guild.getIdLong() + "\r\n"
                + "**Guild name :** " + guild.getName() + "\r\n");

        guild.retrieveOwner()
            .queue(
                (owner) -> {
                    bannedJoinLog.append("**Guild owner id :** " + owner.getIdLong() + "\r\n"
                            + "**Guild owner tag :** " + owner.getUser().getAsMention() + "\r\n"
                            + "LEAVING IN **5** SECONDS\r\n"
                            + "--------------------------");
                },
                (empty) -> {
                    bannedJoinLog.append("**Guild owner id :** null\r\n"
                            + "**Guild owner tag :** null\r\n"
                            + "LEAVING IN **5** SECONDS\r\n"
                            + "--------------------------");
                }
            );

        log.info(bannedJoinLog.toString());

        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(bannedJoinLog.toString())
	    .queue();

        guild.leave().queue();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))) {
            return;
        }

        StringBuilder bannedLeaveLog = new StringBuilder();

        bannedLeaveLog.append("--------------------------\r\n"
                + "**LEMI JUST LEFT A SERVER THAT'S NOT IN THE WHITELIST! :warning:**\r\n" 
                + "**Guild id :** " + guild.getIdLong() + "\r\n"
                + "**Guild name :** " + guild.getName() + "\r\n");

        guild.retrieveOwner()
            .queue(
                (owner) -> {
                    bannedLeaveLog.append("**Guild owner id :** " + owner.getIdLong() + "\r\n"
                            + "**Guild owner tag :** " + owner.getUser().getAsMention() + "\r\n"
                            + "--------------------------");
                },
                (empty) -> {
                    bannedLeaveLog.append("**Guild owner id : ** null\r\n"
                            + "**Guild owner tag :** null\r\n"
                            + "--------------------------");
                }
            );

        log.info(bannedLeaveLog.toString());

        Lemi.getInstance().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(bannedLeaveLog.toString())
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