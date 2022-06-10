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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
import com.honiism.discord.lemi.data.misc.CusQuestionData;
import com.honiism.discord.lemi.data.misc.QuestionData;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

@SuppressWarnings("unlikely-arg-type") // test
public class LemiDbDs implements LemiDbManager {
    
    private static final Logger log = LoggerFactory.getLogger(LemiDbDs.class);
    private final HikariDataSource dataSource;

    public LemiDbDs() {
        try {
            File lemiDBFile = new File("LemiDb.db");

            if (!lemiDBFile.exists()) {
                if (lemiDBFile.createNewFile()) {
                    log.info("Created a database file (LemiDb.db)");
                } else {
                    log.error("Failed to create a database file.");
                }
            } else {
                log.info("Connected to LemiDb.db database file.");
            }
        } catch (IOException e) {
            log.error("\r\nSomething unexpected went wrong while trying to "
                    + "connect / create LemiDb.db file\r\n"
                    + "Error : IOException\r\n"
                    + "\r\n");

            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(Config.get("lemi_db_url"));
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(300000);

        dataSource = new HikariDataSource(config);

        try (Statement statement = getConnection().createStatement()) {
            // guild_settings
            statement.execute("CREATE TABLE IF NOT EXISTS guild_settings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "guild_id VARCHAR(20) NOT NULL,"
                    + "dj_role_id VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "allow_nsfw_rating VARCHAR(20) NOT NULL DEFAULT '0',"
                    + "paranoia_rate VARCHAR(20) NOT NULL DEFAULT '50',"
                    + "custom_questions_json VARCHAR(20) NOT NULL DEFAULT '0'"
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

    @Override
    public boolean hasQuestionData(long guildId) {
        String sql = "SELECT custom_questions_json FROM guild_settings WHERE guild_id = ?";
        
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, guildId);

            try (ResultSet rs = ps.executeQuery()) {
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
    public void addQuestionData(long guildId) {
        String sql = "UPDATE guild_settings SET custom_questions_json = ? WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            CusQuestionData questionData = new CusQuestionData(guildId);

            questionData.setQuestions(new ArrayList<QuestionData>());

            String jsonData = Lemi.getInstance().getObjectMapper().writeValueAsString(questionData);
            
            ps.setString(1, jsonData);
            ps.setLong(2, guildId);
            ps.executeUpdate();
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateQuestionData(long guildId, String jsonData) {
        String sql = "UPDATE guild_settings SET custom_questions_json = ? WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, jsonData);
            ps.setLong(2, guildId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getQuestionData(long guildId) {
        String sql = "SELECT custom_questions_json FROM guild_settings WHERE guild_id = ?";

        try (PreparedStatement selectStatement = getConnection().prepareStatement(sql)) {
            selectStatement.setLong(1, guildId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("custom_questions_json");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        addQuestionData(guildId);
        return getQuestionData(guildId);
    }

    @Override
    public void setParanoiaRate(int shownRate, InteractionHook hook) {
        String sql = "UPDATE guild_settings SET paranoia_rate = ? WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, shownRate);
            ps.setLong(2, hook.getInteraction().getGuild().getIdLong());

            int result = ps.executeUpdate();

            if (result != 0) {
                hook.sendMessage(":crescent_moon: Successfully set the paranoia shown rating to " + shownRate + "%.").queue();
            } else {
                hook.sendMessage(":tea: Failed to set the paranoia shown rating.").queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getParanoiaRate(InteractionHook hook) {
        String sql = "SELECT paranoia_rate FROM guild_settings WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, hook.getInteraction().getGuild().getIdLong());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("paranoia_rate");
                }
            }
        } catch (SQLException e) {
                Tools.reportError("Failed to get paranoia shown rate.", "SQLException", log, hook, e);
        }

        return 50;
    }

    @Override
    public void setNSFWRating(boolean input, InteractionHook hook) {
        String sql = "UPDATE guild_settings SET allow_nsfw_rating = ? WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, (input) ? "1" : "0");
            ps.setLong(2, hook.getInteraction().getGuild().getIdLong());

            int result = ps.executeUpdate();

            if (result != 0) {
                hook.sendMessage(":crescent_moon: Successfully set the NSFW rating to " + input + ".").queue();
            } else {
                hook.sendMessage(":tea: Failed to set the NSFW rating.").queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNSFWAllowed(InteractionHook hook) {
        String sql = "SELECT allow_nsfw_rating FROM guild_settings WHERE guild_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, hook.getInteraction().getGuild().getIdLong());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (rs.getInt("allow_nsfw_rating") == 1) ? true : false;
                }
            }
        } catch (SQLException e) {
            Tools.reportError("Failed to check if NSFW is allowed.", "SQLException", log, hook, e);
        }

        return false;
    }

    @Override
    public List<String> getBannedReasons(MessageReceivedEvent event) {
        List<String> reasons = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT reason FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    reasons.add(rs.getString("reason"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the reasons.", "SQLException", log, event.getMessage(), e);
        }

        return reasons;
    }

    @Override
    public List<Long> getBannerAuthorIds(MessageReceivedEvent event) {
        List<Long> authorIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT author_id FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    authorIds.add(rs.getLong("author_id"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the author (admin) ids.", "SQLException", log, event.getMessage(), e);
        }

        return authorIds;
    }
    
    @Override
    public List<Long> getBannedUserIds(MessageReceivedEvent event) {
        List<Long> userIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT user_id FROM banned_users")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("user_id"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the author user ids.", "SQLException", log, event.getMessage(), e);
        }

        return userIds;
    }
    
    @Override
    public void addBannedUserId(long targetId, String reason, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT user_id FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, targetId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    event.getMessage().reply(":sunflower: That user is already banned.").queue();
                    return;
                }
            }

            try (PreparedStatement insertStatement =
    		    conn.prepareStatement("INSERT INTO banned_users(author_id, user_id, reason) VALUES(?, ?, ?)")) {

                insertStatement.setLong(1, event.getMember().getIdLong());
    	        insertStatement.setLong(2, targetId);
                insertStatement.setString(3, reason);

                int result = insertStatement.executeUpdate();

                if (result != 0) {
                    event.getMessage().reply(":herb: Successfully registered id, they're now banned.").queue();

                    log.info(event.getAuthor().getAsMention() + " added an id to the banned_users database. (<@" + targetId + ">)");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getAuthor().getAsMention() + " added an id to the banned_users database. (<@" + targetId + ">)")
                        .queue();

                } else {
                    event.getMessage().reply(":blueberries: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.reportError("register a user to the database", "SQLException", log, event.getMessage(), e);
        }
    }
    
    @Override
    public void removeBannedUserId(long targetId, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT user_id FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, targetId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    event.getMessage().reply(":butterfly: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    		    conn.prepareStatement("DELETE FROM banned_users WHERE user_id = ?")) {
    		deleteStatement.setLong(1, targetId);

        	long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    event.getMessage().reply(":cherry_blossom: Successfully removed id.").queue();
                    
                    log.info(event.getAuthor().getAsMention() + " removed a banned user id. (<@" + targetId + ">)");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getAuthor().getAsMention() + " removed a banned user id. (<@" + targetId + ">)")
                        .queue();

                } else {
                    event.getMessage().reply(":grapes: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.reportError("remove an id from the database.", "SQLException", log, event.getMessage(), e);
        } 
    }
    
    @Override
    public List<Long> getAdminIds() {
        List<Long> adminIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    if (rs.getLong("admin_ids") == 0) {
                        continue;
                    }
                    adminIds.add(rs.getLong("admin_ids"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the admin ids.", "SQLException", log, e);
        }

        return adminIds;
    }
    
    @Override
    public List<String> getAdminKeys() {
        List<String> adminKeys = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT staff_key FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    if (!rs.getString("staff_key").contains("admin")) {
                        continue;
                    }
                    adminKeys.add(rs.getString("staff_key"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the admin keys.", "SQLException", log, e);
        }

        return adminKeys;
    }
    
    @Override
    public void removeAdminId(Guild guild, Member member, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    event.getMessage().reply(":tulip: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    		    conn.prepareStatement("DELETE FROM admin_mod_ids WHERE admin_ids = ?")) {
    		deleteStatement.setLong(1, member.getIdLong());

        	long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    event.getMessage().reply(":honey_pot: Successfully removed id.").queue();
                    
                    log.info(event.getAuthor().getAsMention() + " removed an admin id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getAuthor().getAsMention() + " removed an admin id. (" + member.getIdLong() + ")").queue();

                    if (member.getRoles().contains(guild.getRoleById(Config.get("admin_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("admin_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":cherry_blossom: Successfully removed admin role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                    if (member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":cherry_blossom: Successfully removed staff role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                } else {
                    event.getMessage().reply(":leaves: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.sendError("remove an admin from the database", "SQLException", log, event.getMessage(), e);
        }
    }
    
    @Override
    public void addAdminId(Guild guild, Member member, String key, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectKeyStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE staff_key = ?")) {
            
            selectKeyStatement.setString(1, key);

            try (ResultSet rs = selectKeyStatement.executeQuery()) {
                if (rs.next()) {
                    event.getMessage().reply(":umbrella: That key already exists.").queue();
                    return;
                }
            }

            try (PreparedStatement selectUserStatement =
                    conn.prepareStatement("SELECT admin_ids FROM admin_mod_ids WHERE admin_ids = ?")) {
                selectKeyStatement.setLong(1, member.getIdLong());

                try (ResultSet rs = selectKeyStatement.executeQuery()) {
                    if (rs.next()) {
                        event.getMessage().reply(":mushroom: That id already exists.").queue();
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
                    event.getMessage().reply(":croissant: Successfully registered id, they're now an admin.").queue();

                    log.info(event.getAuthor().getAsMention() + " added an admin id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getAuthor().getAsMention() + " added an admin id. (" + member.getIdLong() + ")").queue();

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("admin_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("admin_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":honey_pot: Successfully gave the admin role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":honey_pot: Successfully gave the staff role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                } else {
                    event.getMessage().reply(":herb: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.sendError("register an admin to the database", "SQLException", log, event.getMessage(), e);
        }
    }
    
    @Override
    public List<Long> getModIds() {
        List<Long> modIds = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    if (rs.getLong("mod_ids") == 0) {
                        continue;
                    }
                    modIds.add(rs.getLong("mod_ids"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the mods ids", "SQLException", log, e);
        }

        return modIds;
    }
    
    @Override
    public List<String> getModKeys() {
        List<String> modKeys = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement selectStatement = conn.prepareStatement("SELECT staff_key FROM admin_mod_ids")) {
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                while (rs.next()) {
                    if (!rs.getString("staff_key").contains("mod")) {
                        continue;
                    }
                    modKeys.add(rs.getString("staff_key"));
                }
            }

        } catch (SQLException e) {
            Tools.reportError("fetch all the mods keys.", "SQLException", log, e);
        }

        return modKeys;
    }
    
    @Override
    public void removeModId(Guild guild, Member member, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    event.getMessage().reply(":butterfly: That user doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    	            conn.prepareStatement("DELETE FROM admin_mod_ids WHERE mod_ids = ?")) {
    	        deleteStatement.setLong(1, member.getIdLong());

                long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    event.getMessage().reply(":cherry_blossom: Successfully removed id.").queue();
                    
                    log.info(event.getAuthor().getAsMention() + " removed a mod id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
                        .getTextChannelById(Config.get("logs_channel_id"))
                        .sendMessage(event.getAuthor().getAsMention() + " removed a mod id. (" + member.getIdLong() + ")").queue();

                    if (member.getRoles().contains(guild.getRoleById(Config.get("mod_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("mod_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":cherry_blossom: Successfully removed mod role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }

                    if (member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.removeRoleFromMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":cherry_blossom: Successfully removed staff role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while removing the role from them.").queue();
                                }
                            );
                    }
                } else {
                    event.getMessage().reply(":grapes: Something went wrong while removing the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.sendError("remove an id from the database.", "SQLException", log, event.getMessage(), e);
        }
    }
    
    @Override
    public void addModId(Guild guild, Member member, String key, MessageReceivedEvent event) {
        try (Connection conn = getConnection();
                PreparedStatement selectKeyStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE staff_key = ?")) {
            
            selectKeyStatement.setString(1, key);

            try (ResultSet rs = selectKeyStatement.executeQuery()) {
                if (rs.next()) {
                    event.getMessage().reply(":sunflower: That key already exists.").queue();
                    return;
                }
            }

            try (PreparedStatement selectUserStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
                selectKeyStatement.setLong(1, member.getIdLong());

                try (ResultSet rs = selectKeyStatement.executeQuery()) {
                    if (rs.next()) {
                        event.getMessage().reply(":cake: That id already exists.").queue();
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
                    event.getMessage().reply(":herb: Successfully registered id, they're now a mod.").queue();

                    log.info(event.getAuthor().getAsMention() + " added a mod id. (" + member.getIdLong() + ")");

                    Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
        	        .getTextChannelById(Config.get("logs_channel_id"))
        	        .sendMessage(event.getAuthor().getAsMention() + " added a mod id. (" + member.getIdLong() + ")").queue();

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("mod_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("mod_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":honey_pot: Successfully gave the mod role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }

                    if (!member.getRoles().contains(guild.getRoleById(Config.get("staff_role_id")))) {
                        guild.addRoleToMember(member, guild.getRoleById(Config.get("staff_role_id")))
                            .queue(
                                (success) -> {
                                    event.getMessage().reply(":honey_pot: Successfully gave the staff role from them.").queue();
                                },
                                (error) -> {
                                    event.getMessage().reply(":grapes: Something went wrong while giving the role from them.").queue();
                                }
                            );
                    }
                } else {
                    event.getMessage().reply(":blueberries: Something went wrong while registering the id.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.sendError("register a mod id to the database.", "SQLException", log, event.getMessage(), e);
        } 
    }
    
    @Override
    public void insertGuildSettings(Guild guild) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT guild_id FROM guild_settings WHERE guild_id = ?")) {

            selectStatement.setLong(1, guild.getIdLong());
                        
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }

            try (PreparedStatement insertStatement =
    	            conn.prepareStatement("INSERT INTO guild_settings (guild_id) VALUES(?)")) {
                insertStatement.setLong(1, guild.getIdLong());
                        
                int result = insertStatement.executeUpdate();

                if (result != 0) {
                    log.info("Successfully registered settings for " + guild.getName() + "(" + guild.getIdLong() + ").");
                } else {
                    log.info("Had problems while registering settings for " + guild.getName() + "(" + guild.getIdLong() + ").");
                }
            }
                        
        } catch (SQLException e) {}
    }

    @Override
    public void checkIfBanned(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
            
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT reason FROM banned_users WHERE user_id = ?")) {
            
            selectStatement.setLong(1, member.getIdLong());
            
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    String reason = rs.getString("reason");
                    event.reply("Sorry, you're banned from using Lemi for : " + reason).queue();
                    return;
                }

                if (event.getOptions().contains(guild.getMemberById(member.getIdLong()))) {
                    event.reply("Sorry, that person is banned from using Lemi.").queue();
                    return;
                }
            }

        } catch (SQLException e) {
            Tools.reportError("Run the banned users check list.", "SQLException", log, e);
        }
    }

    @Override
    public boolean isAuthorAdmin(User author) {
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
            Tools.reportError("run isAuthorAdmin", "SQLException", log, e);
        }
    
        return false;
    }
    
    @Override
    public boolean isAuthorMod(User author) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT mod_ids FROM admin_mod_ids WHERE mod_ids = ?")) {
                
            selectStatement.setLong(1, author.getIdLong());
    
            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next() || Tools.isAuthorDev(author) || isAuthorAdmin(author)) {
                    return true;
                }
            }
    
            } catch (SQLException e) {
                Tools.reportError("run isAuthorMod", "SQLException", log, e);
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
            Tools.reportError("Run isAuthorAdmin", "SQLException", log, e);
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
                Tools.reportError("Run isAuthorMod", "SQLException", log, e);
            }
    
        return false;
    }

    private Connection getConnection() throws SQLException {
	return dataSource.getConnection();
    }
}