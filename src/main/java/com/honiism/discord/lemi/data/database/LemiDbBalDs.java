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

package com.honiism.discord.lemi.data.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.currency.InventoryData;
import com.honiism.discord.lemi.data.currency.UserData;
import com.honiism.discord.lemi.data.currency.UserDataManager;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Message;

public class LemiDbBalDs implements LemiDbBalManager {

    private static final Logger log = LoggerFactory.getLogger(LemiDbBalDs.class);
    private final HikariDataSource dataSource;

    public LemiDbBalDs() {
        try {
            File lemiDBFile = new File("LemiBalDb.db");

            if (!lemiDBFile.exists()) {
                if (lemiDBFile.createNewFile()) {
                    log.info("Created a database file (LemiBalDb.db)");
                } else {
                    log.error("Failed to create a database file.");
                }
            } else {
                log.info("Connected to LemiBalDb.db database file.");
            }
        } catch (IOException e) {
            log.error("\r\nSomething unexpected went wrong while trying to "
                    + "connect / create LemiBalDb.db file\r\n"
                    + "Error : IOException\r\n"
                    + "\r\n");

            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(Config.get("lemi_bal_db_url"));
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(300000);

        dataSource = new HikariDataSource(config);

        try (Statement statement = getConnection().createStatement()) {
            // user_currency_data
            statement.execute("CREATE TABLE IF NOT EXISTS user_currency_data ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id VARCHAR(20) NOT NULL,"
                    + "json_data VARCHAR(20) NOT NULL"
                    + ");"
            );
    
            log.info("user_currency_data table initialised");   
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "create / connect to database tables\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");
    
            e.printStackTrace();
        }
    }

    @Override
    public String getUserData(long userId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT json_data FROM user_currency_data WHERE user_id = ?")) {
            selectStatement.setLong(1, userId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("json_data");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        addUserData(userId);
        return getUserData(userId);
    }
    
    @Override
    public boolean userHasData(long userId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT json_data FROM user_currency_data WHERE user_id = ?")) {
            selectStatement.setLong(1, userId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    @Override
    public void addUserData(long userId) {
        try (Connection conn = getConnection();
                PreparedStatement insertStatement =
                    conn.prepareStatement("INSERT INTO user_currency_data(user_id, json_data) VALUES(?, ?)")) {

            UserData userData = new UserData(userId);

            userData.setBalance(1000);
            userData.setdeaths(0);
            userData.setPassiveMode(false);
            userData.setInventory(new ArrayList<InventoryData>());

            String jsonData = Lemi.getInstance().getObjectMapper().writeValueAsString(userData);
            
            insertStatement.setLong(1, userId);
            insertStatement.setString(2, jsonData);
            insertStatement.executeUpdate();
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(long userId, String jsonData) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	            conn.prepareStatement("UPDATE user_currency_data SET json_data = ? WHERE user_id = ?")) {
    	    updateStatement.setString(1, jsonData);
            updateStatement.setLong(2, userId);
            updateStatement.executeUpdate();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }
    }

    @Override
    public void removeItemFromUsers(String itemId, Message message) throws JsonMappingException, JsonProcessingException {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
    	            conn.prepareStatement("SELECT * FROM user_currency_data")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    long userId = rs.getLong("user_id");
                    String dataJson = rs.getString("json_data");

                    UserDataManager userDataManager = new UserDataManager(userId, dataJson);

                    InventoryData targetItem = userDataManager.getData().getInventory().stream()
                            .filter(itemData -> itemData.getId().equals(itemId))
                            .findFirst()
                            .orElse(null);

                    if (targetItem == null) {
                        continue;
                    }

                    userDataManager.getData().getInventory().remove(targetItem);
                }
            }
    	} catch (SQLException e) {
            e.printStackTrace();        
        }

        log.info("Removed item with the id of " + itemId + " from all the users.");
        message.reply(":grapes: Removed item with the id of " + itemId + " from all the users.").queue();

        Lemi.getInstance().shutdown();
    }

    private Connection getConnection() throws SQLException {
	return dataSource.getConnection();
    }
}