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

package com.honiism.discord.lemi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmdManager;
import com.honiism.discord.lemi.data.items.Items;
import com.honiism.discord.lemi.listeners.BaseListener;
import com.honiism.discord.lemi.listeners.CustomEmbedListener;
import com.honiism.discord.lemi.listeners.GuildListener;
import com.honiism.discord.lemi.listeners.MemberGuildListener;
import com.honiism.discord.lemi.listeners.MessageListener;
import com.honiism.discord.lemi.utils.customEmbeds.EmbedTools;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Lemi {

    private static final Logger log = LoggerFactory.getLogger(Lemi.class);
    
    private static Lemi instance;

    private final ShardManager shardManager;
    private final ExecutorService cmdExecService;
    private final EventWaiter waiter;
    private final SlashCmdManager slashCmdManager;
    private final EmbedTools embedTools;
    private final ObjectMapper objectMapper;

    private JDA jda;
    private boolean shuttingDown = false;
    private boolean debug = false;
    
    public Lemi() throws LoginException {
        instance = this;
        
        waiter = new EventWaiter();
        slashCmdManager = new SlashCmdManager();
        embedTools = new EmbedTools();
        objectMapper = new ObjectMapper();

        cmdExecService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder()
                        .setNameFormat("Lemi's Cmd Thread %d")
                        .setUncaughtExceptionHandler((t, e) -> log.error("An uncaught error happened on the Lemi's Cmd Thread!\r\n" 
                                + "Thread: " + t.getName()))
                        .build()
        );

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder
            .create(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
            )
            .setToken(Config.get("token"))
            .setActivityProvider((shardId) -> Activity.playing("/help | Follow @honiism on Instagram!"))
            .setMemberCachePolicy(MemberCachePolicy.DEFAULT.or(MemberCachePolicy.PENDING).or(MemberCachePolicy.OWNER))
            .setChunkingFilter(ChunkingFilter.NONE)
            .enableCache(
                CacheFlag.EMOTE,
                CacheFlag.MEMBER_OVERRIDES,
                CacheFlag.VOICE_STATE,
                CacheFlag.ROLE_TAGS
            )
            .disableCache(
                CacheFlag.ONLINE_STATUS,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ACTIVITY
            )
            .setAutoReconnect(true)
            .addEventListeners(
                new BaseListener(),
                new GuildListener(),
                new MessageListener(),
                new MemberGuildListener(),
                waiter
            );

        shardManager = builder.build();

        embedTools.registerEmbedListener(new CustomEmbedListener());
        Items.addItemsToList();
    }

    public static void main(String[] args) {
        try {
           new Lemi();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLemiLogger() {
        return log;
    }

    public static Lemi getInstance() {
        return instance;
    }

    public void shutdown() {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;

        getShardManager().getGuilds().stream().forEach(guild -> {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                guild.getAudioManager().closeAudioConnection();
            }
        });

        cmdExecService.shutdownNow();
        getShardManager().shutdown();
        BotCommons.shutdown(getShardManager());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public EmbedTools getEmbedTools() {
        return embedTools;
    }

    public JDA getJDA() {
        return jda;
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    public SlashCmdManager getSlashCmdManager() {
        return slashCmdManager;
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public EventWaiter getEventWaiter() {
        return waiter;
    }

    public ExecutorService getCmdExecutor() {
        return cmdExecService;
    }
}