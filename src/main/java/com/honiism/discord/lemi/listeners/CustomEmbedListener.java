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

import net.dv8tion.jda.api.interactions.InteractionHook;

public class CustomEmbedListener implements IEmbedListener {
    EmbedTools embedTools = new EmbedTools();

    @Override
    public void afterAskingId(InteractionHook hook) {
        embedTools.askForTitle(hook);
    }

    @Override
    public void afterAskingTitle(InteractionHook hook) {
        embedTools.askForColor(hook);
    }

    @Override
    public void afterAskingColor(InteractionHook hook) {
        embedTools.askForAuthor(hook);
    }

    @Override
    public void afterAskingAuthor(InteractionHook hook) {
        embedTools.askForThumbnail(hook);
    }

    @Override
    public void afterAskingThumbnail(InteractionHook hook) {
        embedTools.askForDesc(hook);         
    }

    @Override
    public void afterAskingDesc(InteractionHook hook) {
        embedTools.askForImage(hook);
    }

    @Override
    public void afterAskingImg(InteractionHook hook) {
        embedTools.askForFooter(hook);
                
    }

    @Override
    public void afterAskingFooter(InteractionHook hook) {
        embedTools.askForMessageContent(hook);
    }
    
    @Override
    public void afterAskingMessageContent(InteractionHook hook) {
        embedTools.sendCreatedEmbed(hook);
    }
}