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

package com.honiism.discord.lemi.commands.slash.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.currency.Balance;
import com.honiism.discord.lemi.commands.slash.currency.Bankrob;
import com.honiism.discord.lemi.commands.slash.currency.Beg;
import com.honiism.discord.lemi.commands.slash.currency.Cook;
import com.honiism.discord.lemi.commands.slash.currency.CurrencyTopLevel;
import com.honiism.discord.lemi.commands.slash.currency.Inventory;
import com.honiism.discord.lemi.commands.slash.main.Donate;
import com.honiism.discord.lemi.commands.slash.main.Help;
import com.honiism.discord.lemi.commands.slash.main.Ping;
import com.honiism.discord.lemi.commands.slash.main.Report;
import com.honiism.discord.lemi.commands.slash.main.Suggest;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class SlashCmdManager {

    private static final Logger log = LoggerFactory.getLogger(SlashCmdManager.class);

    private List<CommandData> cmdsToAdd;
    private List<SlashCmd> allSubTopCmds = new ArrayList<>();
    
    private Map<CommandCategory, List<SlashCmd>> cmdsByCategory = new HashMap<>();
    private Map<String, SlashCmd> commandsMap = new HashMap<>();

    public void initialize() {
        setCmds();
        updateCmdCategory();
    }

    public void reloadGlobalCmds() {
        Lemi.getInstance().getShardManager().getShards().get(0)
            .updateCommands()
            .addCommands(cmdsToAdd)
            .queue();
    }

    public void upsertGlobal(CommandData command) {
        Lemi.getInstance().getShardManager().getShards().get(0)
            .upsertCommand(command)
            .queue();
    }

    public void reloadGuildCmds(Guild guild) {
        guild.updateCommands().addCommands(cmdsToAdd).queue();
    }

    public void upsertGuild(Guild guild, CommandData command) {
        guild.upsertCommand(command).queue();
    }

    public void clearGuildCmds(Guild guild) {
        guild.updateCommands().queue();
    }

    public void clearGlobalCmds() {
        Lemi.getInstance().getShardManager().getShards().get(0)
            .updateCommands()
            .queue();
    }

    public List<SlashCmd> getAllCmds() {
        return allSubTopCmds;
    }

    public SlashCmd getCmdByName(String name) {
        for (SlashCmd cmd : getAllCmds()) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    public Collection<SlashCmd> getCmdByCategory(CommandCategory category) {
        if (cmdsByCategory.get(category) == null) {
            return null;
        }
        return cmdsByCategory.get(category);
    }

    public List<String> getCmdNamesByCategory(Collection<SlashCmd> cmdsByCategory) {
        List<String> cmdNames = new ArrayList<>();

        if (Tools.isEmpty(cmdsByCategory)) {
            cmdNames.add("No commands for this category yet.");
            return cmdNames;
        }

        cmdsByCategory.forEach(cmd -> {
            if (cmd.getSubCmdGroups() != null) {
                cmd.getSubCmdGroups().forEach((subGroup) -> {
                    cmdNames.add("/" + cmd.getName() + " " + subGroup.getName());
                });
            }

            if (cmd.getSubCmds() != null) {
                cmd.getSubCmds().forEach((subCmd) -> {
                    cmdNames.add("/" + cmd.getName() + " " + subCmd.getName());
                });
            }

            if (cmd.getCategory().equals(CommandCategory.MAIN)) {
                cmdNames.add("/" + cmd.getName());
            }
        });

        return cmdNames;
    }

    public void handle(SlashCommandInteractionEvent event) {
        String executedCmdName = event.getName();
        SlashCmd slashCmd = commandsMap.get(executedCmdName);

        if (slashCmd != null) {
            slashCmd.preAction(event);
        }
    }

    private void registerCmd(SlashCmd cmd) {
        if (commandsMap.containsKey(cmd.getName())) {
            return;
        }
        commandsMap.put(cmd.getName(), cmd);
    }

    private void updateCmdCategory() {
        for (CommandCategory category : CommandCategory.values()) {
            cmdsByCategory.put(category,
                    commandsMap.values()
                        .stream()
                        .filter(cmd -> cmd.getCategory().equals(category))
                        .collect(Collectors.toList())
            );
        }

        log.info("Updated all SLASH commands according to it's categories.");
    }

    private void setCmds() {
        Help helpCmd = new Help();
        Ping pingCmd = new Ping();
        Donate donateCmd = new Donate();
        Balance balanceCmd = new Balance();
        Inventory inventoryCmd = new Inventory();
        Bankrob bankrobCmd = new Bankrob();
        Beg begCmd = new Beg();
        Cook cookCmd = new Cook();
        Report reportCmd = new Report();
        Suggest suggestCmd = new Suggest();

        CurrencyTopLevel currencyTopLevelCmd = new CurrencyTopLevel(balanceCmd, inventoryCmd, bankrobCmd, begCmd, cookCmd);

        // main
        registerCmd(helpCmd);
        registerCmd(pingCmd);
        registerCmd(donateCmd);
        registerCmd(reportCmd);
        registerCmd(suggestCmd);

        allSubTopCmds.add(helpCmd);
        allSubTopCmds.add(pingCmd);
        allSubTopCmds.add(donateCmd);
        allSubTopCmds.add(reportCmd);
        allSubTopCmds.add(suggestCmd);

        // currency
        registerCmd(currencyTopLevelCmd);

        allSubTopCmds.add(balanceCmd);
        allSubTopCmds.add(inventoryCmd);
        allSubTopCmds.add(bankrobCmd);
        allSubTopCmds.add(begCmd);
        allSubTopCmds.add(cookCmd);

        cmdsToAdd = commandsMap.values().stream().map(SlashCmd::getCommandData).collect(Collectors.toList());
    }
}
