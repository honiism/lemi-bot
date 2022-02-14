package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BaseListener extends ListenerAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(BaseListener.class);
    public static JDA jda;

    @Override
    public void onReady(ReadyEvent event) {
        jda = event.getJDA();

        getJDA().addEventListener(new SlashCmdListener());
        
        log.info("{} is now online and all set up! (Shard : {} / {})",
                event.getJDA().getSelfUser().getAsTag(),
                event.getJDA().getShardInfo().getShardId() + 1,
                Lemi.getInstance().getShardManager().getShardsTotal());
        
        try {
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                .getTextChannelById(Config.get("logs_channel_id"))
                .sendMessageFormat("%s is now **online on Honey's Picnic server shard** ! " 
                        + "<@" + Config.get("dev_id") + ">", 
                        event.getJDA().getSelfUser().getAsMention())
                .queue();
            
        } catch (NullPointerException ignored) { }
    }

    public static JDA getJDA() {
        return jda;
    }
}