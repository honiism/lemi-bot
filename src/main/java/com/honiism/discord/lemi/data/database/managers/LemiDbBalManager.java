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

package com.honiism.discord.lemi.data.database.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.data.database.LemiDbBalDs;

import net.dv8tion.jda.api.interactions.InteractionHook;

public interface LemiDbBalManager {
    LemiDbBalManager INS = new LemiDbBalDs();

    // users
    boolean userHasData(long userId);
    void addUserData(long userId);
    void update(long userId, String jsonData);
    String getUserData(long userId);
    void removeItemFromDb(String itemId, InteractionHook hook) throws JsonMappingException, JsonProcessingException;
}
    