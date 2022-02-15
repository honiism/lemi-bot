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

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CurrencyTopLevel extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();

    private Balance balanceCmd = new Balance();
    private Inventory inventoryCmd = new Inventory();
    private Bankrob bankrobCmd = new Bankrob();
    private Beg begCmd = new Beg();
    private Cook cookCmd = new Cook();

    private long timeDelayed;

    public CurrencyTopLevel(Balance balanceCmd, Inventory inventoryCmd, Bankrob bankrobCmd, Beg begCmd,
                            Cook cookCmd) {
        this.balanceCmd = balanceCmd;
        this.inventoryCmd = inventoryCmd;
        this.bankrobCmd = bankrobCmd;
        this.begCmd = begCmd;
        this.cookCmd = cookCmd;

        this.name = "currency";
        this.desc = "Commands for the currency category.";
        this.usage = "/currency ((subcommand group/subcommand))";
        this.category = CommandCategory.CURRENCY;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData(this.balanceCmd.getName(), this.balanceCmd.getDesc())
                                         .addOptions(this.balanceCmd.getOptions()),

                                     new SubcommandData(this.inventoryCmd.getName(), this.inventoryCmd.getDesc())
                                         .addOptions(this.inventoryCmd.getOptions()),

                                     new SubcommandData(this.bankrobCmd.getName(), this.bankrobCmd.getDesc())
                                         .addOptions(this.bankrobCmd.getOptions()),

                                     new SubcommandData(this.begCmd.getName(), this.begCmd.getDesc())
                                         .addOptions(this.begCmd.getOptions()),

                                     new SubcommandData(this.cookCmd.getName(), this.cookCmd.getDesc())
                                         .addOptions(this.cookCmd.getOptions())
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        String subCmdName = event.getSubcommandName();

        if (subCmdName != null) {
            switch (subCmdName) {
                case "help":
                    if (delay.containsKey(author.getIdLong())) {
                        timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
                    } else {
                        timeDelayed = (5 * 1000);
                    }
                        
                    if (timeDelayed >= (5 * 1000)) {        
                        if (delay.containsKey(author.getIdLong())) {
                            delay.remove(author.getIdLong());
                        }
                    
                        delay.put(author.getIdLong(), System.currentTimeMillis());
            
                        hook.sendMessageEmbeds(this.getHelp(event)).queue();
                        
                    } else {
                        String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                            
                        EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                            .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                                    + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                            .setColor(0xffd1dc);
                            
                        hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
                    }
                    break;

                case "balance":
                    this.balanceCmd.action(event);
                    break;

                case "inventory":
                    this.inventoryCmd.action(event);
                    break;

                case "bankrob":
                    this.bankrobCmd.action(event);
                    break;

                case "beg":
                    this.begCmd.action(event);
                    break;

                case "cook":
                    this.cookCmd.action(event);
                    break;
            }
        }     
    }
}