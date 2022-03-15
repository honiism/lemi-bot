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

package com.honiism.discord.lemi.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.UserData.InventoryData;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.data.items.Items;

public class UserDataManager {
    
    private final long userId;
    private final UserData userData;

    public UserDataManager(long userId, String dataJson) throws JsonMappingException, JsonProcessingException {
        this.userId = userId;
        this.userData = Lemi.getInstance().getObjectMapper().readValue(dataJson, UserData.class);
    }

    public long getId() {
        return userId;
    }

    public UserData getData() {
        return userData;
    }

    public long getBal() {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        return getData().getBalance();
    }

    public void addBalToUser(long balToAdd) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }
        
        long userBal = getData().getBalance();
        long balAfterAdd = userBal + balToAdd;

        getData().setBalance(balAfterAdd);

        try {
            LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void removeBalFromUser(long balToRemove) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }
        
        long userBal = getData().getBalance();
        long balAfterRemove = userBal - balToRemove;

        getData().setBalance(balAfterRemove);

        try {
            LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<Items> getOwnedItems() {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        List<Items> ownedItems = new ArrayList<>();
        
        for (InventoryData itemData : getData().getInventory()) {
            if (Items.getItemById(itemData.getId()) == null) {
                continue;
            }
            ownedItems.add(Items.getItemById(itemData.getId()));
        }

        return ownedItems;
    }

    public Items getItemFromInv(String itemId) {
        return getOwnedItems().stream()
                .filter(itemData -> itemData.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    public List<String> getFormattedItems() {
        List<String> ownedItems = new ArrayList<>();
        
        for (InventoryData itemData : getData().getInventory()) {
            if (Items.getItemById(itemData.getId()) == null) {
                continue;
            }

            Items item = Items.getItemById(itemData.getId());
            ownedItems.add(item.getEmoji() + " " + item.getName() + " : " + itemData.getCount());
        }

        return ownedItems;
    }

    public long getItemCountFromUser(String itemId) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        for (InventoryData itemData : getData().getInventory()) {
            if (itemData.getId().equals(itemId)) {
                return itemData.getCount();
            }
        }

        return 0;
    }

    public boolean userHasItem(String itemId) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        InventoryData targetItem = getData().getInventory().stream()
                .filter(itemData -> itemData.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (targetItem != null && targetItem.getCount() > 0) {
            return true;
        }

        return false;
    }

    public void addItemToUser(String itemId, long amountToAdd) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        InventoryData targetItem = getData().getInventory().stream()
            .filter(itemData -> itemData.getId().equals(itemId))
            .findFirst()
            .orElse(null);

        long userItemAmount = getItemCountFromUser(itemId);
        long amountAfterAdd = userItemAmount + amountToAdd;

        if (targetItem == null) {
            InventoryData newInvItem = getData().new InventoryData(itemId);
        
            newInvItem.setName(itemId);
            newInvItem.setCount(amountAfterAdd);

            getData().getInventory().add(newInvItem);
        } else {
            targetItem.setCount(amountAfterAdd);
        }

        try {
            LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void removeItemFromUser(String itemId, long amountToRemove) {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
        }

        InventoryData targetItem = getData().getInventory().stream()
            .filter(itemData -> itemData.getId().equals(itemId))
            .findFirst()
            .get();

        long userItemAmount = getItemCountFromUser(targetItem.getName());
        long amountAfterRemove = userItemAmount - amountToRemove;

        if (amountAfterRemove == 0) {
            getData().getInventory().remove(targetItem);
        } else {
            targetItem.setCount(amountAfterRemove);
        }

        try {
            LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void clearInv() {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
            return;
        }

        if (!getData().getInventory().isEmpty()) {
            getData().setInventory(new ArrayList<InventoryData>());

            try {
                LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearBal() {
        if (!LemiDbBalManager.INS.userHasData(getId())) {
            LemiDbBalManager.INS.addUserData(getId());
            return;
        }

        getData().setBalance(1000);

        try {
            LemiDbBalManager.INS.update(getId(), Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void removeData() {
        clearInv();
        clearBal();
    }
}