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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyBal extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyBal() {
        setCommandData(Commands.slash("modifybal", "Add or remove some currency from a user.")
                .addSubcommands(
                        new SubcommandData("add", "Add some currency to a user.")
                                .addOption(OptionType.USER, "user", "The user you'd like to add some currency to.", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of currency you'd like to add.'", true),
  
                        new SubcommandData("remove", "Remove some currency from a user.")
                                .addOption(OptionType.USER, "user", "The user you'd like to remove some currency from.", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of currency you'd like to remove.", true)
                )
        );

        setUsage("/mods modifybal ((subcommands))");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setGlobal(true);
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
            Guild guild = event.getGuild();

            switch (subCmdName) {
                case "add":
                    int addAmount = (int) event.getOption("amount").getAsLong();

                    if (addAmount < 0 || addAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot give less or equal to 0 amount of currency.").queue();
                        return;
                    } 
                        
                    Member memberToAddCurr = event.getOption("user").getAsMember();

                    if (memberToAddCurr == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }
            
                    CurrencyTools.addBalToUser(String.valueOf(memberToAddCurr.getIdLong()), addAmount);
            
                    hook.sendMessage(":cherry_blossom: " 
                            + memberToAddCurr.getAsMention() 
                            + ", you have received " + addAmount 
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + " from " 
                            + author.getAsMention() + "!\r\n"
                            + ":blueberries: You now have " + CurrencyTools.getUserbal(String.valueOf(memberToAddCurr.getIdLong()))
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + ".")
                        .queue();
                    break;

                case "remove":
                    int removeAmount = (int) event.getOption("amount").getAsLong();

                    if (removeAmount < 0 || removeAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot remove less or equal to 0 amount of currency.").queue();
                        return;
                    } 
                        
                    Member memberToRemoveCurr = event.getOption("user").getAsMember();

                    if (memberToRemoveCurr == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }
            
                    CurrencyTools.removeBalFromUser(String.valueOf(memberToRemoveCurr.getIdLong()), removeAmount);
            
                    hook.sendMessage(":cherry_blossom: " 
                            + memberToRemoveCurr.getAsMention() 
                            + ", " + author.getAsMention()
                            + " has taken " + removeAmount + " " 
                            + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + " from " + "you" + "!\r\n"
                            + ":blueberries: You now have " + CurrencyTools.getUserbal(String.valueOf(memberToRemoveCurr.getIdLong()))
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + ".")
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