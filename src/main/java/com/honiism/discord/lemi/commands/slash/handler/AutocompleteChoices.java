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

package com.honiism.discord.lemi.commands.slash.handler;

import net.dv8tion.jda.api.interactions.commands.Command;

public class AutocompleteChoices {

    private final String name;
    private final String value;

    public AutocompleteChoices(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Command.Choice toCommandAutocompleteChoice() {
        return new Command.Choice(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}