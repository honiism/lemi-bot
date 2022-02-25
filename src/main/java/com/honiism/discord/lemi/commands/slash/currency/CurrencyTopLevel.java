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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CurrencyTopLevel extends SlashCmd {

    private Balance balanceCmd = new Balance();
    private Inventory inventoryCmd = new Inventory();
    private Bankrob bankrobCmd = new Bankrob();
    private Beg begCmd = new Beg();
    private Cook cookCmd = new Cook();

    public CurrencyTopLevel(Balance balanceCmd, Inventory inventoryCmd, Bankrob bankrobCmd, Beg begCmd,
                            Cook cookCmd) {
        this.balanceCmd = balanceCmd;
        this.inventoryCmd = inventoryCmd;
        this.bankrobCmd = bankrobCmd;
        this.begCmd = begCmd;
        this.cookCmd = cookCmd;

        setCommandData(Commands.slash("currency", "Commands for the currency category.")
                .addSubcommands(
                        new SubcommandData(this.balanceCmd.getName(), this.balanceCmd.getDesc())
                                .addOptions(this.balanceCmd.getOptions()),

                        new SubcommandData(this.inventoryCmd.getName(), this.inventoryCmd.getDesc())
                                .addOptions(this.inventoryCmd.getOptions()),

                        new SubcommandData(this.bankrobCmd.getName(), this.bankrobCmd.getDesc()),

                        new SubcommandData(this.begCmd.getName(), this.begCmd.getDesc()),

                        new SubcommandData(this.cookCmd.getName(), this.cookCmd.getDesc())
                )
        );

        setUsage("/currency ((subcommand groups/subcommands))");
        setCategory(CommandCategory.CURRENCY);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        String subCmdName = event.getSubcommandName();

        if (subCmdName != null) {
            switch (subCmdName) {
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