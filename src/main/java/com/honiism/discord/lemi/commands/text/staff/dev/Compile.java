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

package com.honiism.discord.lemi.commands.text.staff.dev;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Compile extends TextCmd {

    private static final Logger log = LoggerFactory.getLogger(Compile.class);
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Compile() {
        setName("compile");
        setDesc("Compile code using Lemi's core system.");
        setUsage("compile <language> <version_index> [input]");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
    }

    @Override
    public void action(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
        User author = event.getAuthor();

        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            List<String> args = ctx.getArgs();

            if (args.isEmpty() || args.size() < 2) {
                event.getMessage().replyEmbeds(getHelp(event)).content(":milk: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            if (!Tools.isInt(args.get(1))) {
                event.getMessage().reply(":sunflower: `<version_index>` must be a valid number.").queue();
                return;
            }

            event.getMessage().reply(":coconut: Please type in your code in 1 minute!").queue((msg) -> {
                String language = args.get(0);
                String versionIndex = args.get(1);

                if (args.size() > 2) {
                    String inputOption = args.get(2);

                    waitAndCompile(Lemi.getInstance().getEventWaiter(), event, inputOption, language, versionIndex);
                } else {
                    waitAndCompile(Lemi.getInstance().getEventWaiter(), event, null, language, versionIndex);   
                }
            });

        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            event.getMessage().replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }
    }

    private void waitAndCompile(EventWaiter waiter, MessageReceivedEvent event, String input, String language, String verIndex) {
        Member member = event.getMember();

        waiter.waitForEvent(
                MessageReceivedEvent.class,

                (e) -> e.getAuthor().getIdLong() == member.getIdLong()
                        && e.isFromGuild()
                        && e.getGuild().getIdLong() == event.getGuild().getIdLong()
                        && !e.getAuthor().isBot(),

                (e) -> {
                    e.getMessage().reply("Your code is being compiled, please wait... :coffee:").queue((msg) -> {
                        String script = e.getMessage().getContentRaw().replaceAll("```", "");
                    
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

                            EmbedBuilder resultEmbed = new EmbedBuilder()
                                .setTitle("‧₊੭ :tulip: Here is your code output!\r\n"
                                        + "๑‧˚₊꒷꒦︶︶︶︶︶꒷꒦︶︶︶︶︶✦‧₊˚⊹")
                                .setAuthor(member.getUser().getAsTag(), null, member.getUser().getEffectiveAvatarUrl())
                                .setColor(0xffd1dc)
                                .addField("❥ **REQUESTED BY** :sunflower:", member.getAsMention(), false)
                                .addField("❥ **STATUS CODE** :seedling:", String.valueOf(statusCode) , false)
                                .setThumbnail(e.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
                                .setDescription("```" + output + "```");

                            msg.editMessage("Here's your result! :cherries:").setEmbeds(resultEmbed.build()).queue();

                        } catch (IOException | InterruptedException ex) {
                            Tools.sendEditError("get the code output", "IOException", log, msg, ex);
                        }
                    });
                },
                1L, TimeUnit.MINUTES,
                () -> {
                    event.getMessage().reply("Operation cancelled due to timeout! :cloud:").queue();
                }
        );
    }
}