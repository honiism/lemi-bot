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

package com.honiism.discord.lemi.commands.slash.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class Report extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Report() {
        setCommandData(Commands.slash("report", "Report a bug by using the forms (ONLY FOR LEMI RELATED ISSUES)."));
        setUsage("/report");
        setCategory(CommandCategory.MAIN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        User user = event.getUser();

        if (delay.containsKey(user.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(user.getIdLong());
        } else {
            timeDelayed = (600 * 1000);
        }
            
        if (timeDelayed >= (600 * 1000)) {
            if (delay.containsKey(user.getIdLong())) {
                delay.remove(user.getIdLong());
            }
        
            delay.put(user.getIdLong(), System.currentTimeMillis());
            
            TextInput userTag = TextInput.create("usertag", "Usertag", TextInputStyle.SHORT)
                .setPlaceholder("Enter your username and your discriminator (ex. honiism#8022).")
                .setMinLength(2)
                .setMaxLength(40)
                .setRequired(true)
                .build();

            TextInput expectedBehavior = TextInput.create("expected-behavior", "Expected Behavior", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Explain what you expected to happen before you ran into the issue.")
                .setRequired(true)
                .setMinLength(10)
                .setMaxLength(2000)
                .build();

            TextInput steps = TextInput.create("steps", "Reproduction Steps", TextInputStyle.PARAGRAPH)
                .setPlaceholder("The steps you took to run into this issue.")
                .setRequired(true)
                .setMinLength(10)
                .setMaxLength(2500)
                .build();

            TextInput errorMessage = TextInput.create("error-message", "Error or Exceptions", TextInputStyle.PARAGRAPH)
                .setPlaceholder("The error message given by Lemi. If none, leave empty.")
                .setRequired(false)
                .setMinLength(10)
                .setMaxLength(2500)
                .build();

            Modal reportModal = Modal.create("report", "Report a bug!")
                .addActionRows(ActionRow.of(userTag), ActionRow.of(expectedBehavior),
                        ActionRow.of(steps), ActionRow.of(errorMessage))
                .build();

            event.replyModal(reportModal).queue();

            waitAndSendReport(Lemi.getInstance().getEventWaiter(), event.getMember());

        } else {
            String time = Tools.secondsToTime(((600 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription(("‧₊੭ :cherry_blossom: **CHILL!** ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`."))
                .setColor(0xffd1dc);
                
            event.replyEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void waitAndSendReport(EventWaiter waiter, Member member) {
        waiter.waitForEvent(
                ModalInteractionEvent.class,

                (event) -> event.getMember().getIdLong() == member.getIdLong()
                        && event.isFromGuild()
                        && event.getGuild().getIdLong() == Config.getLong("honeys_hive")
                        && event.getModalId().equals("report"),

                (event) -> {
                    event.reply(":cherry_blossom: Thank you for submitting your report, it is very appreciated.").queue();

                    String usertag = event.getValue("usertag").getAsString();
                    String expectedBehavior = event.getValue("expected-behavior").getAsString();
                    String steps = event.getValue("steps").getAsString();
                    String errorMessage = event.getValue("error-message").getAsString();

                    List<MessageEmbed> modalEmbeds = Arrays.asList(
                        EmbedUtils.getSimpleEmbedBuilder(expectedBehavior).setTitle("expected behavior").build(),
                        EmbedUtils.getSimpleEmbedBuilder(steps).setTitle("reproduction steps").build(),
                        (errorMessage != null) ?
                            EmbedUtils.getSimpleEmbedBuilder(errorMessage).setTitle("error message").build()
                            : EmbedUtils.getSimpleEmbed("No error message provided")
                    );

                    Lemi.getInstance().getShardManager().getGuildById(Config.getLong("honeys_hive"))
                        .getTextChannelById(Config.getLong("report_channel_id"))
                        .sendMessage(usertag + " (" + event.getMember().getAsMention() + ")'s report")
                        .setEmbeds(modalEmbeds)
                        .queue();
                }
        );
    }
}