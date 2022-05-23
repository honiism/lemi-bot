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

package com.honiism.discord.lemi.utils.customEmbeds;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public interface IEmbedListener {
    void afterAskingId(User author, TextChannel channel);
    void afterAskingTitle(User author, TextChannel channel);
    void afterAskingColor(User author, TextChannel channel);
    void afterAskingAuthor(User author, TextChannel channel);
    void afterAskingThumbnail(User author, TextChannel channel);
    void afterAskingDesc(User author, TextChannel channel);
    void afterAskingImg(User author, TextChannel channel);
    void afterAskingFooter(User author, TextChannel channel);
    void afterAskingMessageContent(TextChannel channel);
}