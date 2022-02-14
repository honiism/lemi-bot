package com.honiism.discord.lemi.database.managers;

import java.util.List;

import com.honiism.discord.lemi.database.LemiDbBalDs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface LemiDbBalManager {
    LemiDbBalManager INS = new LemiDbBalDs();

    // currency
    boolean userHasCurrProfile(Member member);
    void addUserCurrProfile(Member member);
    void addUserInvProfile(Member member);
    long getUserBal(String userId);
    void addBalToUser(String userId, long userBal, long balToAdd);
    void removeBalFromUser(String userId, long userBal, long balToRemove);
    void updateUserBal(String userId, long balToUpdate);
    List<String> getOwnedItems(String userId);
    long getItemFromUserInv(String userId, String itemName);
    boolean checkIfItemExists(String itemName);
    void addItemToUser(String userId, String itemName, long userItemAmount, long amountToAdd);
    void removeItemFromUser(String userId, String itemName, long userItemAmount, long amountToRemove);
    void updateItemUser(String userId, String itemName, long amountToUpdate);
    void removeAllItems(String userId, Guild guild);
    void removeCurrData(String userId, Guild guild);
    void addNewItemToDb(String itemId, InteractionHook hook);
    void removeItemFromDb(String itemId, InteractionHook hook);
    void addItemsToDb();
}