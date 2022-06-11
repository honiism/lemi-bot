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

package com.honiism.discord.lemi.utils.buttons;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.Checks;

public class ButtonMenu {

    private final EventWaiter waiter;
    private final long timeout;
    private final JDA jda;
    private final Set<Long> allowedUsers;
    private final Map<Button, Runnable> actions;
    private final Map<Button, Modal> modals;
    private final Map<Button, Runnable> finalActions;

    private ButtonMenu(EventWaiter waiter, long timeout, JDA jda, Set<Long> allowedUsers,
            Map<Button, Runnable> actions, Map<Button, Modal> modals, Map<Button, Runnable> finalActions) {
        this.waiter = waiter;
        this.timeout = timeout;
        this.jda = jda;
        this.allowedUsers = allowedUsers;
        this.actions = actions;
        this.modals = modals;
        this.finalActions = finalActions;
    }

    public void waitAndModal(Message message, long channelId) {
        this.waiter.waitForEvent(
                ButtonInteractionEvent.class,
                (event) -> {
                    if (message.getIdLong() != event.getMessageIdLong()) {
                        return false;
                    }

                    if (allowedUsers.size() >= 1) {
                        if (!allowedUsers.contains(event.getMember().getIdLong())) {
                            return false;
                        }
                    }

                    return true;
                },
                (event) -> {
                    Button buttonClicked = event.getButton();

                    event.getMessage().editMessageComponents(Collections.emptyList()).queue();

                    if (actions.containsKey(buttonClicked)) {
                        actions.get(buttonClicked).run();
                    }

                    if (modals.containsKey(buttonClicked)) {
                        event.replyModal(modals.get(buttonClicked)).queue(
                            (success) -> {
                                if (finalActions.containsKey(buttonClicked)) {
                                    finalActions.get(buttonClicked).run();
                                }
                            }
                        );

                    } else {
                        if (finalActions.containsKey(buttonClicked)) {
                            finalActions.get(buttonClicked).run();
                        }
                    }
                },
                timeout,
                TimeUnit.SECONDS,
                () -> {
                    TextChannel channel = jda.getTextChannelById(channelId);
                    
                    if (channel == null) {
                        return;
                    }

                    channel.retrieveMessageById(message.getIdLong())
                        .flatMap(m -> m.editMessageComponents(Collections.emptyList()))
                        .queue(s -> {}, e -> {});
                }
        );
    }

    public void waitForEvent(Message message, long channelId) {
        this.waiter.waitForEvent(
                ButtonInteractionEvent.class,
                (event) -> {
                    if (message.getIdLong() != event.getMessageIdLong()) {
                        return false;
                    }

                    if (allowedUsers.size() >= 1) {
                        if (!allowedUsers.contains(event.getMember().getIdLong())) {
                            return false;
                        }
                    }

                    return true;
                },
                (event) -> {
                    actions.get(event.getButton()).run();
                },
                timeout,
                TimeUnit.SECONDS,
                () -> {
                    TextChannel channel = jda.getTextChannelById(channelId);
                    
                    if (channel == null) {
                        return;
                    }

                    channel.retrieveMessageById(message.getIdLong())
                        .flatMap(m -> m.editMessageComponents(Collections.emptyList()))
                        .queue(s -> {}, e -> {});
                }
        );
    }

    public static class Builder {
        private final JDA jda;
        private final Set<Long> allowedUsers = new HashSet<>();

        private EventWaiter waiter;
        private long timeout = -1;
        private Map<Button, Runnable> actions;
        private Map<Button, Modal> modals;
        private Map<Button, Runnable> finalActions;

        public Builder(JDA jda) {
            this.jda = jda;
        }

        public Builder setWwaiter (EventWaiter waiter) {
            this.waiter = waiter;
            return this;
        }

        public Builder setFinalActions(Map<Button, Runnable> finalActions) {
            this.finalActions = finalActions;
            return this;
        }

        public Builder setModals(Map<Button, Modal> modals) {
            this.modals = modals;
            return this;
        }

        public Builder setTimeout(long delay, TimeUnit unit) {
            Checks.notNull(unit, "TimeUnit");
            Checks.check(delay > 0, "Timeout must be greater than 0!");
            timeout = unit.toSeconds(delay);
            return this;
        }

        public Builder addAllowedUsers(Long... userIds) {
            allowedUsers.addAll(Set.of(userIds));
            return this;
        }

        public Builder setActions(Map<Button, Runnable> actions) {
            this.actions = actions;
            return this;
        }

        public ButtonMenu build() {
            Checks.notNull(waiter, "Waiter");
            Checks.check(timeout != -1, "You must set a timeout using #setTimeout()!");
            Checks.notNull(actions, "actions");

            return new ButtonMenu(waiter, timeout, jda, allowedUsers, actions, modals, finalActions);
        }
    }
}