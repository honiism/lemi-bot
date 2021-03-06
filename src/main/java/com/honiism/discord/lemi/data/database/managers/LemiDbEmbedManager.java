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

package com.honiism.discord.lemi.data.database.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honiism.discord.lemi.data.database.LemiDbEmbedDs;
import com.honiism.discord.lemi.data.embed.EmbedData;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface LemiDbEmbedManager {
    LemiDbEmbedManager INS = new LemiDbEmbedDs();

    // embed
    String getSavedMsgContent(String embedId);
    EmbedBuilder getSavedEmbedBuilder(String embedId);
    void showSavedEmbed(GuildMemberJoinEvent event, TextChannel channel, String embedId);
    void showSavedEmbed(Message message, String embedId, MessageReceivedEvent event);
    void showEmbedsList(Message message);
    void deleteCustomEmbed(Message message, String embedId);
    void assignUniqueId(TextChannel channel, String specialKey, EmbedData embedData);
    void saveCreatedEmbed(TextChannel channel, EmbedData embedData) throws JsonProcessingException;
}