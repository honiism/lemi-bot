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

package com.honiism.discord.lemi.commands.slash.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.data.UserDataManager;

import java.util.HashMap;
import java.util.Random;

import com.honiism.discord.lemi.utils.currency.WeightedRandom;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Bankrob extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Bankrob() {
        setCommandData(Commands.slash("bankrob", "Attempt to bankrob."));
        setUsage("/currency bankrob");
        setCategory(CommandCategory.CURRENCY);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) throws JsonMappingException, JsonProcessingException {
        InteractionHook hook = event.getHook();
        User author = event.getUser();

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

            setUserDataManager(author.getIdLong());

            UserDataManager dataManager = getUserDataManager();

            if (dataManager.getBal() < 10000) {
                hook.sendMessage(":blossom: You need at least 10,000 " 
                        + Tools.getBalName() + ".")
                    .queue();
                return;
            }

            WeightedRandom<String> randomResult = new WeightedRandom<String>()
                .add(70, "fail")
                .add(15, "success")
                .add(15, "nothing");
            
            String randomResultString = randomResult.next();

            if (randomResultString.equals("fail")) {
                failAction(hook, author, dataManager);
            } else if (randomResultString.equals("nothing")) {
                nothingAction(hook, author, dataManager);
            } else if (randomResultString.equals("success")) {
                successAction(hook, author, dataManager);
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

    private void failAction(InteractionHook hook, User author, UserDataManager dataManager) {
        Random random = new Random();
        int lostAmount = random.nextInt(10000 - 2000) + 2000;
        lostAmount += 1;
        
        String lostBal = lostAmount + " " + Tools.getBalName();

        dataManager.removeBalFromUser(lostAmount);

        String[] resultMessages = new String[] {
                "You dropped the money bag and lost " + lostBal + ".",
                Tools.getRandomNPC() + " was in disguise and they charged you " + lostBal + ".",
                Tools.getRandomNPC() + " caught you and you paid " + lostBal + ".",
                "You got caught and paid " + lostBal + " to bail out of jail.",
                Tools.getRandomNPC() + " told on you and you paid " + lostBal + "."
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + Tools.getRandomEntry(resultMessages) + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + dataManager.getBal()
                + " " + Tools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void nothingAction(InteractionHook hook, User author, UserDataManager dataManager) {
        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + "You wake up from the dream, you gained nothing." + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + dataManager.getBal()
                + " " + Tools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void successAction(InteractionHook hook, User author, UserDataManager dataManager) {
        Random random = new Random();
        
        int gainedAmount = random.nextInt(50000 - 10000) + 10000;
        gainedAmount += 1;
        String gainedBal = gainedAmount + " " + Tools.getBalName();

        dataManager.addBalToUser(gainedAmount);

        String[] resultMessages = new String[] {
                "You ran away and gained " + gainedBal + ".",
                Tools.getRandomNPC() + " distracted the police and you both gained " + gainedBal + ".",
                Tools.getRandomNPC() + " sacrificed themselves and you ran away alone with " + gainedBal + ".",
                "The police got scared of you and paid you " + gainedBal + ".",
                Tools.getRandomNPC() + " made mango juice for the police and you got " + gainedBal + "."
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **ROBBING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " + Tools.getRandomEntry(resultMessages) + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + dataManager.getBal()
                + " " + Tools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }
}