package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.utils.customEmbeds.EmbedTools;
import com.honiism.discord.lemi.utils.customEmbeds.IEmbedSync;

import net.dv8tion.jda.api.interactions.InteractionHook;

public class CustomEmbedListener implements IEmbedSync {
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