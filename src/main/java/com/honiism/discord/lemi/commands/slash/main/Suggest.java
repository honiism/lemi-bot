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

public class Suggest extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Suggest() {
        setCommandData(Commands.slash("suggest", "Suggest a feature for Lemi."));
        setUsage("/suggest");
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

            TextInput featureName = TextInput.create("name", "Feature Name", TextInputStyle.SHORT)
                .setPlaceholder("A simple short name for the feature requested.")
                .setRequired(true)
                .setMinLength(1)
                .setMaxLength(100)
                .build();

            TextInput featureDesc = TextInput.create("desc", "Feature Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("The description of this feature.")
                .setRequired(true)
                .setMinLength(10)
                .setMaxLength(2500)
                .build();

            TextInput featureExample = TextInput.create("example", "Feature Example", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Please provide some example how this feature will be used.")
                .setRequired(true)
                .setMinLength(10)
                .setMaxLength(2500)
                .build();

            Modal reportModal = Modal.create("suggest", "Suggest a feature!")
                .addActionRows(ActionRow.of(userTag), ActionRow.of(featureName),
                        ActionRow.of(featureDesc), ActionRow.of(featureExample))
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
                        && event.getModalId().equals("suggest"),

                (event) -> {
                    event.reply(":cherry_blossom: Thank you for submitting your feature request, it is very appreciated.").queue();

                    String usertag = event.getValue("usertag").getAsString();
                    String featureName = event.getValue("name").getAsString();
                    String featureDesc = event.getValue("desc").getAsString();
                    String featureExample = event.getValue("example").getAsString();

                    List<MessageEmbed> modalEmbeds = Arrays.asList(
                        EmbedUtils.getSimpleEmbedBuilder(featureName).setTitle(":tulip: Feature Name").build(),
                        EmbedUtils.getSimpleEmbedBuilder(featureDesc).setTitle(":sunflower: Feature Description").build(),
                        EmbedUtils.getSimpleEmbedBuilder(featureExample).setTitle(":seedling: Feature Example").build()
                    );

                    Lemi.getInstance().getShardManager().retrieveUserById(Config.getLong("dev_id")).queue(
                            (dev) -> {
                                dev.openPrivateChannel().queue(
                                    (channel) -> {
                                        channel.sendMessage(usertag + " (" + event.getMember().getAsMention() + ")'s feature request")
                                            .setEmbeds(modalEmbeds)
                                            .queue();
                                    }
                                );
                            }
                    );
                }
        );
    }
}