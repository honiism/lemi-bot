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

import java.util.List;

import com.honiism.discord.lemi.data.database.LemiDbDs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface LemiDbManager {
    LemiDbManager INS = new LemiDbDs();

    // userban
    List<String> getBannedReasons(SlashCommandInteractionEvent event);
    List<Long> getBannerAuthorIds(SlashCommandInteractionEvent event);
    List<Long> getBannedUserIds(SlashCommandInteractionEvent event);
    void addBannedUserId(Member member, String reason, SlashCommandInteractionEvent event);
    void removeBannedUserId(Member member, SlashCommandInteractionEvent event);

    // modifyadmins
    List<Long> getAdminIds();
    List<String> getAdminKeys();
    void removeAdminId(Guild guild, Member member, SlashCommandInteractionEvent event);
    void addAdminId(Guild guild, Member member, String key, SlashCommandInteractionEvent event);

    // modifymods
    List<Long> getModIds();
    List<String> getModKeys();
    void removeModId(Guild guild, Member member, SlashCommandInteractionEvent event);
    void addModId(Guild guild, Member member, String key, SlashCommandInteractionEvent event);

    // slashcmd
    void checkIfBanned(SlashCommandInteractionEvent event);

    // tools
    boolean isAuthorAdmin(User author, TextChannel channel);
    boolean isAuthorMod(User author, TextChannel channel);
    boolean isAuthorAdmin(Member member, SlashCommandInteractionEvent event);
    boolean isAuthorMod(Member member, SlashCommandInteractionEvent event);
    void insertGuildSettings(Guild guild);
}