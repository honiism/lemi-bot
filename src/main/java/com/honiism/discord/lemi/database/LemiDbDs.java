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

package com.honiism.discord.lemi.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

@SuppressWarnings("unlikely-arg-type") // test
public class LemiDbDs implements LemiDbManager {
    
    private static final Logger log = LoggerFactory.getLogger(LemiDbDs.class);
    private HikariDataSource ds;

    public LemiDbDs() {
        try {
            File lemiDBFile = new File("LemiDB.db");

            if (!lemiDBFile.exists()) {
                if (lemiDBFile.createNewFile()) {
                    log.info("Created a database file (LemiDB.db)");
                } else {
                    log.error("Failed to create a database file.");
                }
            } else {
                log.info("Connected to LemiDB.db database file.");
            }
        } catch (IOException e) {
            log.error("\r\nSomething unexpected went wrong while trying to "
                    + "connect / create LemiDB.db file\r\n"
                    + "Error : IOException\r\n"
                    + "\r\n");

            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("jdbc:sqlite:LemiDB.db");
	config.setConnectionTestQuery("SELECT 1");
	config.addDataSourceProperty("cachePrepStmts", "true");
	config.addDataSourceProperty("prepStmtCacheSize", "250");
	config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
	
        ds = new HikariDataSource(config);

        try (Statement statement = getConnection().createStatement()) {
            // guild_settings
            statement.execute("CREATE TABLE IF NOT EXISTS guild_settings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "guild_id VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "balance_name VARCHAR(20) NOT NULL DEFAULT '<:coin_hs_nosteal:932137408282103818>'"
                    + ");"
            );
    
            log.info("guild_settings table initialised");
    
            // banned_users
            statement.execute("CREATE TABLE IF NOT EXISTS banned_users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "author_id VARCHAR(20) NOT NULL,"
                    + "user_id VARCHAR(20) NOT NULL,"
                    + "reason VARCHAR (255) NOT NULL"
                    + ");"
            );
    
            log.info("banned_users table initialised");
    
            // admin_mod_ids
            statement.execute("CREATE TABLE IF NOT EXISTS admin_mod_ids ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "staff_key VARCHAR(20) NOT NULL,"
                    + "admin_ids VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "mod_ids VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "twitch_mod_ids VARCHAR(20) NOT NULL DEFAULT '0'"
                    + ");"
            );
    
            log.info("admin_mod_ids table initialised");
                
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
    public List<String> getReasons(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> reasons = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT reason FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.editOriginal(":cherry_blossom: Fetching all the reasons...").queue();

                while (rs.next()) {
                    reasons.add(rs.getString("reason"));
                }

                hook.editOriginal(":grapes: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the reasons.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "Something went wrong while trying to "
                    + "fetch all the reasons.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "Something went wrong while trying to "
                        + "fetch all the reasons.\r\n"
                        + "Error : SQLException" + "\r\n"
                        + "\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return reasons;
    }

    @Override
    public List<String> getAuthorIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> authorIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT author_id FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.sendMessage(":cherry_blossom: Fetching all the author (admin) ids...").queue();

                while (rs.next()) {
                    authorIds.add(rs.getString("author_id"));
                }

                hook.editOriginal(":grapes: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the author (admin) ids.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "Something went wrong while trying to "
                    + "fetch all the author (admin) ids.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "Something went wrong while trying to "
                        + "fetch all the author (admin) ids.\r\n"
                        + "Error : SQLException" + "\r\n"
                        + "\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return authorIds;
    }
    
    @Override
    public List<String> getUserIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> userIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT user_id FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.editOriginal(":cherry_blossom: Fetching all the user ids...").queue();

                while (rs.next()) {
                    userIds.add(rs.getString("user_id"));
                }

                hook.editOriginal(":grapes: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the user ids.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "Something went wrong while trying to "
                    + "fetch all the user ids.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "Something went wrong while trying to "
                        + "fetch all the user ids.\r\n"
                        + "Error : SQLException" + "\r\n"
                        + "\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return userIds;
    }
    
    @Override
    public void addUserId(Member member, String reason, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT user_id FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    hook.sendMessage(":sunflower: That user is already banned.").queue();
                    return;
                }
            }

            try (PreparedStatement insertStatement =
    		    conn.prepareStatement("INSERT INTO banned_users(user_id, reason) VALUES(?, ?)")) {

    	        insertStatement.setLong(1, member.getIdLong());
                insertStatement.setString(2, reason);

                int result = insertStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":herb: Successfully registered id, they're now banned.").queue();

                    log.info(event.getUser().getAsMention() + " added an id to the banned_users database. (<@" + member.getIdLong() + ">)");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getUser().getAsMention() + " added an id to the banned_users database. (<@" + member.getIdLong() + ">)")
                        .queue();

                } else {
                    hook.sendMessage(":blueberries: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "register a user id to the database.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "register a user id to the database. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "register a user id to the database. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }
    }
    
    @Override
    public void removeUserId(Member member, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT user_id FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    hook.sendMessage(":butterfly: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    		    conn.prepareStatement("DELETE FROM banned_users WHERE user_id = ?")) {
    		deleteStatement.setLong(1, member.getIdLong());

        	long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":cherry_blossom: Successfully removed id.").queue();
                    
                    log.info(event.getUser().getAsMention() + " removed a banned user id. (<@" + member.getIdLong() + ">)");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getUser().getAsMention() + " removed a banned user id. (<@" + member.getIdLong() + ">)")
                        .queue();

                } else {
                    hook.sendMessage(":grapes: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "remove an id from the database.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "remove an id from the database. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "remove an id from the database. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        } 
    }
    
    @Override
    public List<String> getAdminIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> adminIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.sendMessage(":blueberries: Fetching all the admin ids...").queue();

                while (rs.next()) {
                    if (rs.getString("admin_ids").equals("0")) {
                        continue;
                    }
                    adminIds.add(rs.getString("admin_ids"));
                }

                hook.editOriginal(":grapes: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the admin ids.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "fetch all the admin ids. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "fetch all the admin ids. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return adminIds;
    }
    
    @Override
    public List<String> getAdminKeys(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> adminKeys = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT staff_key FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.editOriginal(":cherry_blossom: Fetching all the admin keys...").queue();

                while (rs.next()) {
                    if (!rs.getString("staff_key").endsWith("admin")) {
                        continue;
                    }
                    adminKeys.add(rs.getString("staff_key"));
                }

                hook.editOriginal(":sunflower: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the admin keys.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "fetch all the admin keys. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "fetch all the admin keys. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return adminKeys;
    }
    
    @Override
    public void removeAdminId(Guild guild, Member member, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    hook.sendMessage(":tulip: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    		    conn.prepareStatement("DELETE FROM admin_mod_ids WHERE admin_ids = ?")) {
    		deleteStatement.setLong(1, member.getIdLong());

        	long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":honey_pot: Successfully removed id.").queue();
                    
                    log.info(event.getUser().getAsMention() + " removed an admin id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getUser().getAsMention() + " removed an admin id. (" + member.getIdLong() + ")").queue();

                    if (member.getRoles().contains(guild.getRoleById(Config.get("admin_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("admin_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":cherry_blossom: Successfully removed admin role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                    if (member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":cherry_blossom: Successfully removed staff role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                } else {
                    hook.sendMessage(":leaves: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "remove an id from the database.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "remove an id from the database. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "remove an id from the database. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }
    }
    
    @Override
    public void addAdminId(Guild guild, Member member, String key, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectKeyStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE staff_key = ?")) {
            
            selectKeyStatement.setString(1, key);

            try (ResultSet rs = selectKeyStatement.executeQuery()) {
                if (rs.next()) {
                    hook.sendMessage(":umbrella: That key already exists.").queue();
                    return;
                }
            }

            try (PreparedStatement selectUserStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
                selectKeyStatement.setLong(1, member.getIdLong());

                try (ResultSet rs = selectKeyStatement.executeQuery()) {
                    if (rs.next()) {
                        hook.sendMessage(":mushroom: That id already exists.").queue();
                        return;
                    }
                }
            }

            try (PreparedStatement insertStatement =
    		    conn.prepareStatement("INSERT INTO admin_mod_ids(admin_ids, staff_key) VALUES(?, ?)")) {
    		insertStatement.setLong(1, member.getIdLong());
                insertStatement.setString(2, key);

        	int result = insertStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":croissant: Successfully registered id, they're now an admin.").queue();

                    log.info(event.getUser().getAsMention() + " added an admin id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getUser().getAsMention() + " added an admin id. (" + member.getIdLong() + ")").queue();

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("admin_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("admin_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":honey_pot: Successfully gave the admin role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":honey_pot: Successfully gave the staff role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                } else {
                    hook.sendMessage(":herb: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "register an admin id to the database.\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "register an admin id to the database. :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "register an admin id to the database. :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }
    }
    
    @Override
    public List<String> getModIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> modIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.sendMessage(":cherry_blossom: Fetching all the mod ids...").queue();

                while (rs.next()) {
                    if (rs.getString("mod_ids").equals("0")) {
                        continue;
                    }
                    modIds.add(rs.getString("mod_ids"));
                }

                hook.editOriginal(":grapes: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the mod ids.\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "Something went wrong while trying to "
                    + "fetch all the mod ids.\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "Something went wrong while trying to "
                        + "fetch all the mod ids.\r\n"
                        + " : commands.staff.developer.ModifyMods\r\n"
                        + "Error : SQLException" + "\r\n"
                        + "\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return modIds;
    }
    
    @Override
    public List<String> getModKeys(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> modKeys = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT staff_key FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                hook.editOriginal(":cherries: Fetching all the mod keys...").queue();

                while (rs.next()) {
                    if (!rs.getString("staff_key").endsWith("mod")) {
                        continue;
                    }
                    modKeys.add(rs.getString("staff_key"));
                }

                hook.editOriginal(":sunflower: Fetching complete.").queue();
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "fetch all the mod keys.\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "fetch all the mod keys. :no_entry:**\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "fetch all the mod keys. :no_entry:**\r\n"
                        + " : commands.staff.developer.ModifyMods\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }

        return modKeys;
    }
    
    @Override
    public void removeModId(Guild guild, Member member, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    hook.sendMessage(":butterfly: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    	            conn.prepareStatement("DELETE FROM admin_mod_ids WHERE mod_ids = ?")) {
    	        deleteStatement.setLong(1, member.getIdLong());

                long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":cherry_blossom: Successfully removed id.").queue();
                    
                    log.info(event.getUser().getAsMention() + " removed a mod id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                        .getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage(event.getUser().getAsMention() + " removed a mod id. (" + member.getIdLong() + ")").queue();

                    if (member.getRoles().contains(guild.getRoleById(Config.get("mod_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("mod_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":cherry_blossom: Successfully removed mod role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                    if (member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":cherry_blossom: Successfully removed staff role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }
                } else {
                    hook.sendMessage(":grapes: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "remove an id from the database.\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "remove an id from the database. :no_entry:**\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "remove an id from the database. :no_entry:**\r\n"
                        + " : commands.staff.developer.ModifyMods\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        }
    }
    
    @Override
    public void addModId(Guild guild, Member member, String key, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        try (Connection conn = getConnection();
                PreparedStatement selectKeyStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE staff_key = ?")) {
            
            selectKeyStatement.setString(1, key);

            try (ResultSet rs = selectKeyStatement.executeQuery()) {
                if (rs.next()) {
                    hook.sendMessage(":sunflower: That key already exists.").queue();
                    return;
                }
            }

            try (PreparedStatement selectUserStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
                selectKeyStatement.setLong(1, member.getIdLong());

                try (ResultSet rs = selectKeyStatement.executeQuery()) {
                    if (rs.next()) {
                        hook.sendMessage(":cake: That id already exists.").queue();
                        return;
                    }
                }
            }

            try (PreparedStatement insertStatement =
    		    conn.prepareStatement("INSERT INTO admin_mod_ids(mod_ids, staff_key) VALUES(?, ?)")) {
    		insertStatement.setLong(1, member.getIdLong());
                insertStatement.setString(2, key);

        	int result = insertStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":herb: Successfully registered id, they're now a mod.").queue();

                    log.info(event.getUser().getAsMention() + " added a mod id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getUser().getAsMention() + " added a mod id. (" + member.getIdLong() + ")").queue();

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("mod_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("mod_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":honey_pot: Successfully gave the mod role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    hook.editOriginal(":honey_pot: Successfully gave the staff role from them.").queue();
                                },
                                (error) -> {
                                    hook.editOriginal(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }
                } else {
                    hook.sendMessage(":blueberries: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "register a mod id to the database.\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");

            e.printStackTrace();

            hook.sendMessage("--------------------------\r\n" 
                    + "**Something went wrong while trying to "
                    + "register a mod id to the database. :no_entry:**\r\n"
                    + " : commands.staff.developer.ModifyMods\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + "Message : " + e.getMessage() + "\r\n"
                    + "Cause : " + e.getCause() + "\r\n"
                    + "```")
        	.queue();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	.getTextChannelById(Config.get("logs_channel_id"))
        	.sendMessage("--------------------------\r\n" 
                        + "**Something went wrong while trying to "
                        + "register a mod id to the database. :no_entry:**\r\n"
                        + " : commands.staff.developer.ModifyMods\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + "Message : " + e.getMessage() + "\r\n"
                        + "Cause : " + e.getCause() + "\r\n"
                        + "```")
        	.queue();
        } 
    }
    
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();

        if (guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    log.info("Successfully loaded members for Honey's Picnic server.");
                    
                    guild.getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage("Successfully loaded members for Honey's Picnic server.")
                        .queue();

                    CurrencyTools.onGuildReadyAddProf(guild);

                    try (Connection conn = getConnection();
                            PreparedStatement selectStatement =
                                conn.prepareStatement("SELECT guild_id FROM guild_settings WHERE guild_id = ?")) {

                        selectStatement.setString(1, Config.get("honeys_sweets_id"));
                        
                        try (ResultSet rs = selectStatement.executeQuery()) {
                            if (rs.next()) {
                                return;
                            }
                        }

                        try (PreparedStatement insertStatement =
    		                conn.prepareStatement("INSERT INTO guild_settings (guild_id) VALUES(?)")) {
                            insertStatement.setString(1, Config.get("honeys_sweets_id"));
                        
                            int result = insertStatement.executeUpdate();

                            if (result != 0) {
                                log.info("Successfully registered settings.");
                                
                                guild.getTextChannelById(Config.get("logs_channel_id"))
                                    .sendMessage("Successfully registered settings.")
                                    .queue();
                            } else {
                                log.info("Had problems while registering settings.");
                                
                                guild.getTextChannelById(Config.get("logs_channel_id"))
                                    .sendMessage("Had problems while registering settings.")
                                    .queue();
                            }
                        }
                        
                    } catch (SQLException e) {
                        
                    }
                })
                .onError((error) -> {
                    log.error("Failed to load members for Honey's Picnic server.", error);

                    guild.getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage("Failed to load members for Honey's Picnic server.\r\n"
                                + "--------------------------\r\n"
                                + "Message : " + error.getMessage() + "\r\n"
                                + "--------------------------\r\n"
                                + "Cause : " + error.getCause().getMessage() + "\r\n"
                                + "--------------------------\r\n"
                                + "Stack trace : " + error.getStackTrace().toString())
                        .queue();
                });
        }
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        InteractionHook hook = event.getHook();
        Guild guild = event.getGuild();
        Member member = event.getMember();
                    
        if (guild == null || member.getUser().isBot()) {
            return;
        }

        Long guildId = guild.getIdLong();

        if (!guildId.equals(Long.parseLong(Config.get("honeys_sweets_id")))
                && !guildId.equals(Long.parseLong(Config.get("test_server")))) {
            guild.leave().queue();
            return;
        }
            
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT * FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    String reason = rs.getString("reason");
            
                    hook.sendMessage("Sorry, you're banned from using Lemi for : " + reason).queue();
                    return;
                }

                if (event.getOptions().contains(guild.getMemberById(member.getIdLong()))) {
                    hook.sendMessage("Sorry, that person is banned from using Lemi.").queue();
                    return;
                }
            }

        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "run the banned user check for slash commands\r\n"
                    + "Error : SQLException\r\n"
                    + "\r\n");
                            
            e.printStackTrace();
            
            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                .getTextChannelById(Config.get("logs_channel_id"))
                .sendMessage("--------------------------\r\n"
                        + "**Something went wrong while trying to "
                        + "run the banned user check for slash commands :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + e.getMessage() + "\r\n"
                        + e.getCause() + "\r\n"
                        + "```")
                .queue();
        }
    }

    @Override
    public String getBalName(String guildId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                        conn.prepareStatement("SELECT balance_name FROM guild_settings WHERE guild_id = ?")) {
            selectStatement.setString(1, guildId);
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("balance_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean isAuthorAdmin(User author, TextChannel channel) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
                            
            selectStatement.setLong(1, author.getIdLong());
    
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next() || Tools.isAuthorDev(author)) {
                    return true;
                }
            }
    
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "run isAuthorAdmin\r\n"
                    + "Error : SQLException\r\n"
                    + "\r\n");
    
            e.printStackTrace();
    
            channel.sendMessage("--------------------------\r\n" 
                    +"\r\n**Something went wrong while trying to "
                    + "run isAuthorAdmin :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + e.getMessage() + "\r\n"
                    + e.getCause() + "\r\n"
                    + "```")
                .queue();
        }
    
        return false;
    }
    
    @Override
    public boolean isAuthorMod(User author, TextChannel channel) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
                
            selectStatement.setLong(1, author.getIdLong());
    
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next() || Tools.isAuthorDev(author) || isAuthorAdmin(author, channel)) {
                    return true;
                }
            }
    
            } catch (SQLException e) {
                log.error("Something went wrong while trying to"
                        + " run isAuthorMod\r\n"
                        + "Error : SQLException\r\n"
                        + "\r\n");
    
                e.printStackTrace();
    
                channel.sendMessage("--------------------------\r\n" 
                        +"\r\n**Something went wrong while trying to"
                        + " run isAuthorMod :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + e.getMessage() + "\r\n"
                        + e.getCause() + "\r\n"
                        + "```")
                    .queue();
            }
    
        return false;
    }
    
    @Override
    public boolean isAuthorAdmin(Member member, SlashCommandInteractionEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
                            
            selectStatement.setLong(1, member.getIdLong());
    
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next() || Tools.isAuthorDev(member)) {
                    return true;
                }
            }
    
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "run isAuthorAdmin\r\n"
                    + "Error : SQLException\r\n"
                    + "\r\n");
    
            e.printStackTrace();
    
            event.reply("--------------------------\r\n" 
                    +"\r\n**Something went wrong while trying to "
                    + "run isAuthorAdmin :no_entry:**\r\n"
                    + "Error : SQLException\r\n"
                    + "--------------------------\r\n"
                    + "```\r\n"
                    + e.getMessage() + "\r\n"
                    + e.getCause() + "\r\n"
                    + "```")
                .queue();
        }
    
        return false;
    }
    
    @Override
    public boolean isAuthorMod(Member member, SlashCommandInteractionEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
                
            selectStatement.setLong(1, member.getIdLong());
    
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next() || Tools.isAuthorDev(member) || isAuthorAdmin(member, event)) {
                    return true;
                }
            }
    
            } catch (SQLException e) {
                log.error("Something went wrong while trying to"
                        + " run isAuthorMod\r\n"
                        + "Error : SQLException\r\n"
                        + "\r\n");
    
                e.printStackTrace();
    
                event.reply("--------------------------\r\n" 
                        +"\r\n**Something went wrong while trying to"
                        + " run isAuthorMod :no_entry:**\r\n"
                        + "Error : SQLException\r\n"
                        + "--------------------------\r\n"
                        + "```\r\n"
                        + e.getMessage() + "\r\n"
                        + e.getCause() + "\r\n"
                        + "```")
                    .queue();
            }
    
        return false;
    }
}