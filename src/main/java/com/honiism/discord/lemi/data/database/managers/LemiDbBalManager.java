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

import java.util.List;

import com.honiism.discord.lemi.data.database.LemiDbBalDs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface LemiDbBalManager {
    LemiDbBalManager INS = new LemiDbBalDs();

    // currency
    boolean userHasCurrProfile(long userId);
    void addUserCurrProfile(long userId);
    void addUserInvProfile(long userId);
    long getUserBal(Long userId);
    void updateUserBal(Long userId, long balToUpdate);
    List<String> getOwnedItems(Long userId);
    long getItemFromUserInv(Long userId, String itemName);
    boolean checkIfItemExists(String itemName);
    void updateItemUser(Long userId, String itemName, long amountToUpdate);
    void removeAllItems(Long userId, Guild guild);
    void removeCurrData(Long userId, Guild guild);
    void addNewItemToDb(String itemId, InteractionHook hook);
    void removeItemFromDb(String itemId, InteractionHook hook);
}