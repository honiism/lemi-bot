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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonMenu {

    private final EventWaiter waiter;
    private final long timeout;
    private final JDA jda;
    private final Set<Long> allowedUsers;
    private final List<Button> buttons;
    private final List<Runnable> actions;
    private final Runnable finalAction;
    private final Runnable timeoutAction;

    private ButtonMenu(EventWaiter waiter, long timeout, JDA jda, Set<Long> allowedUsers,
            List<Button> buttons, List<Runnable> actions, Runnable finalAction, Runnable timeoutAction) {
        this.waiter = waiter;
        this.timeout = timeout;
        this.jda = jda;
        this.allowedUsers = allowedUsers;
        this.buttons = buttons;
        this.actions = actions;
        this.finalAction = finalAction;
        this.timeoutAction = timeoutAction;
    }

    public static class Builder {
        private final JDA jda;
        private final Set<Long> allowedUsers = new HashSet<>();

        private EventWaiter waiter;
        private long timeout = -1;
        private List<Button> buttons;
        private List<Runnable> actions;
        private Runnable finalAction;
        private Runnable timeoutAction;
    }
}