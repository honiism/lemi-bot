package com.honiism.discord.lemi.database.managers;

import java.util.Map;

import com.honiism.discord.lemi.database.LemiDbEmbedDs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface LemiDbEmbedManager {
    LemiDbEmbedManager INS = new LemiDbEmbedDs();

    // embed
    String getSavedMsgContent(String embedId);
    EmbedBuilder getSavedEmbedBuilder(InteractionHook hook, String embedId);
    void showSavedEmbed(GuildMemberJoinEvent event, TextChannel channel, String embedId);
    void showSavedEmbed(InteractionHook hook, String embedId);
    void showEmbedsList(InteractionHook hook);
    void deleteCustomEmbed(InteractionHook hook, String embedId);
    void assignUniqueId(InteractionHook hook, String specialKey, Map<String, String> embedProperties);
    void saveCreatedEmbed(InteractionHook hook, String messageContent, String embedId,
            Map<String, String> embedProperties, Map<String, Integer> embedColor);
    void saveCreatedEmbed(InteractionHook hook, String embedId, Map<String, String> embedProperties, Map<String, Integer> embedColor);
}