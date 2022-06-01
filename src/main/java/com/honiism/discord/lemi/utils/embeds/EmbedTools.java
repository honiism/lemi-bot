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

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.data.embed.EmbedData;
import com.honiism.discord.lemi.data.embed.FooterData;
import com.honiism.discord.lemi.data.embed.ImageData;
import com.honiism.discord.lemi.data.embed.ThumbnailData;
import com.honiism.discord.lemi.data.embed.AuthorData;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedTools {

    private EmbedData embedData = new EmbedData("rich");
    private MessageReceivedEvent event;
    
    public void askForId(User author, TextChannel channel, MessageReceivedEvent event) {
        this.event = event;

        channel.sendMessage(":blossom: Alright! Type in the **ID** for this embed.\r\n"
                + "1. *Keep it simple, unique, and descriptive*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 20 characters (cannot have spaces)*\r\n"
                + "2. *You only have 5 minutes.*")
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

                            LemiDbEmbedManager.INS.assignUniqueId(channel, e.getMessage().getContentRaw(), embedData);

                            askForTitle(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                + "2. *You only have 5 minutes.*\r\n")
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

                            if (!e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedData.setTitle(e.getMessage().getContentRaw());
                            }

                            askForColor(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                + "1. *You only have 5 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
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
                                embedData.setColor("0xffd1dc");
                            } else if (e.getMessage().getContentRaw().startsWith("#")) {
                                int hexCode = Integer.decode(e.getMessage().getContentRaw().replace("#", "0x"));
                                embedData.setColor(String.valueOf(hexCode));
                            }

                            askForAuthor(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                        + "4. *Author name must be less than 250 characters.*\r\n"
                        + "**-----------**\r\n"
                        + "1. *You only have 5 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
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

                            if (e.getMessage().getContentRaw().contains("&")) {
                                String[] authorValues = e.getMessage().getContentRaw().split("&");
                                
                                AuthorData authorData = new AuthorData(authorValues[0].trim());

                                authorData.setURL(authorValues[1].trim());

                                embedData.setAuthor(authorData);
                            } else {
                                AuthorData authorData = new AuthorData(e.getMessage().getContentRaw());
                                embedData.setAuthor(authorData);
                            }

                            if (embedData.getAuthor().getName().length() > 250) {
                                channel.sendMessage(":shell: Interaction cancelled! "
                                        + "The author name pass the limit of 250 characters")
                                    .queue();
                                return;
                            }

                            askForThumbnail(author, channel);
                        },
                5, TimeUnit.MINUTES,
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
                + "1. *You only have 10 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
                    .setDescription("_ _")
                    .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event),
                            null,
                            embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
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

                            if (!e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                ThumbnailData thumbnailData = new ThumbnailData(e.getMessage().getContentRaw());
                                embedData.setThumbnail(thumbnailData);
                            }

                            askForDesc(author, channel);
                        },
                        10, TimeUnit.MINUTES,
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
                + "3. *You only have 5 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
                    .setDescription("_ _")
                    .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event), 
                            null,
                            embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
                    .setThumbnail(embedData.getThumbnail().getURL() == null ? null : processField(embedData.getThumbnail().getURL(), event))
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

                            if (!e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedData.setDesc(e.getMessage().getContentRaw());
                            }

                            askForImage(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                + "3. *You only have 5 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
                    .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event), 
                            null,
                            embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
                    .setThumbnail(embedData.getThumbnail().getURL() == null ? null : processField(embedData.getThumbnail().getURL(), event))
                    .setDescription(embedData.getDesc() == null ? null : processField(embedData.getDesc(), event))
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

                            if (!e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                ImageData imageData = new ImageData(e.getMessage().getContentRaw());
                                embedData.setImage(imageData);
                            }

                            askForFooter(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                + "3. *You only have 5 minutes.*\r\n"
                + "4. *Text must be less than 2,000 characters (including spaces)*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
                    .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event), 
                            null,
                            embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
                    .setThumbnail(embedData.getThumbnail().getURL() == null ? null : processField(embedData.getThumbnail().getURL(), event))
                    .setDescription(embedData.getDesc() == null ? null : processField(embedData.getDesc(), event))
                    .setImage(embedData.getImage().getURL() == null ? null : processField(embedData.getImage().getURL(), event))
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

                            if (e.getMessage().getContentRaw().contains("&")) {
                                String[] footerValues = e.getMessage().getContentRaw().split("&");

                                FooterData footerData = new FooterData(footerValues[0].trim());

                                footerData.setURL(footerValues[1].trim());

                                embedData.setFooter(footerData);
                            } else {
                                FooterData footerData = new FooterData(e.getMessage().getContentRaw());
                                embedData.setFooter(footerData);
                            }

                            if (embedData.getFooter().getText().length() > 2000) {
                                channel.sendMessage(":shell: Interaction cancelled! "
                                        + "The footer text pass the limit of 2,000 characters")
                                    .queue();
                                return;
                            }

                            askForMessageContent(author, channel);
                        },
                        5, TimeUnit.MINUTES,
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
                + "3. *You only have 5 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
                    .setColor(embedData.getColor())
                    .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event), 
                            null,
                            embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
                    .setThumbnail(embedData.getThumbnail().getURL() == null ? null : processField(embedData.getThumbnail().getURL(), event))
                    .setDescription(embedData.getDesc() == null ? null : processField(embedData.getDesc(), event))
                    .setImage(embedData.getImage().getURL() == null ? null : processField(embedData.getImage().getURL(), event))
                    .setFooter(embedData.getFooter().getText() == null ? null : processField(embedData.getFooter().getText(), event), 
                            embedData.getFooter().getURL() == null ? null : processField(embedData.getFooter().getURL(), event))
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

                            if (!e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                                embedData.setContent(e.getMessage().getContentRaw());
                            }

                            sendCreatedEmbed(channel);
                        },
                        5, TimeUnit.MINUTES,
                        () -> {
                            channel.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                            return;
                        }
                );
            });
    }

    public void sendCreatedEmbed(TextChannel channel) {
        channel.sendMessageEmbeds(createEmbed(event).build()).queue();
        
        try {
            LemiDbEmbedManager.INS.saveCreatedEmbed(channel, embedData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String processField(String field, MessageReceivedEvent event) {
        return Tools.processPlaceholders(field, event.getMember(), event.getGuild(), event.getTextChannel());
    }

    private EmbedBuilder createEmbed(MessageReceivedEvent event) {
        return new EmbedBuilder()
            .setTitle(embedData.getTitle() == null ? null : processField(embedData.getTitle(), event))
            .setColor(embedData.getColor())
            .setAuthor(embedData.getAuthor().getName() == null ? null : processField(embedData.getAuthor().getName(), event), 
                    null,
                    embedData.getAuthor().getURL() == null ? null : processField(embedData.getAuthor().getURL(), event))
            .setThumbnail(embedData.getThumbnail().getURL() == null ? null : processField(embedData.getThumbnail().getURL(), event))
            .setDescription(embedData.getDesc() == null ? null : processField(embedData.getDesc(), event))
            .setImage(embedData.getImage().getURL() == null ? null : processField(embedData.getImage().getURL(), event))
            .setFooter(embedData.getFooter().getText() == null ? null : processField(embedData.getFooter().getText(), event), 
                    embedData.getFooter().getURL() == null ? null : processField(embedData.getFooter().getURL(), event));
    }
}