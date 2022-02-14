package com.honiism.discord.lemi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.security.auth.login.LoginException;

import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
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
import okhttp3.OkHttpClient;

public class Lemi {

    private static final Logger log = LoggerFactory.getLogger(Lemi.class); 
    private static Lemi instance;

    private final ShardManager shardManager;
    private final ScheduledExecutorService threadpool;
    private final EventWaiter waiter;
    private final OkHttpClient httpClient;

    private boolean shuttingDown = false;
    
    public Lemi() throws LoginException {
        instance = this;
        waiter = new EventWaiter();
        httpClient = new OkHttpClient();
        threadpool = Executors.newSingleThreadScheduledExecutor();

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
        EmbedTools embedTools = new EmbedTools();
        CustomEmbedListener embedListener = new CustomEmbedListener();

        embedTools.registerEmbedListener(embedListener);
        Items.addItemsToList();
    }

    public static void main(String[] args) {
        try {
           new Lemi();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void shutdown(JDA jda) {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;

        threadpool.shutdownNow();

        jda.getGuilds().stream().forEach(guild -> {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                guild.getAudioManager().closeAudioConnection();
            }
        });

        jda.getShardManager().shutdown();
        BotCommons.shutdown(jda.getShardManager());
    }

    public static Logger getLemiLogger() {
        return log;
    }

    public static Lemi getInstance() {
        return instance;
    }
    

    public ShardManager getShardManager() {
        return shardManager;
    }

    public EventWaiter getEventWaiter() {
        return waiter;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }
}