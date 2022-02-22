/*
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

package com.honiism.discord.lemi.data.database.managers;

import java.util.Map;

import com.honiism.discord.lemi.data.database.LemiDbEmbedDs;

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