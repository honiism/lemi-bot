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

package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.GroovyShell;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Eval extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Eval.class);

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;
    private GroovyShell engine = new GroovyShell();

    private List<String> imports =
            Arrays.asList(
                    "net.dv8tion.jda.core",
                    "net.dv8tion.jda.api.entities.impl",
                    "net.dv8tion.jda.api.managers",
                    "net.dv8tion.jda.api.entities",
                    "net.dv8tion.jda.api",
                    "net.dv8tion.jda.api.utils",
                    "net.dv8tion.jda.api.utils.data",
                    "net.dv8tion.jda.internal.requests",
                    "net.dv8tion.jda.api.requests",
                    "java.lang",
                    "java.io",
                    "java.math",
                    "java.util",
                    "java.util.concurrent",
                    "java.time",
                    "com.honiism.discord.lemi"
            );

    public Eval() {
        setCommandData(Commands.slash("eval", "Evaluates some code."));
        setUsage("/dev eval");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();

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

            engine.setProperty("event", event);
            engine.setProperty("hook", hook);
            engine.setProperty("guild", event.getGuild());
            engine.setProperty("author", event.getUser());
            engine.setProperty("member", event.getMember());
            engine.setProperty("channel", event.getChannel());
            engine.setProperty("jda", event.getJDA());
            engine.setProperty("api", event.getJDA());
            engine.setProperty("bot", event.getJDA().getSelfUser());
            engine.setProperty("selfuser", event.getJDA().getSelfUser());
            engine.setProperty("selfmember", event.getGuild().getSelfMember());
            engine.setProperty("log", log);

            hook.sendMessage(":rice_ball: Please send the code to evaluate within 1 minute!").queue();

            waitAndEval(Lemi.getInstance().getEventWaiter(), hook);

        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void waitAndEval(EventWaiter waiter, InteractionHook hook) {
        waiter.waitForEvent(
                MessageReceivedEvent.class,

                (event) -> event.getAuthor().getIdLong() == hook.getInteraction().getMember().getIdLong()
                        && event.isFromGuild()
                        && event.getGuild().getIdLong() == Long.parseLong(Config.get("honeys_sweets_id")),

                (event) -> {
                    hook.editOriginal("Your code is being evaluated, please wait... :coffee:").queue();

                    String evalString = event.getMessage().getContentRaw().replaceAll("```", "");

                    StringBuilder toEval = new StringBuilder();

                    imports.forEach(imp -> toEval.append("import " + imp + ".*;\n"));
                    toEval.append(evalString);

                    try {
                        Object output = engine.evaluate(toEval.toString());

                        hook.sendMessage(output == null ? ":cherry_blossom: Executed without error." : output.toString()).queue();
                    } catch (Exception e) {
                        log.error("\r\nSomething went wrong while trying to "
                                + "evaluate the code\r\n"
                                + "Message : " + e.getMessage() + "\r\n"
                                + "\r\n");

                        e.printStackTrace();

                        hook.sendMessage("--------------------------\r\n" 
                                + "**Something went wrong while trying to "
                                + "evaluate the code :no_entry:**\r\n"
                                + "--------------------------\r\n"
                                + "```\r\n"
                                + "Message : " + e.getMessage() + "\r\n"
                                + "Cause : " + e.getCause() + "\r\n"
                                + "```")
        	            .queue();
            
                        Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
        	            .getTextChannelById(Config.get("logs_channel_id"))
        	            .sendMessage("--------------------------\r\n" 
                                    + "**Something went wrong while trying to "
                                    + "evaluate the code :no_entry:**\r\n"
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