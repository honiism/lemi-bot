/*
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
import java.util.List;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class LemiDbBalDs implements LemiDbBalManager {

    private static final Logger log = LoggerFactory.getLogger(LemiDbBalDs.class);
    private HikariDataSource ds;

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

        config.setJdbcUrl("jdbc:sqlite:LemiBalDb.db");
	config.setConnectionTestQuery("SELECT 1");
	config.addDataSourceProperty("cachePrepStmts", "true");
	config.addDataSourceProperty("prepStmtCacheSize", "250");
	config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
	
        ds = new HikariDataSource(config);

        try (Statement statement = getConnection().createStatement()) {
            // user_balance
            statement.execute("CREATE TABLE IF NOT EXISTS user_balance ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "wallet VARCHAR(20) NOT NULL DEFAULT '1000'"
                    + ");"
            );
    
            log.info("user_balance table initialised");   
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "create / connect to database tables\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");
    
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
	return ds.getConnection();
    }

    @Override
    public void addItemsToDb() {
        try (Statement statement = getConnection().createStatement()) {
            List<String> queries = new ArrayList<String>();

            for (Items item : CurrencyTools.getItems()) {
                queries.add(item.getId() + " INTEGER NOT NULL DEFAULT '0'");
            }

            String query = "CREATE TABLE IF NOT EXISTS user_inv ("
                    + "user_id VARCHAR(20) NOT NULL DEFAULT '0',"
                    + String.join(",", queries)
                    + ");";

            statement.execute(query);

            log.info("user_inv table initialised");
            log.info("added items to database.");
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "create / connect to database tables\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");
    
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean userHasCurrProfile(Member member) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT user_id FROM user_balance WHERE user_id = ?")) {
            selectStatement.setLong(1, member.getIdLong());

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
    public void addUserCurrProfile(Member member) {
        try (Connection conn = getConnection();
                PreparedStatement insertStatement =
                        conn.prepareStatement("INSERT INTO user_balance(user_id) VALUES(?)")) {
            insertStatement.setLong(1, member.getIdLong());
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void addUserInvProfile(Member member) {
        try (Connection conn = getConnection();
                PreparedStatement insertStatement =
                        conn.prepareStatement("INSERT INTO user_inv(user_id) VALUES(?)")) {
            insertStatement.setLong(1, member.getIdLong());
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getUserBal(String userId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT wallet FROM user_balance WHERE user_id = ?")) {
            selectStatement.setString(1, userId);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("wallet");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    @Override
    public void addBalToUser(String userId, long balanceToAdd) {
        long userBal = getUserBal(userId);
        long balAfterAdd = userBal + balanceToAdd;

        updateUserBal(userId, balAfterAdd);
    }

    @Override
    public void removeBalFromUser(String userId, long balanceToRemove) {
        long userBal = getUserBal(userId);
        long balAfterRemove = userBal - balanceToRemove;

        updateUserBal(userId, balAfterRemove);
    }
    
    @Override
    public void updateUserBal(String userId, long balanceToUpdate) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("UPDATE user_balance SET wallet = ? WHERE user_id = ?")) {
    	    updateStatement.setLong(1, balanceToUpdate);
            updateStatement.setString(2, userId);
            updateStatement.executeUpdate();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }
    }

    @Override
    public List<String> getOwnedItems(String userId) {
        List<String> ownedItems = new ArrayList<>();

        for (Items item : CurrencyTools.getItems()) {
            try (Connection conn = getConnection();
                    PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT * FROM user_inv WHERE user_id = ?")) {

                selectStatement.setString(1, userId);

                try (ResultSet rs = selectStatement.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getLong(item.getId()) == 0
                                || rs.getLong(item.getId()) < 0) {
                            continue;
                        }

                        ownedItems.add(item.getEmoji() + " " + item.getName() + " : " + rs.getLong(item.getId()));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }  
        }

        return ownedItems;
    }

    @Override
    public long getItemFromUserInv(String userId, String itemName) {
        String itemId = itemName.replaceAll(" ", "_");
        
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT " + itemId + " FROM user_inv WHERE user_id = ?")) {
            selectStatement.setString(1, userId);
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(itemId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    @Override
    public boolean checkIfItemExists(String itemName) {
        String itemId = itemName.replaceAll(" ", "_");

        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT " + itemId + " FROM user_inv")) {
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.info("ignored exception from checkIfItemExists");
        }
        
        return false;
    }

    @Override
    public void addItemToUser(String userId, String itemName, long amountToAdd) {
        long userItemAmount = getItemFromUserInv(userId, itemName);
        long itemAfterAdd = userItemAmount + amountToAdd;

        updateItemUser(userId, itemName, itemAfterAdd);
    }

    @Override
    public void updateItemUser(String userId, String itemName, long amountToUpdate) {
        String itemId = itemName.replaceAll(" ", "_");

        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("UPDATE user_inv SET " + itemId + " = ? WHERE user_id = ?")) {
    	    updateStatement.setLong(1, amountToUpdate);
            updateStatement.setString(2, userId);
            updateStatement.executeUpdate();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }
    }

    @Override
    public void removeItemFromUser(String userId, String itemName, long amountToRemove) {
        long userItemAmount = getItemFromUserInv(userId, itemName);
        long itemAfterRemove = userItemAmount - amountToRemove;
        
        updateItemUser(userId, itemName, itemAfterRemove);
    }

    @Override
    public void removeAllItems(String userId, Guild guild) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("DELETE FROM user_inv WHERE user_id = " + userId)) {
            updateStatement.executeUpdate();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }

        if (guild.getMemberById(userId) == null) {
            guild.retrieveMemberById(userId)
                .queue(
                    (member) -> {
                        CurrencyTools.addUserInvProfile(member);
                    },
                    (empty) -> {}
                );
        } else {
            CurrencyTools.addUserInvProfile(guild.getMemberById(userId));
        }
    }

    @Override
    public void removeCurrData(String userId, Guild guild) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("DELETE FROM user_balance WHERE user_id = " + userId)) {
            updateStatement.executeUpdate();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }

        if (guild.getMemberById(userId) == null) {
            guild.retrieveMemberById(userId)
                .queue(
                    (member) -> {
                        CurrencyTools.addUserCurrProfile(member);
                    },
                    (empty) -> {}
                );
        } else {
            CurrencyTools.addUserCurrProfile(guild.getMemberById(userId));
        }
    }

    @Override
    public void addNewItemToDb(String itemId, InteractionHook hook) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("ALTER TABLE user_inv ADD COLUMN " + itemId + " INTEGER NOT NULL DEFAULT '0'")) {
            updateStatement.execute();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }

        log.info("Altered table to add " + itemId + " to the database, shutting down.");
        hook.sendMessage(":seedling: Altered table to add " + itemId + " to the database, shutting down.").queue();
        
        Lemi.getInstance().shutdown();
    }

    @Override
    public void removeItemFromDb(String itemId, InteractionHook hook) {
        try (Connection conn = getConnection();
                PreparedStatement updateStatement =
    	                conn.prepareStatement("ALTER TABLE user_inv DROP COLUMN " + itemId)) {
            updateStatement.execute();
    	} catch (SQLException e) {
            e.printStackTrace();        
        }

        log.info("Altered table to remove " + itemId + " from the database, shutting down.");
        hook.sendMessage(":grapes: Altered table to remove " + itemId + " from the database, shutting down.").queue();

        Lemi.getInstance().shutdown();
    }
}