package com.honiism.discord.lemi.commands.slash.currency.objects.items.handler;

import net.dv8tion.jda.api.interactions.InteractionHook;

public interface ItemInterface {
    String getName();
    String getDescription();
    String getId();
    String getEmoji();
    boolean isSellable();
    boolean isBuyable();
    boolean isGiftAble();
    boolean isLimited();
    boolean isUsable();
    boolean useableAfterLimit();
    boolean disappearAfterUsage();
    String getLimitedDate();
    long getBuyingPrice();
    long getSellingPrice();
    ItemCategory getCategory();
    ItemType getType();
    EventType getEventType();
    void useItem(InteractionHook hook);
}