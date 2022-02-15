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

package com.honiism.discord.lemi.commands.slash.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.commands.slash.staff.admins.Embed;
import com.honiism.discord.lemi.commands.slash.staff.admins.ResetCurrData;
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
import com.honiism.discord.lemi.commands.slash.staff.admins.AdminsTopLevel;
import com.honiism.discord.lemi.commands.slash.staff.admins.ShardRestart;
import com.honiism.discord.lemi.commands.slash.staff.admins.UserBan;
import com.honiism.discord.lemi.commands.slash.staff.dev.Compile;
import com.honiism.discord.lemi.commands.slash.staff.dev.DevTopLevel;
import com.honiism.discord.lemi.commands.slash.staff.dev.ManageItems;
import com.honiism.discord.lemi.commands.slash.staff.dev.ModifyAdmins;
import com.honiism.discord.lemi.commands.slash.staff.dev.ModifyMods;
import com.honiism.discord.lemi.commands.slash.staff.dev.Shutdown;
import com.honiism.discord.lemi.commands.slash.staff.mods.AddCurrProfile;
import com.honiism.discord.lemi.commands.slash.staff.mods.GuildList;
import com.honiism.discord.lemi.commands.slash.staff.mods.ModifyBal;
import com.honiism.discord.lemi.commands.slash.staff.mods.ModifyItem;
import com.honiism.discord.lemi.commands.slash.staff.mods.ModsTopLevel;
import com.honiism.discord.lemi.commands.slash.staff.mods.ShardStatus;
import com.honiism.discord.lemi.commands.slash.staff.mods.Test;
import com.honiism.discord.lemi.listeners.BaseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashCmdManager {

    private static final Logger log = LoggerFactory.getLogger(SlashCmdManager.class);
    private static Multimap<CommandCategory, ISlashCmd> cmdsByCategory = ArrayListMultimap.create();
    private Map<String, ISlashCmd> commandsMap = new HashMap<>();

    public SlashCmdManager() {
        Shutdown shutdownCmd = new Shutdown();
        Help helpCmd = new Help();
        ModifyAdmins modifyAdminsCmd = new ModifyAdmins();
        ModifyMods modifyModsCmd = new ModifyMods();
        Ping pingCmd = new Ping();
        ShardRestart shardRestartCmd = new ShardRestart();
        UserBan userBanCmd = new UserBan();
        Test testCmd = new Test();
        GuildList guildListCmd = new GuildList();
        Compile compileCmd = new Compile();
        ShardStatus shardStatusCmd = new ShardStatus();
        Donate donateCmd = new Donate();
        Embed embedCmd = new Embed();
        AddCurrProfile addCurrProfileCmd = new AddCurrProfile();
        Balance balanceCmd = new Balance();
        ModifyBal modifyBalCmd = new ModifyBal();
        Inventory inventoryCmd = new Inventory();
        ModifyItem modifyItemCmd = new ModifyItem();
        ResetCurrData resetCurrDataCmd = new ResetCurrData();
        Bankrob bankrobCmd = new Bankrob();
        Beg begCmd = new Beg();
        Cook cookCmd = new Cook();
        ManageItems manageItemsCmd = new ManageItems();

        DevTopLevel devTopLevelCmd = new DevTopLevel(modifyAdminsCmd, modifyModsCmd, shutdownCmd, compileCmd, manageItemsCmd);
        AdminsTopLevel adminsTopLevelCmd = new AdminsTopLevel(userBanCmd, shardRestartCmd, embedCmd, resetCurrDataCmd);
        ModsTopLevel modsTopLevelCmd = new ModsTopLevel(testCmd, guildListCmd, shardStatusCmd,
                addCurrProfileCmd, modifyBalCmd, modifyItemCmd);
        CurrencyTopLevel currencyTopLevelCmd = new CurrencyTopLevel(balanceCmd, inventoryCmd, bankrobCmd, begCmd,
                cookCmd);

        // staff
        addSlashCmd(devTopLevelCmd);
        addSlashCmd(adminsTopLevelCmd);
        addSlashCmd(modsTopLevelCmd);

        // main
        addSlashCmd(helpCmd);
        addSlashCmd(pingCmd);
        addSlashCmd(donateCmd);

        // currency
        addSlashCmd(currencyTopLevelCmd);

        updateCmd(BaseListener.getJDA());

        CommandListUpdateAction commands = BaseListener.getJDA().getShardManager()
            .getGuildById(Config.get("honeys_sweets_id"))
            .updateCommands();

        commands.addCommands(
                // needs privileges
                Commands.slash(devTopLevelCmd.getName(), devTopLevelCmd.getDesc())
                    .addSubcommandGroups(devTopLevelCmd.getSubCmdGroups())
                    .addSubcommands(devTopLevelCmd.getSubCmds())
                    .setDefaultEnabled(false),

                Commands.slash(adminsTopLevelCmd.getName(), adminsTopLevelCmd.getDesc())
                    .addSubcommandGroups(adminsTopLevelCmd.getSubCmdGroups())
                    .addSubcommands(adminsTopLevelCmd.getSubCmds())
                    .setDefaultEnabled(false),

                Commands.slash(modsTopLevelCmd.getName(), modsTopLevelCmd.getDesc())
                    .addSubcommandGroups(modsTopLevelCmd.getSubCmdGroups())
                    .addSubcommands(modsTopLevelCmd.getSubCmds())
                    .setDefaultEnabled(false),

                // doesn't need privileges
                Commands.slash(currencyTopLevelCmd.getName(), currencyTopLevelCmd.getDesc())
                    .addSubcommandGroups(currencyTopLevelCmd.getSubCmdGroups())
                    .addSubcommands(currencyTopLevelCmd.getSubCmds()),

                Commands.slash(helpCmd.getName(), helpCmd.getDesc())
                    .addOptions(helpCmd.getOptions()),
                Commands.slash(pingCmd.getName(), pingCmd.getDesc())
                    .addOptions(pingCmd.getOptions()),
                Commands.slash(donateCmd.getName(), donateCmd.getDesc())
                    .addOptions(donateCmd.getOptions())
        );

        Guild honeysSweetsGuild = BaseListener.getJDA().getShardManager().getGuildById(Config.get("honeys_sweets_id"));
        Role adminRole = honeysSweetsGuild.getRoleById(Config.get("admin_role_id"));
        Role modsRole = honeysSweetsGuild.getRoleById(Config.get("mod_role_id"));
        Role twitchModsRole = honeysSweetsGuild.getRoleById(Config.get("twitch_mod_role_id"));

        commands.queue((cmds) -> {
            cmds.forEach((cmd) -> {
                if (cmd.getName().equals(devTopLevelCmd.getName())) {
                    honeysSweetsGuild.retrieveMemberById(Config.get("dev_id"))
                        .queue(
                            (dev) -> {
                                Collection<CommandPrivilege> privileges = new ArrayList<>();

                                privileges.add(CommandPrivilege.enable(dev.getUser()));
                                cmd.updatePrivileges(honeysSweetsGuild, privileges).queue();
                            }
                        );
                } else if (cmd.getName().equals(adminsTopLevelCmd.getName())) {
                    honeysSweetsGuild.retrieveMemberById(Config.get("dev_id"))
                        .queue(
                            (dev) -> {
                                Collection<CommandPrivilege> privileges = new ArrayList<>();

                                privileges.add(CommandPrivilege.enable(dev.getUser()));
                                privileges.add(CommandPrivilege.enable(adminRole));
                                
                                cmd.updatePrivileges(honeysSweetsGuild, privileges).queue();
                            }
                        );
                } else if (cmd.getName().equals(modsTopLevelCmd.getName())) {
                    honeysSweetsGuild.retrieveMemberById(Config.get("dev_id"))
                        .queue(
                            (dev) -> {
                                Collection<CommandPrivilege> privileges = new ArrayList<>();

                                privileges.add(CommandPrivilege.enable(dev.getUser()));
                                privileges.add(CommandPrivilege.enable(adminRole));
                                privileges.add(CommandPrivilege.enable(modsRole));
                                privileges.add(CommandPrivilege.enable(twitchModsRole));
                                cmd.updatePrivileges(honeysSweetsGuild, privileges).queue();
                            }
                        );
                }
            });
        });
    }

    private void addSlashCmd(ISlashCmd cmd) {
        if (commandsMap.containsKey(cmd.getName())) {
            return;
        }
        commandsMap.put(cmd.getName(), cmd);
    }

    public void updateCmd(JDA jda) {
        for (ISlashCmd cmd : commandsMap.values()) {
            cmdsByCategory.put(cmd.getCategory(), cmd);
        }
        
        log.info("Updated all commands according to it's categories.");

        jda.getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                .getTextChannelById(Config.get("logs_channel_id"))
                .sendMessage("Updated all commands according to it's categories.")
                .queue();
    }

    public static Collection<ISlashCmd> getCmdByCategory(CommandCategory category) {
        if (cmdsByCategory.get(category) == null) {
            return null;
        }
        return cmdsByCategory.get(category);
    }

    public static List<String> getCmdNames(Collection<ISlashCmd> commandsList) {
        List<String> cmdNames = new ArrayList<>();

        if (commandsList.isEmpty()) {
            cmdNames.add("No commands for this category yet.");
            return cmdNames;
        }

        commandsList.forEach(cmd -> {
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
        ISlashCmd slashCmd = commandsMap.get(executedCmdName);

        if (slashCmd != null) {
            slashCmd.executeAction(event);
        }
    }
}
