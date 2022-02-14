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

import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
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

public class Cook extends SlashCmd {
    
    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Cook() {
        this.name = "cook";
        this.desc = "Let's see if your cooking skills can handle this!";
        this.usage = "/currency cook";
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
            timeDelayed = (45 * 1000);
        }
            
        if (timeDelayed >= (45 * 1000)) {        
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
            WeightedRandom<String> randomResult = new WeightedRandom<>();

            randomResult.add(60, "fail").add(40, "success");

            switch (randomResult.next()) {
                case "fail":
                    failAction(hook, author, guild);
                    break;

                case "success":
                    WeightedRandom<String> randomLootType = new WeightedRandom<>();
                    randomLootType.add(50, "coins").add(50, "item");

                    successAction(hook, author, guild, randomLootType.next());
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
    
    private void failAction(InteractionHook hook, User author, Guild guild) {
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
                + CurrencyTools.getRandomNPC() + ": \"" + Tools.getRandomEntry(resultMessages) + "\"\r\n"
                + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) 
                + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
            .queue();
    }

    private void successAction(InteractionHook hook, User author, Guild guild, String lootType) {
        if (lootType.equals("coins")) {
            Random random = new Random();
            int gainedAmount = random.nextInt(500 - 200) + 200;
            gainedAmount += 1;
            String gainedBal = gainedAmount + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong()));

            CurrencyTools.addBalToUser(String.valueOf(author.getIdLong()),
                    CurrencyTools.getUserbal(String.valueOf(author.getIdLong())), gainedAmount);
        
            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **COOKING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() + " gave you " + gainedBal 
                    + " for the perfect " + Tools.getRandomEntry(foodTypes) + ".\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) 
                    + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
                
        } else if (lootType.equals("item")) {
            WeightedRandom<Items> randomItem = new WeightedRandom<>();

            randomItem.add(20, new Items.Cookie())
                .add(20, new Items.Donut())
                .add(20, new Items.LenSushi())
                .add(20, new Items.Fish())
                .add(20, new Items.Duck());

            String userId = String.valueOf(author.getIdLong());
            Items pickedItem = randomItem.next();
            String itemName = pickedItem.getName();
            String itemEmoji = pickedItem.getEmoji();

            CurrencyTools.addItemToUser(userId, itemName,
                    CurrencyTools.getItemFromUserInv(userId, itemName), 1);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tulip: **COOKING . . .**\r\n" 
                    + "**˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.**\r\n"
                    + "> " + author.getAsMention() + "\r\n"
                    + "> :cherry_blossom: " 
                    + CurrencyTools.getRandomNPC() 
                    + " gave you a " + itemEmoji + " " + itemName + "\r\n"
                    + "**︶︶︶︶︶︶︶︶︶︶︶︶︶**\r\n"
                    + "> :sunflower: You now have " + CurrencyTools.getUserbal(String.valueOf(author.getIdLong())) 
                    + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + "\r\n"
                    + "> ╰ ʚ₊˚꒦꒷✦ 🌱"))
                .queue();
        }
    }
}