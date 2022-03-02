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

package com.honiism.discord.lemi.utils.customEmbeds;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class EmbedTools {

    private static IEmbedListener embedListener;
    private static Map<String, String> embedProperties = new HashMap<String, String>();
  
    public void registerEmbedListener(IEmbedListener embedListener) {
        EmbedTools.embedListener = embedListener;
    }
    
    public void askForId(InteractionHook hook) {
        hook.sendMessage(":blossom: Alright! Type in the **ID** for this embed.\r\n"
                + "1. *Keep it simple, unique, and descriptive*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 20 characters (cannot have spaces)*\r\n"
                + "2. *You only have 2 minutes.*")
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id"))
                    && e.getMessage().getContentRaw().length() < 20,
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    LemiDbEmbedManager.INS.assignUniqueId(hook, e.getMessage().getContentRaw(), embedProperties);

                    if (embedListener != null) {
                        embedListener.afterAskingId(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForTitle(InteractionHook hook) {
        hook.editOriginal(":tulip: Alright! Type in the **title** for this embed.\r\n"
                + "1. *Keep it simple.*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 246 characters (including spaces)*\r\n"
                + "2. *You only have 2 minutes.*\r\n"
                + "**-----------**\r\n"
                + "*You cannot take more than 15 minutes!*\r\n")
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id"))
                    && e.getMessage().getContentRaw().length() < 246,
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("title", null);
                    } else {
                        embedProperties.put("title", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingTitle(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForColor(InteractionHook hook) {
        hook.editOriginal(":crescent_moon: Alright! Type in the **color** for this embed.\r\n"
                + "1. *Please use HEX CODE (starts with #).*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setDescription("_ _")
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("color", "0xffd1dc");
                    } else if (e.getMessage().getContentRaw().startsWith("#")) {
                        int hexCode = Integer.decode(e.getMessage().getContentRaw().replace("#", "0x"));
                        embedProperties.put("color", String.valueOf(hexCode));
                    }

                    e.getMessage().delete().queue();

                    if (embedListener != null) {
                        embedListener.afterAskingColor(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForAuthor(InteractionHook hook) {
        hook.editOriginal(
                ":seedling: Alright! Type in the **author name & avatar** for this embed.\r\n"
                        + "1. *Please use this format:* `[name]&[link]` *or* `[name]`*\r\n"
                        + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                        + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                        + "4. *Author name must be less than 250 characters.*\r\n*"
                        + "**-----------**\r\n"
                        + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
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

                    e.getMessage().delete().queue();

                    if (embedProperties.get("author-name").length() < 250) {
                        hook.editOriginal(":shell: Interaction cancelled! The author name pass the limit of 250 characters")
                            .queue();
                        return;
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingAuthor(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForThumbnail(InteractionHook hook) {
        hook.editOriginal(":snowflake: Alright! Type in the **thumbnail** for this embed.\r\n"
                + "1. *Please use a valid link. You can also use* `%user_avatar%`.\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook),
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("thumbnail", null);
                    } else {
                        embedProperties.put("thumbnail", e.getMessage().getContentRaw());
                    }

                    e.getMessage().delete().queue();

                    if (embedListener != null) {
                        embedListener.afterAskingThumbnail(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForDesc(InteractionHook hook) {
        hook.editOriginal(":grapes: Alright! Type in the **description** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 4,000 characters (including spaces)*\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id"))
                    && e.getMessage().getContentRaw().length() < 4000,
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("description", null);
                    } else {
                        embedProperties.put("description", e.getMessage().getContentRaw());
                    }

                    e.getMessage().delete().queue();

                    if (embedListener != null) {
                        embedListener.afterAskingDesc(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForImage(InteractionHook hook) {
        hook.editOriginal(":tulip: Alright! Type in the **image** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use a valid link or a placeholder.*\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("image", null);
                    } else {
                        embedProperties.put("image", e.getMessage().getContentRaw());
                    }

                    e.getMessage().delete().queue();

                    if (embedListener != null) {
                        embedListener.afterAskingImg(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForFooter(InteractionHook hook) {
        hook.editOriginal(":crescent_moon: Alright! Type in the **footer** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use this format:* `[text]&[link]` *or* `[text]`.\r\n"
                + "2. *You can type* `skip` *to continue to the next category.*\r\n"
                + "3. *You only have 2 minutes.*\r\n"
                + "4. *Text must be less than 2,000 characters (including spaces)*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), hook))
                    .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
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

                    e.getMessage().delete().queue();

                    if (embedProperties.get("footer-text").length() < 2000) {
                        hook.editOriginal(":shell: Interaction cancelled! The footer text pass the limit of 2,000 characters")
                            .queue();
                        return;
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingFooter(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForMessageContent(InteractionHook hook) {
        hook.editOriginal(":seedling: Alright! Type in the **message content** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *This will appear outside the embed and show as a message.*\r\n"
                + "2. *You can type* `skip` *to continue.*\r\n"
                + "3. *You only have 2 minutes.*")
            .setEmbeds(new EmbedBuilder()
                    .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
                    .setColor(Integer.decode(embedProperties.get("color")))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), hook))
                    .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), hook))
                    .setFooter(embedProperties.get("footer-text") == null ? null : processField(embedProperties.get("footer-text"), hook), 
                            embedProperties.get("footer-icon") == null ? null : processField(embedProperties.get("footer-icon"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.editOriginal(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("message-content", null);
                    } else {
                        embedProperties.put("message-content", e.getMessage().getContentRaw());
                    }

                    e.getMessage().delete().queue();

                    if (embedListener != null) {
                        embedListener.afterAskingMessageContent(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    private String processField(String field, InteractionHook hook) {
        return Tools.processPlaceholders(field,
                hook.getInteraction().getMember(),
                hook.getInteraction().getGuild(),
                hook.getInteraction().getTextChannel()
        );
    }

    private EmbedBuilder createEmbed(InteractionHook hook) {
        return new EmbedBuilder()
            .setTitle(embedProperties.get("title") == null ? null : processField(embedProperties.get("title"), hook))
            .setColor(Integer.decode(embedProperties.get("color")))
            .setAuthor(embedProperties.get("author-name") == null ? null : processField(embedProperties.get("author-name"), hook), 
                    null,
                    embedProperties.get("author-avatar") == null ? null : processField(embedProperties.get("author-avatar"), hook))
            .setThumbnail(embedProperties.get("thumbnail") == null ? null : processField(embedProperties.get("thumbnail"), hook))
            .setDescription(embedProperties.get("description") == null ? null : processField(embedProperties.get("description"), hook))
            .setImage(embedProperties.get("image") == null ? null : processField(embedProperties.get("image"), hook))
            .setFooter(embedProperties.get("footer-text") == null ? null : processField(embedProperties.get("footer-text"), hook), 
                    embedProperties.get("footer-icon") == null ? null : processField(embedProperties.get("footer-icon"), hook));
    }

    public void sendCreatedEmbed(InteractionHook hook) {
        hook.editOriginalEmbeds(createEmbed(hook).build()).queue();
        LemiDbEmbedManager.INS.saveCreatedEmbed(hook, embedProperties);
    }
}