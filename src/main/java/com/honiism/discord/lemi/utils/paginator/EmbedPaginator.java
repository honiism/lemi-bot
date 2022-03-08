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

import javax.annotation.Nonnull;

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

public class EmbedPaginator {

    private final EventWaiter waiter;
    private final int pages;
    private final long timeout;
    private final List<EmbedBuilder> items;
    private final JDA jda;
    private final Set<Long> allowedUsers;
    private final String msgContent;
    private final String footer;

    private Button first = Button.secondary("first", Emoji.fromMarkdown(Emojis.SKIP_TO_START_BUTTON));
    private Button previous = Button.secondary("previous", Emoji.fromMarkdown(Emojis.LEFT_ARROW));
    private Button next = Button.secondary("next", Emoji.fromMarkdown(Emojis.RIGHT_ARROW));
    private Button last = Button.secondary("last", Emoji.fromMarkdown(Emojis.SKIP_TO_END_BUTTON));
    private Button delete = Button.danger("stop", Emoji.fromMarkdown(Emojis.CROSS_MARK));

    private int page = 1;
    private boolean interactionStopped = false;

    private EmbedPaginator(EventWaiter waiter, long timeout, List<EmbedBuilder> items, JDA jda,
                           Set<Long> allowedUsers, String msgContent, String footer) {
        this.waiter = waiter;
        this.timeout = timeout;
        this.items = items;
        this.jda = jda;
        this.allowedUsers = Collections.unmodifiableSet(allowedUsers);
        this.msgContent = msgContent;
        this.footer = footer;
        this.pages = items.size();
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
        
        int start = page == 1 ? 0 : ((page - 1));
        int end = Math.min(items.size(), page);
        
        for (int i = start; i < end; i++) {
            this.items.get(i).setFooter("Page " + page + "/" + pages + (footer != null ? " • " + footer : ""));
        }

        return this.items.get(page - 1).build();
    }

    public static class Builder {

        private final JDA jda;
        private final Set<Long> allowedUsers = new HashSet<>();

        private EventWaiter waiter;
        private long timeout = -1;
        private List<EmbedBuilder> items;
        private String msgContent = null;
        private String footer;

        public Builder(JDA jda) {
            this.jda = jda;
        }

        public Builder setEventWaiter(@Nonnull EventWaiter waiter) {
            this.waiter = waiter;
            return this;
        }

        public Builder setTimeout(long delay, TimeUnit unit) {
            Checks.notNull(unit, "TimeUnit");
            Checks.check(delay > 0, "Timeout must be greater than 0!");
            timeout = unit.toSeconds(delay);
            return this;
        }

        public Builder setItems(List<EmbedBuilder> items) {
            this.items = items;
            return this;
        }

        public Builder addAllowedUsers(Long... userIds) {
            allowedUsers.addAll(Set.of(userIds));
            return this;
        }

        public Builder setMSgContent(String msgContent) {
            this.msgContent = msgContent;
            return this;
        }

        public Builder setFooter(String footer) {
            this.footer = footer;
            return this;
        }

        public EmbedPaginator build() {
            Checks.notNull(waiter, "Waiter");
            Checks.check(timeout != -1, "You must set a timeout using #setTimeout()!");
            Checks.noneNull(items, "items");
            Checks.notEmpty(items, "items");
            return new EmbedPaginator(waiter, timeout, items, jda, allowedUsers, msgContent, footer);
        }
    }       
}