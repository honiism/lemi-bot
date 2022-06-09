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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.utils.URIBuilder;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.AutocompleteChoices;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
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

public class TruthOrDare extends SlashCmd {

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
                                        false, true)
                )
        );

        setUsage("/tod ((subcommands))");
        setCategory(CommandCategory.FUN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
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

        JSONObject jsonRespose = handleRequest(rating, subCmdName, hook);
        
        switch (subCmdName) {
            case "truth":
            case "dare":
            case "wyr":
            case "nhie":
                try {
                    String question = jsonRespose.getString("question");
                    String id = jsonRespose.getString("id");

                    Member member = hook.getInteraction().getMember();

                    EmbedBuilder questionEmbed = new EmbedBuilder()
                        .setTitle(":tulip: " + subCmdName.toUpperCase() + " question!")
                        .setDescription(question)
                        .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                        .setFooter("ID: " + id + " | Rating: " + rating)
                        .setThumbnail(hook.getInteraction().getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    hook.sendMessageEmbeds(questionEmbed.build()).queue();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case "paranoia":
                try {
                    String question = jsonRespose.getString("question");
                    String id = jsonRespose.getString("id");

                    Member member = event.getOption("user", event.getMember(), OptionMapping::getAsMember);

                    EmbedBuilder questionEmbed = new EmbedBuilder()
                        .setTitle(":tulip: " + subCmdName.toUpperCase() + " question!")
                        .setDescription(question)
                        .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                        .setFooter("ID: " + id + " | Rating: " + rating + " | Type: " + subCmdName)
                        .setThumbnail(hook.getInteraction().getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    member.getUser().openPrivateChannel().queue(
                        (channel) -> {
                            hook.sendMessage(":strawberry: Sent them the question.").queue();

                            channel.sendMessage(":grapes: Please respond with an answer in 5 minutes.")
                                .setEmbeds(questionEmbed.build())
                                .queue((msg) -> {
                                    Lemi.getInstance().getEventWaiter().waitForEvent(
                                            MessageReceivedEvent.class,
                                            (e) -> e.isFromType(ChannelType.PRIVATE)
                                                && e.getAuthor().getIdLong() == member.getIdLong()
                                                && !e.getAuthor().isBot(),
                                            (e) -> {
                                                TextChannel textChannel = hook.getInteraction().getTextChannel();
                                                
                                                EmbedBuilder answerEmbed = new EmbedBuilder()
                                                    .setTitle(":strawberry: Answer Received!")
                                                    .addField(":sunflower: Question", question, false)
                                                    .addField(":seedling: Answer", e.getMessage().getContentDisplay(), false)
                                                    .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                                                    .setFooter("ID: " + id + " | Rating: " + rating + " | Type: " + subCmdName)
                                                    .setThumbnail(hook.getInteraction().getGuild().getSelfMember().getEffectiveAvatarUrl())
                                                    .setColor(0xffd1dc);

                                                textChannel.sendMessage(member.getAsMention() + "has responded!")
                                                    .setEmbeds(answerEmbed.build())
                                                    .queue((message) -> {
                                                        channel.sendMessage("Answer sent.").queue();
                                                    });
                                            },
                                            5,
                                            TimeUnit.MINUTES,
                                            null
                                    );
                                });
                        },
                        (error) -> {
                            hook.sendMessage(":grapes: Cannot dm that user.").queue();
                        }
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
        List<AutocompleteChoices> choices = List.of(
                new AutocompleteChoices("PG", "PG"),
                new AutocompleteChoices("PG13", "PG13"),
                new AutocompleteChoices("R", "R")
        );

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
            e.printStackTrace();
        }

        return null;
    }
}