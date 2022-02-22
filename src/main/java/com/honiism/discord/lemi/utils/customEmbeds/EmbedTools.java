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

    private IEmbedListener embedListener;
    private Map<String, String> embedProperties = new HashMap<String, String>();
    private Map<String, Integer> embedColor = new HashMap<String, Integer>();
  
    public void registerEmbedListener(IEmbedListener embedListener) {
        this.embedListener = embedListener;
    }

    public String[] editableFields = new String[] {
            "embed-id", "author-name", "author-avatar", "title", "description",
            "color", "image", "thumbnail", "footer-text", "footer-icon",
            "timestamp", "message-content"
    };
    
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
        hook.sendMessage(":tulip: Alright! Type in the **title** for this embed.\r\n"
                + "1. *Keep it simple.*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 246 characters (including spaces)*\r\n"
                + "2. *You only have 2 minutes.*\r\n"
                + "3. *The image below can help you!*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png \r\n"
                + "**-----------**\r\n"
                + "*You cannot take more than 15 minutes!*\r\n")
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

    public void askForColor(InteractionHook hook) {
        hook.sendMessage(":crescent_moon: Alright! Type in the **color** for this embed.\r\n"
                + "1. *Please use HEX CODE (starts with #).*\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *The image below can help you!*\r\n"
                + "2. *You only have 2 minutes.*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
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
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedColor.put("color", 0xffd1dc);
                    } else if (e.getMessage().getContentRaw().startsWith("#")) {
                        embedColor.put("color", Integer.decode(e.getMessage().getContentRaw().replace("#", "0x")));
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingColor(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForAuthor(InteractionHook hook) {
        hook.sendMessage(
                ":seedling: Alright! Type in the **author name & avatar** for this embed.\r\n"
                        + "1. *Please use this format:* `[name]&[link]` *or* `[text]`*\r\n"
                        + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                        + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                        + "**-----------**\r\n"
                        + "1. *The image below can help you!*\r\n"
                        + "2. *You only have 2 minutes.*\r\n"
                        + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
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
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedColor.put("color", 0xffd1dc);
                    } else if (e.getMessage().getContentRaw().startsWith("#")) {
                        embedColor.put("color", Integer.decode(e.getMessage().getContentRaw().replace("#", "0x")));
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingColor(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForThumbnail(InteractionHook hook) {
        hook.sendMessage(":snowflake: Alright! Type in the **thumbnail** for this embed.\r\n"
                + "1. *Please use a valid link. You can also use* `%user_avatar%`.\r\n"
                + "2. *You can cancel this interaction by typing* `cancel`.\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "**-----------**\r\n"
                + "1. *The image below can help you!*\r\n"
                + "2. *You only have 2 minutes.*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook),
                            null,
                            embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("thumbnail", null);
                    } else {
                        embedProperties.put("thumbnail", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingThumbnail(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForDesc(InteractionHook hook) {
        hook.sendMessage(":grapes: Alright! Type in the **description** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to make your title less than 4,000 characters (including spaces)*\r\n"
                + "2. *The image below can help you!*\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "4. *You only have 2 minutes.*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
                    .setDescription("_ _")
                    .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processThumbnail(embedProperties.get("thumbnail"), hook))
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
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("description", null);
                    } else {
                        embedProperties.put("description", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingDesc(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForImage(InteractionHook hook) {
        hook.sendMessage(":tulip: Alright! Type in the **image** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use a valid link or a placeholder.*\r\n"
                + "2. *The image below can help you!*\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "4. *You only have 2 minutes.*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processThumbnail(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processDesc(embedProperties.get("description"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("image", null);
                    } else {
                        embedProperties.put("image", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingImg(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForFooter(InteractionHook hook) {
        hook.sendMessage(":crescent_moon: Alright! Type in the **footer** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *You need to use this format:* `[text]&[link]` *or* `[text]`.\r\n"
                + "2. *The image below can help you!*\r\n"
                + "3. *You can type* `skip` *to continue to the next category.*\r\n"
                + "4. *You only have 2 minutes.*\r\n"
                + "5. *Text must be less than 2,000 characters (including spaces)*\r\n"
                + "https://cdn.discordapp.com/attachments/814041366627090463/928103774915026944/embedGuide.png")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processThumbnail(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processDesc(embedProperties.get("description"), hook))
                    .setImage(embedProperties.get("image") == null ? null : processImg(embedProperties.get("image"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("footer-text", null);
                        embedProperties.put("footer-icon", null);
                    } else if (e.getMessage().getContentRaw().contains("&")) {
                        String[] footerValuesSplit = e.getMessage().getContentRaw().split("&");
                        embedProperties.put("footer-text", footerValuesSplit[0]);
                        embedProperties.put("footer-icon", footerValuesSplit[1]);
                    } else {
                        embedProperties.put("footer-text", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingFooter(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    public void askForMessageContent(InteractionHook hook) {
        hook.sendMessage(":seedling: Alright! Type in the **message content** for this embed.\r\n"
                + "**-----------**\r\n"
                + "1. *This will appear outside the embed and show as a message.*\r\n"
                + "2. *The image below can help you!*\r\n"
                + "3. *You can type* `skip` *to continue.*\r\n"
                + "4. *You only have 2 minutes.*")
            .addEmbeds(new EmbedBuilder()
                    .setTitle(processTitle(embedProperties.get("title"), hook))
                    .setColor(embedColor.get("color"))
                    .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook), 
                            null,
                            embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
                    .setThumbnail(embedProperties.get("thumbnail") == null ? null : processThumbnail(embedProperties.get("thumbnail"), hook))
                    .setDescription(embedProperties.get("description") == null ? null : processDesc(embedProperties.get("description"), hook))
                    .setImage(embedProperties.get("image") == null ? null : processImg(embedProperties.get("image"), hook))
                    .setFooter(embedProperties.get("footer-text") == null ? null : processFooterText(embedProperties.get("footer-text"), hook), 
                            embedProperties.get("footer-icon") == null ? null : processFooterIcon(embedProperties.get("footer-icon"), hook))
                    .build())
            .queue();

        Lemi.getInstance().getEventWaiter().waitForEvent(
                MessageReceivedEvent.class,
                (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getUser().getIdLong()
                    && e.isFromGuild()
                    && e.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id"))
                    && e.getMessage().getContentRaw().length() < 2000,
                (e) -> {
                    if (e.getMessage().getContentRaw().toLowerCase().equals("cancel")) {
                        hook.sendMessage(":sunflower: Interaction cancelled.").queue();
                        return;
                    }

                    if (e.getMessage().getContentRaw().toLowerCase().equals("skip")) {
                        embedProperties.put("message-content", null);
                    } else {
                        embedProperties.put("message-content", e.getMessage().getContentRaw());
                    }

                    if (embedListener != null) {
                        embedListener.afterAskingMessageContent(hook);
                    }
                },
                2, TimeUnit.MINUTES,
                () -> {
                    hook.sendMessage(":sunflower: Interaction cancelled due to inactivity.").queue();
                    return;
                }
        );
    }

    private String processTitle(String title, InteractionHook hook) {
        return Tools.processPlaceholders(title,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processAuthorName(String authorName, InteractionHook hook) {
        return Tools.processPlaceholders(authorName,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processAuthorAvatar(String authorAvatar, InteractionHook hook) {
        return Tools.processPlaceholders(authorAvatar,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processThumbnail(String thumbnail, InteractionHook hook) {
        return Tools.processPlaceholders(thumbnail,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processDesc(String desc, InteractionHook hook) {
        return Tools.processPlaceholders(desc,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processImg(String img, InteractionHook hook) {
        return Tools.processPlaceholders(img,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processFooterText(String footerText, InteractionHook hook) {
        return Tools.processPlaceholders(footerText,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processFooterIcon(String footerIcon, InteractionHook hook) {
        return Tools.processPlaceholders(footerIcon,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private String processMessageContent(String messageContent, InteractionHook hook) {
        return Tools.processPlaceholders(messageContent,
            hook.getInteraction().getMember(),
            hook.getInteraction().getGuild(),
            hook.getInteraction().getTextChannel()
        );
    }

    private EmbedBuilder createEmbed(InteractionHook hook) {
        return new EmbedBuilder()
            .setTitle(processTitle(embedProperties.get("title"), hook))
            .setColor(embedColor.get("color"))
            .setAuthor(embedProperties.get("author-name") == null ? null : processAuthorName(embedProperties.get("author-name"), hook), 
                    null,
                    embedProperties.get("author-avatar") == null ? null : processAuthorAvatar(embedProperties.get("author-avatar"), hook))
            .setThumbnail(embedProperties.get("thumbnail") == null ? null : processThumbnail(embedProperties.get("thumbnail"), hook))
            .setDescription(embedProperties.get("description") == null ? null : processDesc(embedProperties.get("description"), hook))
            .setImage(embedProperties.get("image") == null ? null : processImg(embedProperties.get("image"), hook))
            .setFooter(embedProperties.get("footer-text") == null ? null : processFooterText(embedProperties.get("footer-text"), hook), 
                    embedProperties.get("footer-icon") == null ? null : processFooterIcon(embedProperties.get("footer-icon"), hook));
    }

    public void sendCreatedEmbed(InteractionHook hook) {
        if (processMessageContent(embedProperties.get("message-content"), hook) == null) {
            hook.sendMessageEmbeds(createEmbed(hook).build())
                .queue((msg) -> {
                    LemiDbEmbedManager.INS.saveCreatedEmbed(hook, embedProperties.get("embed-id"), embedProperties, embedColor);
                });
        } else {
            hook.sendMessage(processMessageContent(embedProperties.get("message-content"), hook))
                .addEmbeds(createEmbed(hook).build())
                .queue((msg) -> {
                    LemiDbEmbedManager.INS.saveCreatedEmbed(hook, processMessageContent(embedProperties.get("message-content"), hook),
                            embedProperties.get("embed-id"), embedProperties, embedColor);
                });
        }
    }
}