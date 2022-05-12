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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

public class LemiDbEmbedDs implements LemiDbEmbedManager {
    
    private static final Logger log = LoggerFactory.getLogger(LemiDbEmbedDs.class);
    private final HikariDataSource dataSource;

    public LemiDbEmbedDs() {
        try {
            File lemiDBFile = new File("LemiEmbedDb.db");

            if (!lemiDBFile.exists()) {
                if (lemiDBFile.createNewFile()) {
                    log.info("Created a database file (LemiEmbedDb.db)");
                } else {
                    log.error("Failed to create a database file.");
                }
            } else {
                log.info("Connected to LemiEmbedDb.db database file.");
            }
        } catch (IOException e) {
            log.error("\r\nSomething unexpected went wrong while trying to "
                    + "connect / create LemiEmbedDb.db file\r\n"
                    + "Error : IOException\r\n"
                    + "\r\n");

            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:LemiEmbedDb.db");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(300000);

        dataSource = new HikariDataSource(config);

        try (Statement statement = getConnection().createStatement()) {
            // saved_embeds
            statement.execute("CREATE TABLE IF NOT EXISTS saved_embeds ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "embed_id VARCHAR(20) NOT NULL,"
                    + "message_content VARCHAR(20),"
                    + "embed_json VARCHAR(20) NOT NULL"
                    + ");"
            );
    
            log.info("saved_embeds table initialised");
                
        } catch (SQLException e) {
            log.error("\r\nSomething went wrong while trying to "
                    + "create / connect to database tables\r\n"
                    + "Error : SQLException" + "\r\n"
                    + "\r\n");
    
            e.printStackTrace();
        }
    }

    @Override
    public String getSavedMsgContent(String embedId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT message_content FROM saved_embeds WHERE embed_id = ?")) {

            selectStatement.setString(1, embedId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("message_content");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    @Override
    public EmbedBuilder getSavedEmbedBuilder(InteractionHook hook, String embedId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT embed_json FROM saved_embeds WHERE embed_id = ?")) {

            selectStatement.setString(1, embedId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    MessageEmbed savedEmbed = ((JDAImpl) hook.getJDA())
                            .getEntityBuilder().createMessageEmbed(DataObject.fromJson(rs.getString("embed_json")));

                    EmbedBuilder savedEmbedBuilder = new EmbedBuilder(savedEmbed);

                    return savedEmbedBuilder;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    @Override
    public void showSavedEmbed(GuildMemberJoinEvent event, TextChannel channel, String embedId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT message_content, embed_json FROM saved_embeds WHERE embed_id = ?")) {

            selectStatement.setString(1, embedId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    channel.sendMessage(":butterfly: There's no embed with that id in the database.").queue();
                    return;
                }

                MessageEmbed savedEmbed = ((JDAImpl) event.getJDA())
                        .getEntityBuilder().createMessageEmbed(DataObject.fromJson(
                                Tools.processPlaceholders(rs.getString("embed_json"),
                                event.getMember(),
                                event.getGuild(),
                                channel)));

                if (!rs.getString("message_content").equals("null")) {
                    channel.sendMessage(rs.getString("message_content"))
                        .setEmbeds(savedEmbed)
                        .queue();
                } else {
                    channel.sendMessageEmbeds(savedEmbed).queue();
                }
            }
        } catch (SQLException e) {
            Tools.reportError("show an embed from the database.", "SQLException", log, channel, e);
        }
    }
    
    @Override
    public void showSavedEmbed(InteractionHook hook, String embedId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT message_content, embed_json FROM saved_embeds WHERE embed_id = ?")) {

            selectStatement.setString(1, embedId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    hook.sendMessage(":butterfly: There's no embed with that id in the database.").queue();
                    return;
                }

                MessageEmbed savedEmbed = ((JDAImpl) hook.getJDA())
                    .getEntityBuilder().createMessageEmbed(DataObject.fromJson(
                            Tools.processPlaceholders(rs.getString("embed_json"),
                                    hook.getInteraction().getMember(),
                                    hook.getInteraction().getGuild(),
                                    hook.getInteraction().getTextChannel())));

                if (!rs.getString("message_content").equals("null")) {
                    hook.getInteraction().getTextChannel()
                        .sendMessage(rs.getString("message_content"))
                        .setEmbeds(savedEmbed)
                        .queue();
                } else {
                    hook.getInteraction().getTextChannel().sendMessageEmbeds(savedEmbed).queue();
                }
            }
        } catch (SQLException e) {
            Tools.reportError("show an embed from the database.", "SQLException", log, e);
        }
    }
    
    @Override
    public void showEmbedsList(InteractionHook hook) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT embed_id FROM saved_embeds")) {

            try (ResultSet rs = selectStatement.executeQuery()) {
                List<String> embedIds = new ArrayList<>();
                boolean found = false;

                while (rs.next()) {
                    embedIds.add(rs.getString("embed_id"));
                    found = true;
                }

                if (!found) {
                    hook.sendMessage(":butterfly: There's currently no embeds.").queue();
                    return;
                }

                Paginator.Builder builder = new Paginator.Builder(hook.getJDA())
                    .setEmbedDesc("‧₊੭ :bread: **EMBEDS LIST!** ♡ ⋆｡˚")
                    .setEventWaiter(Lemi.getInstance().getEventWaiter())
                    .setItemsPerPage(10)
                    .setItems(embedIds)
                    .useNumberedItems(true)
                    .useTimestamp(true)
                    .addAllowedUsers(hook.getInteraction().getUser().getIdLong())
                    .setColor(0xffd1dc)
                    .setTimeout(1, TimeUnit.MINUTES);

                int page = 1;

                hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                    .queue(message -> builder.build().paginate(message, page));
            }
        } catch (SQLException e) {
            Tools.reportError("show the embed list from the database.", "SQLException", log, hook, e);
        }
    }
    
    @Override
    public void deleteCustomEmbed(InteractionHook hook, String embedId) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT embed_id FROM saved_embeds WHERE embed_id = ?")) {
            
            selectStatement.setString(1, embedId);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (!rs.next()) {
                    hook.sendMessage(":butterfly: That embed doesn't exist in the database.").queue();
                    return;
                }
            }

            try (PreparedStatement deleteStatement =
    		    conn.prepareStatement("DELETE FROM saved_embeds WHERE embed_id = ?")) {
    		deleteStatement.setString(1, embedId);

        	long result = deleteStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":cherry_blossom: Successfully removed embed.").queue();

                } else {
                    hook.sendMessage(":grapes: Something went wrong while removing the embed.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.reportError("remove an embed from the database.", "SQLException", log, hook, e);
        }
    }
    
    @Override
    public void assignUniqueId(InteractionHook hook, String specialKey, Map<String, String> embedProperties) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT embed_id FROM saved_embeds WHERE embed_id = ?")) {
            selectStatement.setString(1, specialKey);

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    hook.sendMessage(":butterfly: That id already exists, operation cancelled.").queue();
                    return;
                }

                embedProperties.put("embed-id", specialKey);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveCreatedEmbed(InteractionHook hook, Map<String, String> embedProperties) {
        try (Connection conn = getConnection();
                PreparedStatement selectStatement =
                    conn.prepareStatement("SELECT embed_id FROM saved_embeds WHERE embed_id = ?")) {
            selectStatement.setString(1, embedProperties.get("embed-id"));

            try (ResultSet rs = selectStatement.executeQuery()) {
                if (rs.next()) {
                    hook.sendMessage(":butterfly: Found the same id while saving, operation cancelled.").queue();
                    return;
                }
            }

            JSONObject embedBuilderJson = new JSONObject();

            embedBuilderJson.put("type", "rich");

            if (embedProperties.get("title") != null) {
                embedBuilderJson.put("title", embedProperties.get("title"));
            }
            
            if (embedProperties.get("description") != null) {
                embedBuilderJson.put("description", embedProperties.get("description"));
            }
            
            embedBuilderJson.put("color", Integer.decode(embedProperties.get("color")));

            if (embedProperties.get("image") != null) {
                JSONObject imageItems = new JSONObject();
                
                imageItems.put("url", embedProperties.get("image"));
                embedBuilderJson.put("image", imageItems);
            }
            
            if (embedProperties.get("thumbnail") != null) {
                JSONObject thumbnailItems = new JSONObject();
                
                thumbnailItems.put("url", embedProperties.get("thumbnail"));
                embedBuilderJson.put("thumbnail", thumbnailItems);
            }

            if (embedProperties.get("author-name") != null) {
                JSONObject authorItems = new JSONObject();
                authorItems.put("name", embedProperties.get("author-name"));

                if (embedProperties.get("author-avatar") != null) {
                    authorItems.put("icon_url", embedProperties.get("author-avatar"));
                }

                embedBuilderJson.put("author", authorItems);
            }

            if (embedProperties.get("footer-text") != null) {
                JSONObject footerItems = new JSONObject();
                footerItems.put("text", embedProperties.get("footer-text"));

                if (embedProperties.get("footer-icon") != null) {
                    footerItems.put("icon_url", embedProperties.get("footer-icon"));
                }

                embedBuilderJson.put("footer", footerItems);
            }

            if (embedProperties.get("message-content") != null) {
                try (PreparedStatement insertStatement =
    		        conn.prepareStatement("INSERT INTO saved_embeds(embed_id, message_content, embed_json) VALUES(?, ?, ?)")) {
    	            String embedJson = embedBuilderJson.toString();

                    insertStatement.setString(1, embedProperties.get("embed-id"));
                    insertStatement.setString(2, embedProperties.get("message-content"));
                    insertStatement.setString(3, embedJson);

                    int result = insertStatement.executeUpdate();

                    if (result != 0) {
                        hook.sendMessage(":herb: Successfully registered embed with the id of : " 
                                + embedProperties.get("embed-id"))
                            .queue();

                    } else {
                        hook.sendMessage(":blueberries: Something went wrong while registering the embed.").queue();
                    }
    	        }
                return;
            }

            try (PreparedStatement insertStatement =
    		    conn.prepareStatement("INSERT INTO saved_embeds(embed_id, embed_json) VALUES(?, ?)")) {
                String embedJson = embedBuilderJson.toString();

    	        insertStatement.setString(1, embedProperties.get("embed-id"));
                insertStatement.setString(2, embedJson);

                int result = insertStatement.executeUpdate();

                if (result != 0) {
                    hook.sendMessage(":herb: Successfully registered embed with the id of : " 
                            + embedProperties.get("embed-id"))
                        .queue();

                } else {
                    hook.sendMessage(":blueberries: Something went wrong while registering the embed.").queue();
                }
    	    }
                
        } catch (SQLException e) {
            Tools.reportError("register an embed id to the database.", "SQLException", log, hook, e);
        }
    }

    private Connection getConnection() throws SQLException {
	return dataSource.getConnection();
    }
}