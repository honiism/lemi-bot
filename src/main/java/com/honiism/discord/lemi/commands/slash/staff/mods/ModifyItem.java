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

package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyItem extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyItem() {
        this.name = "modifyitem";
        this.desc = "Add or remove some items from a user.";
        this.usage = "/mods modifybal ((subcommand))";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),
                                      
                                     new SubcommandData("add", "Add some items to a user.")
                                         .addOption(OptionType.USER, "user", "The user you'd like to give some items to.", true)
                                         .addOption(OptionType.STRING, "item-name",
                                                 "The name of the item you'd like to add.",
                                                 true)
                                         .addOption(OptionType.INTEGER, "amount",
                                                 "The amount of item you'd like to add.'",
                                                 true),
  
                                     new SubcommandData("remove", "Remove some otems from a user.")
                                         .addOption(OptionType.USER, "user",
                                                 "The user you'd like to take some items from.",
                                                 true)
                                         .addOption(OptionType.STRING, "item-name",
                                                 "The name of the item you'd like to take.'",
                                                 true)
                                         .addOption(OptionType.INTEGER, "amount",
                                                 "The amount of item you'd like to take.'",
                                                 true)
                                    );
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

            String subCmdName = event.getSubcommandName();

            switch (subCmdName) {
                case "help":
                    hook.sendMessageEmbeds(this.getHelp(event)).queue();
                    break;

                case "add":
                    int addAmount = (int) event.getOption("amount").getAsLong();

                    if (addAmount < 0 || addAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot give less or equal to 0 amount of item.").queue();
                        return;
                    }

                    if (!CurrencyTools.checkIfItemExists(event.getOption("item-name").getAsString())) {
                        hook.sendMessage(":tea: That item does not exist.").queue();
                        return;
                    }

                    User userAdd = event.getOption("user").getAsUser();
            
                    CurrencyTools.addItemToUser(String.valueOf(userAdd.getIdLong()), event.getOption("item-name").getAsString(),
                            CurrencyTools.getItemFromUserInv(String.valueOf(userAdd.getIdLong()),
                                    event.getOption("item-name").getAsString()),
                            addAmount);
            
                    hook.sendMessage(":oden: " 
                            + userAdd.getAsMention() 
                            + ", you have received " + addAmount 
                            + " " + event.getOption("item-name").getAsString() + " from " 
                            + author.getAsMention() + "!\r\n"
                            + ":blueberries: You now have " 
                            + CurrencyTools.getItemFromUserInv(String.valueOf(userAdd.getIdLong()),
                                    event.getOption("item-name").getAsString())
                            + " " + event.getOption("item-name").getAsString() + ".")
                        .queue();
                    break;

                case "remove":
                    int removeAmount = (int) event.getOption("amount").getAsLong();

                    if (removeAmount < 0 || removeAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot remove less or equal to 0 amount of items").queue();
                        return;
                    }

                    if (!CurrencyTools.checkIfItemExists(event.getOption("item-name").getAsString())) {
                        hook.sendMessage(":tea: That item does not exist.").queue();
                        return;
                    }
                        
                    User userRemove = event.getOption("user").getAsUser();

                    if (CurrencyTools.getItemFromUserInv(String.valueOf(userRemove.getIdLong()),
                            event.getOption("item-name").getAsString()) == 0) {
                        hook.sendMessage(":snowflake: Sheesh you tryna make em even more broke? They have none of those item.")
                            .queue();
                        return;
                    }

                    if (CurrencyTools.getItemFromUserInv(String.valueOf(userRemove.getIdLong()), 
                            event.getOption("item-name").getAsString()) < removeAmount) {
                        hook.sendMessage(":hibiscus: You cannot take more than what they have.").queue();
                        return;
                    }

                    CurrencyTools.removeItemFromUser(String.valueOf(userRemove.getIdLong()), event.getOption("item-name").getAsString(),
                            CurrencyTools.getItemFromUserInv(String.valueOf(userRemove.getIdLong()),
                                    event.getOption("item-name").getAsString()),
                            removeAmount);
            
                    hook.sendMessage(":oden: " 
                            + userRemove.getAsMention() 
                            + ", " + author.getAsMention()
                            + " has taken " + removeAmount + " " + event.getOption("item-name").getAsString() + " from " 
                            + "you" + "!\r\n"
                            + ":blueberries: You now have " 
                            + CurrencyTools.getItemFromUserInv(String.valueOf(userRemove.getIdLong()), event.getOption("item-name").getAsString())
                            + " " + event.getOption("item-name").getAsString() + ".")
                        .queue();
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }        
    }    
}