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

import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.data.items.Items;

import java.util.HashMap;
import java.util.Random;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.WeightedRandom;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Beg extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Beg() {
        setCommandData(Commands.slash("beg", "Try out your chances! You can get small, maybe rare items."));
        setUsage("/currency beg");
        setCategory(CommandCategory.CURRENCY);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();

        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (45 * 1000);
        }
            
        if (timeDelayed >= (45 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            WeightedRandom<String> randomRarity = new WeightedRandom<>();
            WeightedRandom<String> randomLootType = new WeightedRandom<>();

            randomRarity.add(45, "fail")
                .add(25, "common")
                .add(20, "uncommon")
                .add(9.5, "rare")
                .add(0.5, "super-rare");

            randomLootType.add(50, "coins").add(50, "item");

            Guild guild = event.getGuild();

            switch (randomRarity.next()) {
                case "fail":
                    failAction(hook, author, guild);
                    break;

                case "common":
                    commonAction(hook, author, guild);
                    break;

                case "uncommon":
                    uncommonAction(hook, author, guild, randomLootType.next());
                    break;

                case "rare":
                    rareAction(hook, author, guild, randomLootType.next());
                    break;

                case "super-rare":
                    superRareAction(hook, author, guild, randomLootType.next());
            }

        } else {
            String time = Tools.secondsToTime(((45 * 1000) - timeDelayed) / 1000);
                
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
        String[] resultMessages = new String[] {
            "Shoo.",
            "RareItem.exe has stopped working <3!",
            "HAHA",
            "I don't have money on me rn, *goes into the store*.",
            "No <3.",
            "Here, some air for ya.",
            "Did you know that Honey is so pog? Anyway no lol.",
            "No you, pls I also need money-",
            "Yeah, just wait a sec! *runs*",
            "Uhm- uh uh look- uh-"
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " 
                + CurrencyTools.getRandomNPC() + ": \"" + Tools.getRandomEntry(resultMessages) + "\"\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                + " " + CurrencyTools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void commonAction(InteractionHook hook, User author, Guild guild) {
        Random random = new Random();
        int gainedAmount = random.nextInt(250 - 50) + 50;
        gainedAmount += 1;
        
        String gainedBal = gainedAmount + " " + CurrencyTools.getBalName();

        CurrencyTools.addBalToUser(author.getIdLong(), gainedAmount);

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " 
                + CurrencyTools.getRandomNPC() + " gave you " + gainedBal + "\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                + " " + CurrencyTools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void uncommonAction(InteractionHook hook, User author, Guild guild, String lootType) {
        if (lootType.equals("coins")) {
            Random random = new Random();
            int gainedAmount = random.nextInt(500 - 200) + 200;
            gainedAmount += 1;

            String gainedBal = gainedAmount + " " + CurrencyTools.getBalName();

            CurrencyTools.addBalToUser(author.getIdLong(), gainedAmount);
        
            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() + " gave you " + gainedBal + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
                
        } else if (lootType.equals("item")) {
            WeightedRandom<Items> randomItem = new WeightedRandom<>();

            randomItem.add(20, new Items.Fish())
                .add(20, new Items.Strawberry())
                .add(20, new Items.SmallFossil())
                .add(20, new Items.Junk())
                .add(20, new Items.Cookie());

            long userId = author.getIdLong();
            Items pickedItem = randomItem.next();
            String itemName = pickedItem.getName();
            String itemEmoji = pickedItem.getEmoji();

            CurrencyTools.addItemToUser(userId, itemName, 1);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() 
                    + " gave you a " + itemEmoji + " " + itemName + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
        }
    }

    private void rareAction(InteractionHook hook, User author, Guild guild, String lootType) {
        if (lootType.equals("coins")) {
            Random random = new Random();
            int gainedAmount = random.nextInt(800 - 500) + 500;
            gainedAmount += 1;

            String gainedBal = gainedAmount + " " + CurrencyTools.getBalName();

            CurrencyTools.addBalToUser(author.getIdLong(), gainedAmount);
        
            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() + " gave you " + gainedBal + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
                
        } else if (lootType.equals("item")) {
            WeightedRandom<Items> randomItem = new WeightedRandom<>();

            randomItem.add(25, new Items.Notebook())
                .add(25, new Items.Donut())
                .add(25, new Items.LenSushi())
                .add(25, new Items.Sticker());

            long userId = author.getIdLong();
            Items pickedItem = randomItem.next();
            String itemName = pickedItem.getName();
            String itemEmoji = pickedItem.getEmoji();

            CurrencyTools.addItemToUser(userId, itemName, 1);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() 
                    + " gave you a " + itemEmoji + " " + itemName + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
        }
    }

    private void superRareAction(InteractionHook hook, User author, Guild guild, String lootType) {
        if (lootType.equals("coins")) {
            Random random = new Random();
            int gainedAmount = random.nextInt(1200 - 800) + 800;
            gainedAmount += 1;

            String gainedBal = gainedAmount + " " + CurrencyTools.getBalName();

            CurrencyTools.addBalToUser(author.getIdLong(), gainedAmount);
        
            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() + " gave you " + gainedBal + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
                
        } else if (lootType.equals("item")) {
            WeightedRandom<Items> randomItem = new WeightedRandom<>();

            randomItem.add(50, new Items.CommonChest())
                .add(50, new Items.BankNote());

            long userId = author.getIdLong();
            Items pickedItem = randomItem.next();
            String itemName = pickedItem.getName();
            String itemEmoji = pickedItem.getEmoji();

            CurrencyTools.addItemToUser(userId, itemName, 1);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **BEGGING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() 
                    + " gave you a " + itemEmoji + " " + itemName + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserBal(author.getIdLong()) 
                    + " " + CurrencyTools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
        }
    }
}