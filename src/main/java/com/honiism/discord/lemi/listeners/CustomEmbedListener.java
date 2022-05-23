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

import com.honiism.discord.lemi.utils.customEmbeds.EmbedTools;
import com.honiism.discord.lemi.utils.customEmbeds.IEmbedListener;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CustomEmbedListener implements IEmbedListener {
    EmbedTools embedTools = new EmbedTools();

    @Override
    public void afterAskingId(User author, TextChannel channel) {
        embedTools.askForTitle(author, channel);
    }

    @Override
    public void afterAskingTitle(User author, TextChannel channel) {
        embedTools.askForColor(author, channel);
    }

    @Override
    public void afterAskingColor(User author, TextChannel channel) {
        embedTools.askForAuthor(author, channel);
    }

    @Override
    public void afterAskingAuthor(User author, TextChannel channel) {
        embedTools.askForThumbnail(author, channel);
    }

    @Override
    public void afterAskingThumbnail(User author, TextChannel channel) {
        embedTools.askForDesc(author, channel);         
    }

    @Override
    public void afterAskingDesc(User author, TextChannel channel) {
        embedTools.askForImage(author, channel);
    }

    @Override
    public void afterAskingImg(User author, TextChannel channel) {
        embedTools.askForFooter(author, channel);
                
    }

    @Override
    public void afterAskingFooter(User author, TextChannel channel) {
        embedTools.askForMessageContent(author, channel);
    }
    
    @Override
    public void afterAskingMessageContent(TextChannel channel) {
        embedTools.sendCreatedEmbed(channel);
    }
}