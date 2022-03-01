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

import java.util.HashMap;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyInv extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyInv() {
        setCommandData(Commands.slash("modifyinv", "Add or remove some items from a user's inventory.")
                .addSubcommands(
                        new SubcommandData("add", "Add some items to a user.")
                                .addOption(OptionType.USER, "user", "The user you'd like to give some items to.", true)
                                .addOption(OptionType.STRING, "item_name", "The name of the item you'd like to add.", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of item you'd like to add.'", true),
  
                        new SubcommandData("remove", "Remove some items from a user.")
                                .addOption(OptionType.USER, "user", "The user you'd like to take some items from.", true)
                                .addOption(OptionType.STRING, "item_name", "The name of the item you'd like to take.'", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of item you'd like to take.'", true)
                )
        );

        setUsage("/mods modifyInv ((subcommands))");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
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
                case "add":
                    long addAmount = (long) event.getOption("amount", OptionMapping::getAsLong);

                    if (addAmount < 0 || addAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot give less or equal to 0 amount of item.").queue();
                        return;
                    }

                    String itemNameToAdd = event.getOption("item_name", OptionMapping::getAsString);

                    if (!CurrencyTools.checkIfItemExists(itemNameToAdd)) {
                        hook.sendMessage(":tea: That item does not exist.").queue();
                        return;
                    }

                    Member memberAdd = event.getOption("user", OptionMapping::getAsMember);

                    if (memberAdd == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }
            
                    CurrencyTools.addItemToUser(memberAdd.getIdLong(), itemNameToAdd, addAmount);
            
                    hook.sendMessage(":oden: " 
                            + memberAdd.getAsMention() 
                            + ", you have received " + addAmount 
                            + " " + itemNameToAdd + " from " 
                            + author.getAsMention() + "!\r\n"
                            + ":blueberries: You now have " 
                            + CurrencyTools.getItemFromUserInv(memberAdd.getIdLong(), itemNameToAdd)
                            + " " + itemNameToAdd + ".")
                        .queue();
                    break;

                case "remove":
                    long removeAmount = event.getOption("amount", OptionMapping::getAsLong);

                    if (removeAmount < 0 || removeAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot remove less or equal to 0 amount of items").queue();
                        return;
                    }

                    String itemNameToRemove = event.getOption("item_name", OptionMapping::getAsString);

                    if (!CurrencyTools.checkIfItemExists(itemNameToRemove)) {
                        hook.sendMessage(":tea: That item does not exist.").queue();
                        return;
                    }

                    Member memberRemove = event.getOption("user", OptionMapping::getAsMember);

                    if (memberRemove == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    if (CurrencyTools.getItemFromUserInv(memberRemove.getIdLong(), itemNameToRemove) < removeAmount) {
                        hook.sendMessage(":hibiscus: You cannot take more than what they have.").queue();
                        return;
                    }

                    CurrencyTools.removeItemFromUser(memberRemove.getIdLong(), itemNameToRemove, removeAmount);
            
                    hook.sendMessage(":oden: " 
                            + memberRemove.getAsMention() 
                            + ", " + author.getAsMention()
                            + " has taken " + removeAmount + " " + itemNameToRemove + " from " 
                            + "you" + "!\r\n"
                            + ":blueberries: You now have " 
                            + CurrencyTools.getItemFromUserInv(memberRemove.getIdLong(), itemNameToRemove)
                            + " " + itemNameToRemove + ".")
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