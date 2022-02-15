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

package com.honiism.discord.lemi.commands.slash.currency;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.currency.WeightedRandom;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Bankrob extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Bankrob() {
        this.name = "bankrob";
        this.desc = "Attempt to bankrob (You can win double but at the same time you can also go in debt).";
        this.usage = "/currency bankrob";
        this.category = CommandCategory.CURRENCY;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(new OptionData(OptionType.BOOLEAN,
                                             "help",
                                             "Want a help guide for this command? (True = yes, false = no).")
                                         .setRequired(false)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();

        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (3600 * 1000);
        }
            
        if (timeDelayed >= (3600 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                hook.sendMessageEmbeds(this.getHelp(event)).queue();
                return;
            }

            Guild guild = event.getGuild();

            if (CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) < 10000) {
                hook.sendMessage(":blossom: You need at least 10,000 " 
                        + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + ".")
                    .queue();
                return;
            }

            WeightedRandom<String> randomResult = new WeightedRandom<String>();

            randomResult.add(70, "fail")
                .add(15, "success")
                .add(15, "nothing");

            if (randomResult.next().equals("fail")) {
                failAction(hook, author, guild);
            } else if (randomResult.next().equals("nothing")) {
                nothingAction(hook, author, guild);
            } else if (randomResult.next().equals("success")) {
                successAction(hook, author, guild);
            }
            
        } else {
            String time = Tools.secondsToTime(((3600 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }         
    }

    private void failAction(InteractionHook hook, User author, Guild guild) {
        Random random = new Random();
        int lostAmount = random.nextInt(10000 - 2000) + 2000;
        lostAmount += 1;
        
        String lostBal = lostAmount + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong()));

        CurrencyTools.removeBalFromUser(String.valueOf(author.getIdLong()),
        CurrencyTools.getUserbal(String.valueOf(author.getIdLong())), lostAmount);

        String[] resultMessages = new String[] {
                "You dropped the money bag and lost " + lostBal + ".",
                CurrencyTools.getRandomNPC() + " was in disguise and they charged you " + lostBal + ".",
                CurrencyTools.getRandomNPC() + " caught you and you paid " + lostBal + ".",
                "You got caught and paid " + lostBal + " to bail out of jail.",
                CurrencyTools.getRandomNPC() + " told on you and you paid " + lostBal + "."
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + Tools.getRandomEntry(resultMessages) + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) 
                + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void nothingAction(InteractionHook hook, User author, Guild guild) {
        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + "You wake up from the dream, you gained nothing." + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong()))
                + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void successAction(InteractionHook hook, User author, Guild guild) {
        Random random = new Random();
        
        int gainedAmount = random.nextInt(50000 - 10000) + 10000;
        gainedAmount += 1;
        String gainedBal = gainedAmount + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong()));

        CurrencyTools.addBalToUser(String.valueOf(author.getIdLong()),
                CurrencyTools.getUserbal(String.valueOf(author.getIdLong())), gainedAmount);

        String[] resultMessages = new String[] {
                "You ran away and gained " + gainedBal + ".",
                CurrencyTools.getRandomNPC() + " distracted the police and you both gained " + gainedBal + ".",
                CurrencyTools.getRandomNPC() + " sacrificed themselves and you ran away alone with " + gainedBal + ".",
                "The police got scared of you and paid you " + gainedBal + ".",
                CurrencyTools.getRandomNPC() + " made mango juice for the police and you got " + gainedBal + "."
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + Tools.getRandomEntry(resultMessages) + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) 
                + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }
}