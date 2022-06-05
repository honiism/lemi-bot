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
import com.honiism.discord.lemi.data.currency.UserDataManager;
import com.honiism.discord.lemi.data.items.Items;

import java.util.HashMap;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.WeightedRandom;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cook extends SlashCmd {
    
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    private String[] foodTypes = new String[] {
        ":pizza:", ":hamburger:", ":fries:",
        ":hotdog:", ":popcorn:", ":bacon:",
        ":cooking:", ":pancakes:", ":waffle:", 
        ":bread:", ":croissant:", ":pretzel:",
        ":bagel:", ":french_bread:", ":salad:",
        ":stuffed_flatbread:", ":sandwich:",
        ":taco:", ":burrito:", ":poultry_leg:",
        ":sweet_potato:", ":dumpling:",
        ":fortune_cookie:", ":takeout_box:",
        ":bento:", ":rice_cracker:",
        ":rice_ball:", ":curry:", ":ramen:",
        ":sushi:", ":fried_shrimp:", ":fish_cake:",
        ":moon_cake:", ":oden:", ":falafel:",
        ":shallow_pan_of_food:", ":stew:",
        ":spaghetti:", ":pie:", ":icecream:",
        ":shaved_ice:", ":ice_cream:", ":doughnut:",
        ":cookie:", ":birthday:", ":cake:", ":cupcake:",
        ":chocolate_bar:", ":candy:", ":lollipop:",
        ":dango:", ":custard:", ":coffee:", ":tea:"
    };

    public Cook() {
        setCommandData(Commands.slash("cook", "Attempt to cook."));
        setUsage("/currency cook");
        setCategory(CommandCategory.CURRENCY);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
    }

    @Override
    public void action(SlashCommandInteractionEvent event) throws JsonProcessingException {
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

            setUserDataManager(author.getIdLong());

            WeightedRandom<String> randomResult = new WeightedRandom<String>()
                .add(60, "fail")
                .add(40, "success");

            String randomResultString = randomResult.next();
            UserDataManager dataManager = getUserDataManager();

            if (randomResultString.equals("fail")) {
                failAction(hook, author, dataManager);
            } else if (randomResultString.equals("success")) {
                WeightedRandom<String> randomLootType = new WeightedRandom<String>()
                    .add(50, "coins")
                    .add(50, "item");

                successAction(hook, author, dataManager, randomLootType.next());
            }

        } else {
            String time = Tools.secondsToTime(((45 * 1000) - timeDelayed) / 1000);
                
            hook.sendMessageEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }         
    }
    
    private void failAction(InteractionHook hook, User author, UserDataManager dataManager) {
        String[] resultMessages = new String[] {
            "HAHA, TOOK TOO SLOW TO ASK ME FOR THE MONEY BYEE! I ENJOYED THE " + Tools.getRandomEntry(foodTypes) + "!",
            "I need to call your manager, there's something weird about that " + Tools.getRandomEntry(foodTypes) + ".",
            "There's a fly in my " + Tools.getRandomEntry(foodTypes) + "!",
            "Ew you burnt my " + Tools.getRandomEntry(foodTypes) + ".",
            "This " + Tools.getRandomEntry(foodTypes) + " taste like Honey's cookies, gross.",
            "Do you even have any experience with cooking? This " + Tools.getRandomEntry(foodTypes) + " is horrible.",
            "Uhm, I left my wallet at home, can't pay for the " + Tools.getRandomEntry(foodTypes) + ".",
            "Money?? Uhm. . . bye! Thanks for the " + Tools.getRandomEntry(foodTypes) + ".",
            "I thought that the " + Tools.getRandomEntry(foodTypes) + " were the samples?",
            "Why would I pay for this gross " + Tools.getRandomEntry(foodTypes) + " ?!"
        };

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **COOKING . . .**\r\n" 
                + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                + "> " + author.getAsMention() + "\r\n"
                + "> :cherry_blossom: " 
                + Tools.getRandomNPC() + ": \"" + Tools.getRandomEntry(resultMessages) + "\"\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + dataManager.getBal() 
                + " " + Tools.getBalName() + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void successAction(InteractionHook hook, User author, UserDataManager dataManager, String lootType) {
        if (lootType.equals("coins")) {
            Random random = new Random();
            int gainedAmount = random.nextInt(500 - 200) + 200;
            gainedAmount += 1;
            String gainedBal = gainedAmount + " " + Tools.getBalName();

            dataManager.addBalToUser(gainedAmount);
        
            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **COOKING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + Tools.getRandomNPC() + " gave you " + gainedBal 
                    + " for the perfect " + Tools.getRandomEntry(foodTypes) + ".\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + dataManager.getBal()
                    + " " + Tools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
                
        } else if (lootType.equals("item")) {
            WeightedRandom<Items> randomItem = new WeightedRandom<>();

            randomItem.add(20, new Items.Cookie())
                .add(20, new Items.Donut())
                .add(20, new Items.LenSushi())
                .add(20, new Items.Fish())
                .add(20, new Items.Duck());

            Items pickedItem = randomItem.next();
            String itemId = pickedItem.getId();
            String itemEmoji = pickedItem.getEmoji();

            dataManager.addItemToUser(itemId, 1);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **COOKING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + Tools.getRandomNPC() 
                    + " gave you a " + itemEmoji + " " + itemId + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + dataManager.getBal() 
                    + " " + Tools.getBalName() + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
        }
    }
}