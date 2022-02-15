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

package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.EmbedPaginator;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Compile extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Compile.class);
    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Compile() {
        this.name = "compile";
        this.desc = "Compile code using Lemi's core system.";
        this.usage = "/dev compile [input] [true/false] [page number]";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.DEV;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.options = Arrays.asList(new OptionData(OptionType.STRING,
                                             "language",
                                             "Language you want to use to code.")
                                        .setRequired(true),

                                     new OptionData(OptionType.STRING,
                                             "version-index",
                                             "The version index of the language to use.")
                                        .setRequired(true),

                                     new OptionData(OptionType.STRING,
                                             "input",
                                             "Input for the code if needed for execution.")
                                         .setRequired(false),
                
                                     new OptionData(OptionType.BOOLEAN,
                                             "help",
                                             "Want a help guide for this command? (True = yes, false = no).")
                                         .setRequired(false),

                                     new OptionData(OptionType.INTEGER,
                                             "page",
                                             "The page number for the compile menu you want to see.")
                                         .setRequired(false)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User user = event.getUser();

        if (delay.containsKey(user.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(user.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {
            if (delay.containsKey(user.getIdLong())) {
                delay.remove(user.getIdLong());
            }
        
            delay.put(user.getIdLong(), System.currentTimeMillis());

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                EmbedBuilder supportedLangsEmbed = new EmbedBuilder()
                    .setTitle("**Languages Guide** ༊·˚”♡ᵎ꒱ˀˀ↷⋯ 》 :tulip:\r\n"
                            + "๑‧˚₊꒷꒦︶︶︶︶︶꒷꒦︶︶︶︶︶✦‧₊˚⊹")
                    .setDescription(":blossom: [Here]" 
                            + "(https://docs.jdoodle.com/compiler-api/compiler-api#what-languages-and-versions-are-supported) " 
                            + "is the full list of usable languages.")
                    .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
                    .setFooter("Have fun ! ꒱ੈ♡˳")
                    .setColor(0xffd1dc);
    
                List<EmbedBuilder> items = new ArrayList<>();
    
                items.add(this.getHelpBuilder(event));
                items.add(supportedLangsEmbed);
    
                EmbedPaginator.Builder builder = new EmbedPaginator.Builder(event.getJDA())
                    .setEventWaiter(Lemi.getInstance().getEventWaiter())
                    .setTimeout(1, TimeUnit.MINUTES)
                    .setItems(items)
                    .addAllowedUsers(event.getUser().getIdLong());
    
                int page = 1;
    
                if (event.getOption("page") != null) {
                    page = (int) event.getOption("page").getAsLong();
                }
    
                int finalPage = page;

                hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":cherry_blossom: Loading..."))
                    .queue(message -> builder.build().paginate(message, finalPage));

                return;
            }

            hook.sendMessage(":coconut: Please type in your code in 1 minute!").queue();

            OptionMapping inputOption = event.getOption("input");
            String versionIndex = event.getOption("version-index").getAsString();
            String language = event.getOption("language").getAsString();

            waitAndCompile(Lemi.getInstance().getEventWaiter(), hook,
                    (inputOption != null) ? inputOption.getAsString() : null,
                    language, versionIndex);
                    
        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void waitAndCompile(EventWaiter waiter, InteractionHook hook, String input, String language, String verIndex) {
        Member member = hook.getInteraction().getMember();

        waiter.waitForEvent(
                MessageReceivedEvent.class,

                (event) -> event.getAuthor().getIdLong() == member.getIdLong()
                        && event.isFromGuild()
                        && event.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),

                (event) -> {
                    hook.editOriginal("Your code is being compiled, please wait... :coffee:").queue();

                    String script = event.getMessage().getContentRaw().replaceAll("```", "");
                    
                    JSONObject bodyReq = new JSONObject()
                        .put("clientId", Config.get("jdoodle_client_id"))
                        .put("clientSecret", Config.get("jdoodle_client_secret"))
                        .put("script", script)
                        .put("language", language)
                        .put("versionIndex", verIndex);

                    if (input != null) {
                        bodyReq.put("stdin", input);
                    }

                    HttpClient httpClient = HttpClient.newBuilder()
                        .version(Version.HTTP_2)
                        .followRedirects(Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                    HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(bodyReq.toString()))
                        .uri(URI.create("https://api.jdoodle.com/v1/execute"))
                        .setHeader("User-Agent", Config.get("user_agent"))
                        .header("Content-Type", Config.get("content_type"))
                        .build();

                    try {
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        JSONObject jsonRespose = new JSONObject(response.body());
                        String output = jsonRespose.getString("output");
                        int statusCode = jsonRespose.getInt("statusCode");
                        long memory = jsonRespose.getLong("memory");
                        double cpuTime = jsonRespose.getDouble("cpuTime");

                        EmbedBuilder resultEmbed = new EmbedBuilder()
                            .setTitle("‧₊੭ :tulip: Here is your code output!\r\n"
                                + "๑‧˚₊꒷꒦︶︶︶︶︶꒷꒦︶︶︶︶︶✦‧₊˚⊹")
                            .setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                            .setColor(0xffd1dc)
                            .addField("❥ **REQUESTED BY** :sunflower:", member.getAsMention(), false)
                            .addField("❥ **STATUS CODE** :seedling:", String.valueOf(statusCode) , false)
                            .addField("❥ **CPU TIME** :snowflake:", String.valueOf(cpuTime) , false)
                            .addField("❥ **MEMORY** :grapes:", String.valueOf(memory) , false)
                            .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
                            .setDescription("```" + output + "```");

                        hook.editOriginal("Here's your result! :cherries:").setEmbeds(resultEmbed.build()).queue();

                    } catch (IOException | InterruptedException e) {
                        log.error("\r\nSomething went wrong while trying to "
                                + "get the output/input stream.\r\n"
                                + "Location : commands.staff.developer.Compile\r\n"
                                + "Error : IOException" + "\r\n"
                                + "\r\n");

                        e.printStackTrace();

                        hook.sendMessage("--------------------------\r\n" 
                                + "**Something went wrong while trying to "
                                + "get the output/input stream. :no_entry:**\r\n"
                                + "Location : commands.staff.developer.Compile\r\n"
                                + "Error : IOException\r\n"
                                + "--------------------------\r\n"
                                + "```\r\n"
                                + "Message : " + e.getMessage() + "\r\n"
                                + "Cause : " + e.getCause() + "\r\n"
                                + "```")
                            .queue();
                    }
                },
                1L, TimeUnit.MINUTES,
                () -> {
                    hook.editOriginal("Operation cancelled due to timeout! :cloud:").queue();
                }
        );
    }
}