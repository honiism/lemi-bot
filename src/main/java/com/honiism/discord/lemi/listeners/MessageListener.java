package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            this.onGuildMessageReceived(event);
        }
        // else if (event.isFromType(ChannelType.PRIVATE)) {}
    }

    private void onGuildMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (!guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))
                && !guildId.equals(Long.parseLong(Config.get("test_server")))) {
            guild.leave().queue();
            return;
        }
        
        if (member.getUser().isBot() || event.isWebhookMessage()) {
            return;
        }

        String raw = event.getMessage().getContentRaw();

        if (raw.equalsIgnoreCase("lemi emergency shutdown") && Tools.isAuthorDev(member)) {
            log.info(member.getUser().getAsTag() + "(" + member.getIdLong() + ") initiated emergency shutdown!");
			
            event.getChannel().sendMessage("Shutting down. . .").queue();
        	
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage(member.getAsMention() + " **received emergency shutdown request. :bell:**")
        	.queue();
        	
            Lemi.getInstance().getShardManager().shutdown();
            BotCommons.shutdown(Lemi.getInstance().getShardManager());
        }
    }
}