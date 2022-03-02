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

import net.dv8tion.jda.api.interactions.InteractionHook;

public interface IEmbedListener {
    void afterAskingId(InteractionHook hook);
    void afterAskingTitle(InteractionHook hook);
    void afterAskingColor(InteractionHook hook);
    void afterAskingAuthor(InteractionHook hook);
    void afterAskingThumbnail(InteractionHook hook);
    void afterAskingDesc(InteractionHook hook);
    void afterAskingImg(InteractionHook hook);
    void afterAskingFooter(InteractionHook hook);
    void afterAskingMessageContent(InteractionHook hook);
}