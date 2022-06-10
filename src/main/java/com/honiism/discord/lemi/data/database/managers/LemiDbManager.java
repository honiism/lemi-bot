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

import java.util.List;

import com.honiism.discord.lemi.data.database.LemiDbDs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface LemiDbManager {
    LemiDbManager INS = new LemiDbDs();

    // guildsettings
    void setNSFWRating(boolean input, InteractionHook hook);
    boolean isNSFWAllowed(InteractionHook hook);
    void setParanoiaRate(int shownRate, InteractionHook hook);
    int getParanoiaRate(InteractionHook hook);

    boolean hasQuestionData(long guildId);
    void addQuestionData(long guildId);
    void updateQuestionData(long guildId, String jsonData);
    String getQuestionData(long guildId);

    // userban
    List<String> getBannedReasons(MessageReceivedEvent event);
    List<Long> getBannerAuthorIds(MessageReceivedEvent event);
    List<Long> getBannedUserIds(MessageReceivedEvent event);
    void addBannedUserId(long targetId, String reason, MessageReceivedEvent event);
    void removeBannedUserId(long targetId, MessageReceivedEvent event);

    // modifyadmins
    List<Long> getAdminIds();
    List<String> getAdminKeys();
    void removeAdminId(Guild guild, Member member, MessageReceivedEvent event);
    void addAdminId(Guild guild, Member member, String key, MessageReceivedEvent event);

    // modifymods
    List<Long> getModIds();
    List<String> getModKeys();
    void removeModId(Guild guild, Member member, MessageReceivedEvent event);
    void addModId(Guild guild, Member member, String key, MessageReceivedEvent event);

    // slashcmd
    void checkIfBanned(SlashCommandInteractionEvent event);

    // tools
    boolean isAuthorAdmin(User author);
    boolean isAuthorMod(User author);
    boolean isAuthorAdmin(Member member, SlashCommandInteractionEvent event);
    boolean isAuthorMod(Member member, SlashCommandInteractionEvent event);
    void insertGuildSettings(Guild guild);
}