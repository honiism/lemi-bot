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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserData {

    @JsonProperty("id")
    private final long id;

    @JsonProperty("balance")
    private long balance;
    @JsonProperty("deaths")
    private long deaths;

    @JsonProperty("passive_mode")
    private boolean passiveMode;
    @JsonProperty("inventory")
    private List<InventoryData> InventoryData;

    public UserData(@JsonProperty("id") long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }

    public long getdeaths() {
        return deaths;
    }

    public boolean getPassiveMode() {
        return passiveMode;
    }

    public List<InventoryData> getInventory() {
        return InventoryData;
    }

    public void setBalance(long balance) {
        if (balance < 1000) {
            balance = 1000;
        }

        this.balance = balance;
    }

    public void setdeaths(long deaths) {
        this.deaths = deaths;
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public void setInventory(List<InventoryData> InventoryData) {
        this.InventoryData = InventoryData;
    }
}