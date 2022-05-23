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

package com.honiism.discord.lemi.utils.embeds;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedTools {

    private Map<String, String> embedProperties = new HashMap<String, String>();
    private MessageReceivedEvent event;
    
    public void askForId(User author, TextChannel channel, MessageReceivedEvent event) {
        this.event = event;

        channel.sendMessage(":blossom: Alright! Type in the **ID** for this embed.\r\n"
                + "1. *Keep it simple, unique, and descriptive*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 20 characters (cannot have spaces)*\r\n"
                + "2. *You only have 2 minutes.*")
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive")
                            && e.getMessage().getContentRaw().length() < 20,
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            LemiDbEmbedManager.INS.assignUniqueId(channel, e.getMessage().getContentRaw(), embedProperties);

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingId(author, channel);
                            }
                            */

                            askForTitle(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForTitle(User author, TextChannel channel) {
        channel.sendMessage(":tulip: Alright! Type in the **title** for this embed.\r\n"
                + "1. *Keep it simple.*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 246 characters (including spaces)*\r\n"
                + "2. *You only have 2 minutes.*\r\n"
                + "**-----------**\r\n"
                + "*You cannot take more than 15 minutes!*\r\n")
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive")
                            && e.getMessage().getContentRaw().length() < 246,
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("title", null);
                            } else {
                                embedProperties.put("title", e.getMessage().getContentRaw());
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingTitle(author, channel);
                            }
                            */

                            askForColor(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForColor(User author, TextChannel channel) {
        channel.sendMessage(":crescent_moon: Alright! Type in the **color** for this embed.\r\n"
                + "1. *Please use HEX CODE (starts with #).*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setDescription("_ _")
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("color", "0xffd1dc");
                            } else if (e.getMessage().getContentRaw().startsWith("#")) {
                                int hexCode = Integer.decode(e.getMessage().getContentRaw().replace("#", "0x"));
                                embedProperties.put("color", String.valueOf(hexCode));
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingColor(author, channel);
                            }
                            */

                            askForAuthor(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForAuthor(User author, TextChannel channel) {
        channel.sendMessage(
                ":seedling: Alright! Type in the **author name & avatar** for this embed.\r\n"
                        + "1. *Please use this format:* `[name]&[link]` *or* `[name]`\r\n"
                        + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                        + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                        + "4. *Author name must be less than 250 characters.*\r\n*"
                        + "**-----------**\r\n"
                        + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("author-name", null);
                                embedProperties.put("author-avatar", null);
                            } else if (e.getMessage().getContentRaw().contains("&")) {
                                String[] authorValues = e.getMessage().getContentRaw().split("&");

                                embedProperties.put("author-name", authorValues[0].trim());
                                embedProperties.put("author-avatar", authorValues[1].trim());
                            } else {
                                embedProperties.put("author-name", e.getMessage().getContentRaw());
                            }

                            if (embedProperties.get("author-name").length() > 250) {
                                channel.sendMessage(":shell: Interaction cancelled! The author name pass the limit of 250 characters")
                                    .queue();
                                return;
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingAuthor(author, channel);
                            }
                            */

                            askForThumbnail(author, channel);
                        },
                2, TimeUnit.MINUTES,
                () -> {
                    channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
                );
            });
    }

    public void askForThumbnail(User author, TextChannel channel) {
        channel.sendMessage(":snowflake: Alright! Type in the **thumbnail** for this embed.\r\n"
                + "1. *Please use a valid link. You can also use* `%user_avatar%`.\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event),
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("thumbnail", null);
                            } else {
                                embedProperties.put("thumbnail", e.getMessage().getContentRaw());
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingThumbnail(author, channel);
                            }
                            */

                            askForDesc(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForDesc(User author, TextChannel channel) {
        channel.sendMessage(":grapes: Alright! Type in the **description** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 4,000 characters (including spaces)*\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), event))
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive")
                            && e.getMessage().getContentRaw().length() < 4000,
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("description", null);
                            } else {
                                embedProperties.put("description", e.getMessage().getContentRaw());
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingDesc(author, channel);
                            }
                            */

                            askForImage(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForImage(User author, TextChannel channel) {
        channel.sendMessage(":tulip: Alright! Type in the **image** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use a valid link or a placeholder.*\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), event))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), event))
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("image", null);
                            } else {
                                embedProperties.put("image", e.getMessage().getContentRaw());
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingImg(author, channel);
                            }
                            */

                            askForFooter(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForFooter(User author, TextChannel channel) {
        channel.sendMessage(":crescent_moon: Alright! Type in the **footer** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use this format:* `[text]&[link]` *or* `[text]`.\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*\r\n"
                + "4. *Text must be less than 2,000 characters (including spaces)*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), event))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), event))
                    .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), event))
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("footer-text", null);
                                embedProperties.put("footer-icon", null);
                            } else if (e.getMessage().getContentRaw().contains("&")) {
                                String[] footerValues = e.getMessage().getContentRaw().split("&");

                                embedProperties.put("footer-text", footerValues[0].trim());
                                embedProperties.put("footer-icon", footerValues[1].trim());
                            } else {
                                embedProperties.put("footer-text", e.getMessage().getContentRaw());
                            }

                            if (embedProperties.get("footer-text").length() > 2000) {
                                channel.sendMessage(":shell: Interaction cancelled! The footer text pass the limit of 2,000 characters")
                                    .queue();
                                return;
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingFooter(author, channel);
                            }
                            */

                            askForMessageContent(author, channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void askForMessageContent(User author, TextChannel channel) {
        channel.sendMessage(":seedling: Alright! Type in the **message content** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *This will appear outside the embed and show as a message.*\r\n"
                + "2. *You can type* `skip` *to continue.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), event))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), event))
                    .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), event))
                    .setFooter(embedProperties.get("footer-text") == null ? null : processField(embedProperties.get("footer-text"), event), 
                            embedProperties.get("footer-icon") == null ? null : processField(embedProperties.get("footer-icon"), event))
                    .build())
            .queue((msg) -> {
                Lemi.getInstance().getEventWaiter().waitForEvent(
                        MessageReceivedEvent.class,
                        (e) -> e.getAuthor().getIdLong() == author.getIdLong()
                            && e.isFromGuild()
                            && e.getGuild().getIdLong() == Config.getLong("honeys_hive"),
                        (e) -> {
                            if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                                channel.sendMessage(":sunflower: Interaction cancelled.").queue();
                                return;
                            }

                            if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedProperties.put("message-content", null);
                            } else {
                                embedProperties.put("message-content", e.getMessage().getContentRaw());
                            }

                            /*
                            if (embedListener != null) {
                                embedListener.afterAskingMessageContent(channel);
                            }
                            */

                            sendCreatedEmbed(channel);
                        },
                        2, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void sendCreatedEmbed(TextChannel channel) {
        channel.sendMessageEmbeds(createEmbed(event).build()).queue();
        LemiDbEmbedManager.INS.saveCreatedEmbed(channel, embedProperties);
    }

    private String processField(String field, MessageReceivedEvent event) {
        return Tools.processPlaceholders(field, event.getMember(), event.getGuild(), event.getTextChannel());
    }

    private EmbedBuilder createEmbed(MessageReceivedEvent event) {
        return new EmbedBuilder()
            .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), event))
            .setColor(Integer.decode(embedProperties.get("color")))
            .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), event), 
                    null,
                    embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), event))
            .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), event))
            .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), event))
            .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), event))
            .setFooter(embedProperties.get("footer-text") == null ? null : processField(embedProperties.get("footer-text"), event), 
                    embedProperties.get("footer-icon") == null ? null : processField(embedProperties.get("footer-icon"), event));
    }
}