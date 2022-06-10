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

package com.honiism.discord.lemi.commands.slash.fun;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.AutocompleteChoices;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
import com.honiism.discord.lemi.data.misc.QuestionData;
import com.honiism.discord.lemi.utils.buttons.ButtonMenu;
import com.honiism.discord.lemi.utils.buttons.Paginator;
import com.honiism.discord.lemi.utils.currency.WeightedRandom;
import com.honiism.discord.lemi.utils.misc.Emojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class TruthOrDare extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(TruthOrDare.class);

    private final String baseURL = "https://api.truthordarebot.xyz/v1/";

    public TruthOrDare() {
        setCommandData(Commands.slash("tod", "A group of ToD commands.")
                .addSubcommands(
                        new SubcommandData("truth", "Get a truth question (default: PG13).")
                                .addOption(OptionType.STRING, "rating", "The maturity rating.",
                                        false, true),

                        new SubcommandData("dare", "Get a dare question (default: PG13).")
                                .addOption(OptionType.STRING, "rating", "The maturity rating.",
                                        false, true),

                        new SubcommandData("wyr", "Get a Would You Rather question (default: PG13).")
                                .addOption(OptionType.STRING, "rating", "The maturity rating.",
                                        false, true),

                        new SubcommandData("nhie", "Get a Never Have I Ever question (default: PG13).")
                                .addOption(OptionType.STRING, "rating", "The maturity rating.",
                                        false, true),

                        new SubcommandData("paranoia", "Get a paranoia question (default: PG13).")
                                .addOption(OptionType.USER, "user", "User to ask.", false)
                                .addOption(OptionType.STRING, "rating", "The maturity rating.",
                                        false, true),

                        new SubcommandData("settings", "Modify settings for this command.")
                                .addOption(OptionType.STRING, "category", "The setting category.",
                                        true, true)
                )
        );

        setUsage("/tod ((subcommands))");
        setCategory(CommandCategory.FUN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setWhitelist(true);
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        String subCmdName = event.getSubcommandName();
        InteractionHook hook = event.getHook();

        String rating = event.getOption("rating", "PG13", OptionMapping::getAsString);

        if (!rating.equals("PG") && !rating.equals("PG13") && !rating.equals("R")) {
            hook.sendMessage(":snowflake: Invalid rating. Available ones: PG, PG13, R.").queue();
            return;
        }

        if (rating.equals("R") && !LemiDbManager.INS.isNSFWAllowed(hook)) {
            hook.sendMessage(":butterfly: NSFW rating is not allowed (modify it with `/tod settings setNSFW`).").queue();
            return;
        }

        JSONObject jsonResponse = handleRequest(rating, subCmdName, hook);
        Guild guild = event.getGuild();
        
        Member member = event.getMember();

        switch (subCmdName) {
            case "truth":
            case "dare":
            case "wyr":
            case "nhie":
                try {
                    String question = jsonResponse.getString("question");
                    String id = jsonResponse.getString("id");

                    EmbedBuilder questionEmbed = new EmbedBuilder()
                        .setTitle(":tulip: " + subCmdName.toUpperCase() + " question!")
                        .setDescription(question)
                        .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                        .setFooter("ID: " + id + " | Rating: " + rating)
                        .setThumbnail(guild.getSelfMember().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    hook.sendMessageEmbeds(questionEmbed.build()).queue();
                } catch (JSONException e) {
                    Tools.sendError("Get the question.", "JSONException", log, hook, e);
                }
                break;

            case "paranoia":
                try {
                    String question = jsonResponse.getString("question");
                    String id = jsonResponse.getString("id");

                    Member target = event.getOption("user", event.getMember(), OptionMapping::getAsMember);

                    EmbedBuilder questionEmbed = new EmbedBuilder()
                        .setTitle(":tulip: " + subCmdName.toUpperCase() + " question!")
                        .setDescription(question)
                        .setAuthor(target.getEffectiveName(), null, target.getEffectiveAvatarUrl())
                        .setFooter("ID: " + id + " | Rating: " + rating + " | Type: " + subCmdName)
                        .setThumbnail(guild.getSelfMember().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    target.getUser().openPrivateChannel().queue(
                        (channel) -> {
                            hook.sendMessage(":strawberry: Sent them the question.").queue();

                            channel.sendMessage(":grapes: Please respond with an answer in 5 minutes.")
                                .setEmbeds(questionEmbed.build())
                                .queue((msg) -> {
                                    Lemi.getInstance().getEventWaiter().waitForEvent(
                                            MessageReceivedEvent.class,
                                            (e) -> e.isFromType(ChannelType.PRIVATE)
                                                && e.getAuthor().getIdLong() == target.getIdLong()
                                                && !e.getAuthor().isBot(),
                                            (e) -> {
                                                TextChannel textChannel = hook.getInteraction().getTextChannel();

                                                WeightedRandom<Boolean> isShown = new WeightedRandom<>();
                                                int shownRate = LemiDbManager.INS.getParanoiaRate(hook);

                                                isShown.add(shownRate, true);
                                                isShown.add(100 - shownRate, false);
                                                
                                                EmbedBuilder answerEmbed = new EmbedBuilder()
                                                    .setTitle(":strawberry: Answer Received!")
                                                    .addField(":sunflower: Question",
                                                            (isShown.next()) ? question : "Hidden!", false)
                                                    .addField(":seedling: Answer", e.getMessage().getContentRaw(), false)
                                                    .setAuthor(target.getEffectiveName(), null, target.getEffectiveAvatarUrl())
                                                    .setFooter("ID: " + id + " | Rating: " + rating + " | Type: " + subCmdName)
                                                    .setThumbnail(guild.getSelfMember().getEffectiveAvatarUrl())
                                                    .setColor(0xffd1dc);

                                                textChannel.sendMessage(":cherry_blossom: " + target.getAsMention() + " has responded!")
                                                    .setEmbeds(answerEmbed.build())
                                                    .queue((message) -> {
                                                        channel.sendMessage("Answer sent.").queue();
                                                    });
                                            },
                                            5,
                                            TimeUnit.MINUTES,
                                            () -> channel.sendMessage("You ran out of time to answer").queue()
                                    );
                                });
                        },
                        (error) -> {
                            hook.sendMessage(":grapes: Cannot dm that user.").queue();
                        }
                    );
                } catch (JSONException e) {
                    Tools.sendError("Get the question.", "JSONException", log, hook, e);
                }
                break;

            case "settings":
                if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                    hook.sendMessage(":grapes: Sorry only people with the ADMINISTRATOR permission can run this.")
                        .queue();
                    return;
                }

                String settingCategory = event.getOption("category", OptionMapping::getAsString);

                if (!settingCategory.equals("NSFW_rating") && !settingCategory.equals("paranoia_rate")) {
                    hook.sendMessage(":cherries: Invalid setting category.").queue();
                    return;
                }

                if (settingCategory.equals("NSFW_rating")) {
                    hook.sendMessage(":sunflower: Please respond with either `true` or `false` to `allow` or `not allow`.")
                        .queue((msg) -> {
                            Lemi.getInstance().getEventWaiter().waitForEvent(
                                    MessageReceivedEvent.class,
                                    (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getMember().getIdLong()
                                        && e.isFromGuild()
                                        && e.getGuild().getIdLong() == hook.getInteraction().getGuild().getIdLong()
                                        && e.getTextChannel().getIdLong() == hook.getInteraction().getTextChannel().getIdLong()
                                        && (e.getMessage().getContentRaw().equals("true") 
                                        || e.getMessage().getContentRaw().equals("false")),
                                    (e) -> {
                                        String raw = e.getMessage().getContentRaw();
                                        LemiDbManager.INS.setNSFWRating(Boolean.parseBoolean(raw), hook);
                                    },
                                    1,
                                    TimeUnit.MINUTES,
                                    () -> hook.sendMessage(":tea: You ran out of time.").queue()
                            );
                        });
                        
                } else if (settingCategory.equals("paranoia_rate")) {
                    hook.sendMessage(":sunflower: Please respond with a number out of 1 - 100.")
                        .queue((msg) -> {
                            Lemi.getInstance().getEventWaiter().waitForEvent(
                                    MessageReceivedEvent.class,
                                    (e) -> e.getAuthor().getIdLong() == hook.getInteraction().getMember().getIdLong()
                                        && e.isFromGuild()
                                        && e.getGuild().getIdLong() == hook.getInteraction().getGuild().getIdLong()
                                        && e.getTextChannel().getIdLong() == hook.getInteraction().getTextChannel().getIdLong()
                                        && Tools.isInt(e.getMessage().getContentRaw())
                                        && Integer.parseInt(e.getMessage().getContentRaw()) <= 100,
                                    (e) -> {
                                        String raw = e.getMessage().getContentRaw();
                                        LemiDbManager.INS.setParanoiaRate(Integer.parseInt(raw), hook);
                                    },
                                    1,
                                    TimeUnit.MINUTES,
                                    () -> hook.sendMessage(":tea: You ran out of time.").queue()
                            );
                        });

                } else if (settingCategory.equals("custom_question")) {
                    EmbedBuilder guideEmbed = new EmbedBuilder()
                        .setTitle(":strawberry: Custom Question Menu!")
                        .setDescription(":warning: **READ BEFORE CLICKING!**\r\n"
                                + "> :sunflower: You can obtain question ids by clicking the view button!\r\n"
                                + "> This id will be needed to delete any custom questions you've added before.")
                        .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                        .setThumbnail(guild.getSelfMember().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    Modal.Builder addModal = Modal.create("add_custom_question", "Add a custom question!");
                    Modal.Builder deleteModal = Modal.create("delete_custom_question", "Remove a custom question!");

                    Runnable addRun = () -> {
                        TextInput type = TextInput.create("add_question_type", "Question Type", TextInputStyle.SHORT)
                            .setPlaceholder("The type of question it is (Truth, Dare, WYR, NHIE, Paranoia).")
                            .setMaxLength(8)
                            .setRequired(true)
                            .build();

                        TextInput questionRating = TextInput.create("add_question_rating", "Question Rating",
                                TextInputStyle.SHORT)
                            .setPlaceholder("The maturarity rate of question it is (PG, PG13, R).")
                            .setMaxLength(4)
                            .setRequired(true)
                            .build();

                        TextInput question = TextInput.create("add_question", "Question", TextInputStyle.SHORT)
                            .setPlaceholder("Enter the custom question you'd like to add.")
                            .setMaxLength(250)
                            .setRequired(true)
                            .build();

                        addModal.addActionRows(ActionRow.of(type), ActionRow.of(questionRating), ActionRow.of(question));
                    };

                    Runnable deleteRun = () -> {
                        TextInput questionId = TextInput.create("question_id", "Question ID", TextInputStyle.SHORT)
                            .setPlaceholder("The id of the question you'd like to delete (can be seen through view).")
                            .setMaxLength(8)
                            .setRequired(true)
                            .build();

                        deleteModal.addActionRows(ActionRow.of(questionId));
                    };

                    Runnable viewRun = () -> {
                        List<String> items = new ArrayList<>();

                        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                            .setEmbedDesc("‧₊੭ :bread: **CUSTOM QUESTIONS LIST!** ♡ ⋆｡˚")
                            .setEventWaiter(Lemi.getInstance().getEventWaiter())
                            .setItemsPerPage(10)
                            .setItems()
                            .useNumberedItems(true)
                            .useTimestamp(true)
                            .addAllowedUsers(event.getAuthor().getIdLong())
                            .setColor(0xffd1dc)
                            .setTimeout(1, TimeUnit.MINUTES);

                        int page = 1;

                        event.getMessage().replyEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                            .queue(message -> builder.build().paginate(message, page));
                    };

                    Button addButton = Button.success("question_add", "Add")
                        .withEmoji(Emoji.fromMarkdown(Emojis.PLUS_SIGN));

                    Button viewButton = Button.secondary("question_view", "View")
                        .withEmoji(Emoji.fromMarkdown(Emojis.LIST));

                    Button deleteButton = Button.danger("question_delete", "Delete")
                        .withEmoji(Emoji.fromMarkdown(Emojis.TRASH_BIN));

                    ButtonMenu.Builder menuBuilder = new ButtonMenu.Builder(hook.getJDA())
                        .addAllowedUsers(member.getIdLong())
                        .setTimeout(1, TimeUnit.MINUTES);

                    hook.sendMessageEmbeds(guideEmbed.build()).queue(
                        (msg) -> {
                            
                        }
                    );
                }
        }
    }

    @Override
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
        List<AutocompleteChoices> choices = new ArrayList<>();

        if (event.getFocusedOption().getName().equals("rating")) {
            choices.add(new AutocompleteChoices("PG", "PG"));
            choices.add(new AutocompleteChoices("PG13", "PG13"));
            choices.add(new AutocompleteChoices("R", "R"));

        } else if (event.getFocusedOption().getName().equals("category")) {
            choices.add(new AutocompleteChoices("NSFW rating", "NSFW_rating"));
            choices.add(new AutocompleteChoices("Paranoia rate", "paranoia_rate"));
            choices.add(new AutocompleteChoices("Custom question", "custom_question"));
        }

        event.replyChoices(
                choices.stream().map(AutocompleteChoices::toCommandAutocompleteChoice).toList()
        ).queue();
    }

    private JSONObject handleRequest(String rating, String reqType, InteractionHook hook) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            URI uri = new URIBuilder(baseURL + reqType)
                .addParameter("rating", rating)
                .build();

            HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .setHeader("User-Agent", Config.get("user_agent"))
                .header("Content-Type", Config.get("content_type"))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return new JSONObject(response.body());
        } catch (JSONException | IOException | InterruptedException | URISyntaxException e) {
            Tools.reportError("Trying to send the request", "JSON/IO/Interrupted/URISyntax Exception", log, e);
        }

        return null;
    }
}