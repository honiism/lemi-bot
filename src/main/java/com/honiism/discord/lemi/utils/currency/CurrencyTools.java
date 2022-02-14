package com.honiism.discord.lemi.utils.currency;

import java.util.ArrayList;
import java.util.List;

import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.EventType;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.ItemType;
import com.honiism.discord.lemi.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.database.managers.LemiDbManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class CurrencyTools {

    public static boolean userHasCurrProfile(Member member) {
        return LemiDbBalManager.INS.userHasCurrProfile(member);
    }

    public static void addAllProfiles(Member member) {
        addUserCurrProfile(member);
        addUserInvProfile(member);
    }

    public static void addUserCurrProfile(Member member) {
        LemiDbBalManager.INS.addUserCurrProfile(member);
    }

    public static void addUserInvProfile(Member member) {
        LemiDbBalManager.INS.addUserInvProfile(member);
    }

    public static String getBalName(String guildId) {
        return LemiDbManager.INS.getBalName(guildId);
    }

    public static long getUserbal(String userId) {
        return LemiDbBalManager.INS.getUserBal(userId); 
    }

    public static void addBalToUser(String userId, long userBal, long balToAdd) {
        LemiDbBalManager.INS.addBalToUser(userId, userBal, balToAdd);
    }

    public static void removeBalFromUser(String userId, long userBal, long balToRemove) {
        LemiDbBalManager.INS.removeBalFromUser(userId, userBal, balToRemove);
    }

    public static void updateUserBal(String userId, long balToUpdate) {
        LemiDbBalManager.INS.updateUserBal(userId, balToUpdate);
    }

    public static List<String> getOwnedItems(String userId) {
        return LemiDbBalManager.INS.getOwnedItems(userId);
    }

    public static long getItemFromUserInv(String userId, String itemName) {
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

    public static boolean userHasItem(String userId, String itemName) {
        for (String ownedItemData : CurrencyTools.getOwnedItems(userId)) {
            if (ownedItemData.contains(itemName)) {
                return true;
            }
        }
        return false;
    }

    public static void onGuildReadyAddProf(Guild guild) {
        guild.loadMembers()
            .onSuccess((memberList) -> {
                memberList.stream()
                    .filter(m -> !LemiDbBalManager.INS.userHasCurrProfile(m) && !m.getUser().isBot())
                    .forEach(m -> {
                        CurrencyTools.addAllProfiles(m);
                    });
            });
    }

    public static void addItemToUser(String userId, String itemName, long userItemAmount, long amountToAdd) {
        LemiDbBalManager.INS.addItemToUser(userId, itemName, userItemAmount, amountToAdd);
    }

    public static void updateItemUser(String userId, String itemName, long amountToUpdate) {
        LemiDbBalManager.INS.updateItemUser(userId, itemName, amountToUpdate);
    }

    public static void removeItemFromUser(String userId, String itemName, long userItemAmount, long amountToRemove) {
        LemiDbBalManager.INS.removeItemFromUser(userId, itemName, userItemAmount, amountToRemove);
    }

    public static void removeAllItems(String userId, Guild guild) {
        LemiDbBalManager.INS.removeAllItems(userId, guild);
    }

    public static void removeCurrData(String userId, Guild guild) {
        LemiDbBalManager.INS.removeCurrData(userId, guild);
    }

    public static void removeUserData(String userId, Guild guild) {
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

    public static void addItemsToDb() {
        LemiDbBalManager.INS.addItemsToDb();
    }
}