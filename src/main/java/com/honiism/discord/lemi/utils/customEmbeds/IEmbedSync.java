package com.honiism.discord.lemi.utils.customEmbeds;

import net.dv8tion.jda.api.interactions.InteractionHook;

public interface IEmbedSync {
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