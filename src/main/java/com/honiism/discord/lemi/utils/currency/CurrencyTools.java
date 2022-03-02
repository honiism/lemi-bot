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

package com.honiism.discord.lemi.utils.currency;

import java.util.ArrayList;
import java.util.List;

import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.EventType;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.ItemType;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class CurrencyTools {

    public static boolean userHasCurrProfile(long userId) {
        return LemiDbBalManager.INS.userHasCurrProfile(userId);
    }

    public static void addAllProfiles(long userId) {
        addUserCurrProfile(userId);
        addUserInvProfile(userId);
    }

    public static void addUserCurrProfile(long userId) {
        LemiDbBalManager.INS.addUserCurrProfile(userId);
    }

    public static void addUserInvProfile(long userId) {
        LemiDbBalManager.INS.addUserInvProfile(userId);
    }

    public static String getBalName() {
        return CustomEmojis.BALANCE;
    }

    public static long getUserBal(Long userId) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }
        return LemiDbBalManager.INS.getUserBal(userId); 
    }

    public static void addBalToUser(Long userId, long balToAdd) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }
        
        long userBal = getUserBal(userId);
        long balAfterAdd = userBal + balToAdd;

        updateUserBal(userId, balAfterAdd);
    }

    public static void removeBalFromUser(Long userId, long balToRemove) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }

        long userBal = getUserBal(userId);
        long balAfterRemove = userBal - balToRemove;

        updateUserBal(userId, balAfterRemove);
    }

    public static void updateUserBal(Long userId, long balToUpdate) {
        LemiDbBalManager.INS.updateUserBal(userId, balToUpdate);
    }

    public static List<String> getOwnedItems(Long userId) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }
        return LemiDbBalManager.INS.getOwnedItems(userId);
    }

    public static long getItemFromUserInv(Long userId, String itemName) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }
        return LemiDbBalManager.INS.getItemFromUserInv(userId, itemName);
    }

    public static boolean checkIfItemExists(String itemName) {
        return LemiDbBalManager.INS.checkIfItemExists(itemName);
    }

    public static List<Items> getItems() {
        return Items.allItems;
    }

    public static List<Items> getCommonItems() {
        List<Items> commonItems = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (!item.getEventType().equals(EventType.NONE)) {
                continue;
            }
            commonItems.add(item);
        }

        return commonItems;
    }

    public static List<Items> getEventItems() {
        List<Items> eventItems = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (item.getEventType().equals(EventType.NONE)) {
                continue;
            }
            eventItems.add(item);
        }

        return eventItems;
    }

    public static List<Items> getItemsByType(ItemType itemType) {
        List<Items> itemsByType = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (!item.getType().equals(itemType)) {
                continue;
            }
            itemsByType.add(item);
        }

        return itemsByType;
    }

    public static List<Items> getItemsByName(String itemName) {
        List<Items> itemsByName = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (!item.getName().equals(itemName)) {
                continue;
            }
            itemsByName.add(item);
        }

        return itemsByName;
    }

    public static List<Items> getItemsById(String itemId) {
        List<Items> itemsById = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (!item.getId().equals(itemId)) {
                continue;
            }
            itemsById.add(item);
        }

        return itemsById;
    }

    public static List<Items> getEventItemsByType(EventType eventType) {
        List<Items> eventItemsByType = new ArrayList<Items>();

        for (Items item : getItems()) {
            if (!item.getEventType().equals(eventType)) {
                continue;
            }
            eventItemsByType.add(item);
        }

        return eventItemsByType;
    }

    public static boolean userHasItem(Long userId, String itemName) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }

        if (CurrencyTools.getOwnedItems(userId).contains(itemName)) {
            return true;
        }
        return false;
    }

    public static void addItemToUser(Long userId, String itemName, long amountToAdd) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }

        long userItemAmount = getItemFromUserInv(userId, itemName);
        long itemAfterAdd = userItemAmount + amountToAdd;

        updateItemUser(userId, itemName, itemAfterAdd);
    }

    public static void updateItemUser(Long userId, String itemName, long amountToUpdate) {
        LemiDbBalManager.INS.updateItemUser(userId, itemName, amountToUpdate);
    }

    public static void removeItemFromUser(Long userId, String itemName, long amountToRemove) {
        if (!userHasCurrProfile(userId)) {
            addAllProfiles(userId);
        }

        long userItemAmount = getItemFromUserInv(userId, itemName);
        long itemAfterRemove = userItemAmount - amountToRemove;
        
        updateItemUser(userId, itemName, itemAfterRemove);
    }

    public static void removeAllItems(Long userId, Guild guild) {
        LemiDbBalManager.INS.removeAllItems(userId, guild);
    }

    public static void removeCurrData(Long userId, Guild guild) {
        LemiDbBalManager.INS.removeCurrData(userId, guild);
    }

    public static void removeUserData(Long userId, Guild guild) {
        removeAllItems(userId, guild);
        removeCurrData(userId, guild);
    }

    public static String[] getNPCs() {
        String[] npcList = new String[] {
            "Honey",
            "HoneyXD",
            "Fuku",
            "Lemi",
            "Hoppy",
            "Gumdrop",
            "Swirls",
            "God",
            "Dizzy",
            "Paimon",
            "Dark enchantress cookie"
        };
        return npcList;
    }

    public static String getRandomNPC() {
        WeightedRandom<String> randomNPC = new WeightedRandom<String>();

        for (String npcName : getNPCs()) {
            randomNPC.add(10, npcName);
        }

        return randomNPC.next();
    }

    public static void addNewItemToDb(String itemId, InteractionHook hook) {
        LemiDbBalManager.INS.addNewItemToDb(itemId, hook);
    }

    public static void removeItemFromDb(String itemId, InteractionHook hook) {
        LemiDbBalManager.INS.removeItemFromDb(itemId, hook);
    }

    public static void createInvDb() {
        LemiDbBalManager.INS.createInvDb();
    }
}