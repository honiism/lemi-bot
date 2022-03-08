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

package com.honiism.discord.lemi.utils.paginator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import java.awt.*;
import java.time.Instant;

import javax.annotation.Nonnull;

import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Emojis;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.internal.utils.Checks;

public class Paginator {
        
    private static Button first = Button.secondary("first", Emoji.fromMarkdown(Emojis.SKIP_TO_START_BUTTON));
    private static Button previous = Button.secondary("previous", Emoji.fromMarkdown(Emojis.LEFT_ARROW));
    private static Button next = Button.secondary("next", Emoji.fromMarkdown(Emojis.RIGHT_ARROW));
    private static Button last = Button.secondary("last", Emoji.fromMarkdown(Emojis.SKIP_TO_END_BUTTON));
    private static Button delete = Button.danger("stop", Emoji.fromMarkdown(Emojis.CROSS_MARK));
    
    private final EventWaiter waiter;
    private final int itemsPerPage;
    private final int pages;
    private final long timeout;
    private final List<String> items;
    private final JDA jda;
    private final Set<Long> allowedUsers;
    private final boolean numbered;
    private final String msgContent;
    private final Color color;
    private final String footer;
    private final String embedTitle;
    private final String thumbnailUrl;
    private final String embedDesc;
    private final String imageUrl;
    private final boolean timestamp;

    private int page = 1;
    private boolean interactionStopped = false;

    private Paginator(EventWaiter waiter, long timeout, List<String> items, JDA jda,
                      Set<Long> allowedUsers, int itemsPerPage, boolean numberedItems, String msgContent,
                      Color color, String footer, String embedTitle, String thumbnailUrl, String embedDesc,
                      String imageUrl, boolean timestamp) {
        this.waiter = waiter;
        this.timeout = timeout;
        this.items = items;
        this.jda = jda;
        this.allowedUsers = Collections.unmodifiableSet(allowedUsers);
        this.itemsPerPage = itemsPerPage;
        this.numbered = numberedItems;
        this.msgContent = msgContent;
        this.color = color;
        this.footer = footer;
        this.pages = (int) Math.ceil((double) items.size() / itemsPerPage);
        this.embedTitle = embedTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.embedDesc = embedDesc;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public void paginate(Message message, int page) {
        this.page = page;

        if (msgContent == null) {
            message.editMessageEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        } else {
            message.editMessage(msgContent).setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        }
    }

    public void paginate(MessageAction messageAction, int page) {
        this.page = page;

        if (msgContent == null) {
            messageAction.setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        } else {
            messageAction.content(msgContent).setEmbeds(getEmbed(page)).setActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        }
    }

    public void paginate(WebhookMessageAction<Message> action, int page) {
        this.page = page;

        if (msgContent == null) {
            action.addEmbeds(getEmbed(page)).addActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        } else {
            action.setContent(msgContent).addEmbeds(getEmbed(page)).addActionRows(getButtonLayout(page))
                .queue(m -> waitForEvent(m.getChannel().getIdLong(), m.getIdLong()));
        }
    }

    private ActionRow getButtonLayout(int page) {
        if (pages > 2) {
            return ActionRow.of(
                    page <= 1 ? first.asDisabled() : first,
                    page <= 1 ? previous.asDisabled() : previous,
                    delete,
                    page >= pages ? next.asDisabled() : next,
                    page >= pages ? last.asDisabled() : last);
        } else {
            return ActionRow.of(
                    page <= 1 ? previous.asDisabled() : previous,
                    delete,
                    page >= pages ? next.asDisabled() : next);
        }
    }

    private void waitForEvent(long channelId, long messageId) {
        waiter.waitForEvent(
                ButtonInteractionEvent.class,
                event -> {
                    if (interactionStopped) {
                        return false;
                    }
                    
                    if (messageId != event.getMessageIdLong()) {
                        return false;
                    }
                    
                    if (allowedUsers.size() >= 1) {
                        if (!allowedUsers.contains(event.getUser().getIdLong())) {
                            event.deferEdit().queue(s -> {}, e -> {});
                            return false;
                        }
                    }

                    return true;
                },
                event -> {
                    switch (event.getComponentId()) {
                        case "previous":
                            page--;

                            if (page < 1) {
                                page = 1;
                            }
                            
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                            break;

                        case "next":
                            page++;
                            
                            if (page > pages) {
                                page = pages;
                            }
                            
                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                            break;

                        case "stop":
                            interactionStopped = true;

                            if (event.getMessage() != null) {
                                event.getMessage().delete().queue(s -> {}, e -> {});
                            } else {
                                event.editMessageEmbeds(getEmbed(page)).setActionRows(Collections.emptyList()).queue();
                            }
                            break;

                        case "first":
                            page = 1;

                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                            break;

                        case "last":
                            page = pages;

                            event.editMessageEmbeds(getEmbed(this.page)).setActionRows(getButtonLayout(page)).queue();
                            waitForEvent(event.getChannel().getIdLong(), event.getMessageIdLong());
                    }
                },
                timeout,
                TimeUnit.SECONDS,
                () -> {
                    interactionStopped = true;
                    TextChannel channel = jda.getTextChannelById(channelId);

                    if (channel == null) {
                        return;
                    }

                    channel.retrieveMessageById(messageId)
                        .flatMap(m -> m.editMessageComponents(Collections.emptyList()))
                        .queue(s -> {}, e -> {});
                }
        );
    }

    private MessageEmbed getEmbed(int page) {
        if (page > pages) {
            page = pages;
        }
        
        if (page < 1) {
            page = 1;
        }

        int start = page == 1 ? 0 : ((page - 1) * itemsPerPage);
        int end = Math.min(items.size(), page * itemsPerPage);

        StringBuilder itemsStringBuilder = new StringBuilder();

        for (int i = start; i < end; i++) {
            itemsStringBuilder.append(numbered ? "`" + (i + 1) + ".` " : "").append(this.items.get(i)).append("\n");
        }

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(embedTitle)
            .setThumbnail(thumbnailUrl)
            .setImage(imageUrl)
            .setFooter("Page " + page + "/" + pages + (footer != null ? " • " + footer : ""))
            .setColor(color)
            .setDescription(embedDesc + "\r\n๑‧˚₊꒷꒦︶︶︶︶︶꒷꒦︶︶︶︶︶✦‧₊˚⊹\r\n" + itemsStringBuilder.toString().trim());

        if (timestamp) {
            builder.setTimestamp(Instant.now());
        }
        
        return builder.build();
    }

    public static class Builder {

        private final JDA jda;
        private final Set<Long> allowedUsers = new HashSet<Long>();

        private EventWaiter waiter;
        private long timeout = -1;
        private List<String> items;
        private int itemsPerPage = 10;
        private boolean numberItems = true;
        private String msgContent = null;
        private Color color;
        private String footer;
        private String embedTitle;
        private String thumbnailUrl;
        private String embedDesc;
        private String imageUrl;
        private boolean timestamp;

        public Builder(JDA jda) {
            this.jda = jda;
        }

        public Builder setEventWaiter(@Nonnull EventWaiter waiter) {
            this.waiter = waiter;
            return this;
        }

        public Builder useTimestamp(boolean timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setImage(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setEmbedDesc(String embedDesc) {
            this.embedDesc = embedDesc;
            return this;
        }

        public Builder setThumbnail(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public Builder setEmbedTitle(String embedTitle) {
            this.embedTitle = embedTitle;
            return this;
        }

        public Builder setTimeout(long delay, TimeUnit unit) {
            Checks.notNull(unit, "TimeUnit");
            Checks.check(delay > 0, "Timeout must be greater than 0!");
            timeout = unit.toSeconds(delay);
            return this;
        }

        public Builder setItems(List<String> items) {
            this.items = items;
            return this;
        }

        public Builder addAllowedUsers(Long... userIds) {
            allowedUsers.addAll(Set.of(userIds));
            return this;
        }

        public Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder setColor(int color) {
            this.color = EmbedUtils.intToColor(color);
            return this;
        }

        public Builder setItemsPerPage(int items) {
            Checks.check(items > 0, "Items per page must be at least 1");
            this.itemsPerPage = items;
            return this;
        }

        public Builder useNumberedItems(boolean b) {
            this.numberItems = b;
            return this;
        }

        public Builder setMsgContentContent(String msgContent) {
            this.msgContent = msgContent;
            return this;
        }

        public Builder setFooter(String footer) {
            this.footer = footer;
            return this;
        }

        public Paginator build() {
            Checks.notNull(waiter, "Waiter");
            Checks.check(timeout != -1, "You must set a timeout using #setTimeout()!");
            Checks.noneNull(items, "Items");
            
            return new Paginator(waiter, timeout, items, jda, allowedUsers, itemsPerPage,
                                 numberItems, msgContent, color == null ? Color.black : color,
                                 footer, embedTitle, thumbnailUrl, embedDesc, imageUrl, timestamp);
        }
    }       
}