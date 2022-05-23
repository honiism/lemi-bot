package com.honiism.discord.lemi.utils.wrapper;

import net.dv8tion.jda.api.entities.Message;

public class CachedMessage {

    private final long messageId;
    private final long authorId;

    private final String content;
    private final String contentDis;

    public CachedMessage(Message message) {
        this.messageId = message.getIdLong();
        this.authorId = message.getAuthor().getIdLong();
        this.content = message.getContentRaw();
        this.contentDis = message.getContentDisplay();
    }

    public long getMsgId() {
        return messageId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getContentDis() {
        return contentDis;
    }
}