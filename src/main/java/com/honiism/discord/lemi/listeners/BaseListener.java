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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BaseListener extends ListenerAdapter {
    
    public static JDA jda;
    private static final Logger log = LoggerFactory.getLogger(BaseListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        jda = event.getJDA();

        getJDA().addEventListener(new SlashCmdListener());

        Lemi.getInstance().getSlashCmdManager().initialize();
        
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