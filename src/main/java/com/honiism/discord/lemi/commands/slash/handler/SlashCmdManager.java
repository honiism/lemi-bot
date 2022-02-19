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
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
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
import com.honiism.discord.lemi.commands.slash.staff.mods.ViewItems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashCmdManager {

    private static final Logger log = LoggerFactory.getLogger(SlashCmdManager.class);

    private final List<ISlashCmd> registeredCmds;
    private final Map<Long, List<ISlashCmd>> registeredGuildCmds;

    private CommandListUpdateAction commandUpdateAction;

    private static Multimap<CommandCategory, ISlashCmd> cmdsByCategory = ArrayListMultimap.create();
    private List<ISlashCmd> allSlashCmds = new ArrayList<>();

    public SlashCmdManager() {
        registeredCmds = new ArrayList<>();
        registeredGuildCmds = new HashMap<>();
    }

    public void initialize() {
        commandUpdateAction = Lemi.getInstance().getShardManager().getShards().get(0).updateCommands();
        registerAllCmds();
    }

    public void registerAllCmds() {
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
        ViewItems viewItemsCmd = new ViewItems();

        DevTopLevel devTopLevelCmd = new DevTopLevel(modifyAdminsCmd, modifyModsCmd, shutdownCmd, compileCmd, manageItemsCmd);
        AdminsTopLevel adminsTopLevelCmd = new AdminsTopLevel(userBanCmd, shardRestartCmd, embedCmd, resetCurrDataCmd);
        ModsTopLevel modsTopLevelCmd = new ModsTopLevel(testCmd, guildListCmd, shardStatusCmd,
                addCurrProfileCmd, modifyBalCmd, modifyItemCmd, viewItemsCmd);
        CurrencyTopLevel currencyTopLevelCmd = new CurrencyTopLevel(balanceCmd, inventoryCmd, bankrobCmd, begCmd,
                cookCmd);

        // staff
        registerCmd(devTopLevelCmd);
        registerCmd(adminsTopLevelCmd);
        registerCmd(modsTopLevelCmd);

        // main
        registerCmd(helpCmd);
        registerCmd(pingCmd);
        registerCmd(donateCmd);

        // currency
        registerCmd(currencyTopLevelCmd);

        updateCmdCategory();

        allSlashCmds.add(shutdownCmd);
        allSlashCmds.add(helpCmd);
        allSlashCmds.add(modifyAdminsCmd);
        allSlashCmds.add(modifyModsCmd);
        allSlashCmds.add(pingCmd);
        allSlashCmds.add(shardRestartCmd);
        allSlashCmds.add(userBanCmd);
        allSlashCmds.add(testCmd);
        allSlashCmds.add(guildListCmd);
        allSlashCmds.add(compileCmd);
        allSlashCmds.add(shardStatusCmd);
        allSlashCmds.add(donateCmd);
        allSlashCmds.add(embedCmd);
        allSlashCmds.add(addCurrProfileCmd);
        allSlashCmds.add(balanceCmd);
        allSlashCmds.add(modifyBalCmd);
        allSlashCmds.add(inventoryCmd);
        allSlashCmds.add(modifyItemCmd);
        allSlashCmds.add(resetCurrDataCmd);
        allSlashCmds.add(bankrobCmd);
        allSlashCmds.add(begCmd);
        allSlashCmds.add(cookCmd);
        allSlashCmds.add(manageItemsCmd);
        allSlashCmds.add(viewItemsCmd);
        allSlashCmds.add(devTopLevelCmd);
        allSlashCmds.add(adminsTopLevelCmd);
        allSlashCmds.add(modsTopLevelCmd);
        allSlashCmds.add(currencyTopLevelCmd);
    }

    private void registerCmd(ISlashCmd cmd) {
        if (!cmd.isGlobal() && !Lemi.getInstance().isDebug()) {
            for (long guildId : Lemi.getInstance().getWhitelistedUsers()) {
                Guild guild = Lemi.getInstance().getShardManager().getGuildById(guildId);

                if (guild == null) {
                    return;
                }

                List<ISlashCmd> alreadyRegistered = registeredGuildCmds.containsKey(guildId) ?
                        registeredGuildCmds.get(guildId) : new ArrayList<>();

                alreadyRegistered.add(cmd);
                registeredGuildCmds.put(guildId, alreadyRegistered);
            }
            return;
        }

        if (Lemi.getInstance().isDebug()) {
            Guild honeysSweetsGuild = Lemi.getInstance().getShardManager().getGuildById(Config.getLong("honeys_sweets_id"));

            if (honeysSweetsGuild != null) {
                List<ISlashCmd> alreadyRegistered = registeredGuildCmds.containsKey(Config.getLong("honeys_sweets_id")) ?
                        registeredGuildCmds.get(Config.getLong("honeys_sweets_id")) : new ArrayList<>();
                
                alreadyRegistered.add(cmd);
                registeredGuildCmds.put(Config.getLong("honeys_sweets_id"), alreadyRegistered);
            }
            return;
         }

        commandUpdateAction.addCommands(cmd.getCommandData());
        registeredCmds.add(cmd);
    }

    public void updateCmds(Consumer<List<Command>> success, Consumer<Throwable> failure) {
        if (!Lemi.getInstance().isDebug()) {
            commandUpdateAction.queue(success, failure);

            for (Map.Entry<Long, List<ISlashCmd>> entrySet : registeredGuildCmds.entrySet()) {
                Long guildId = entrySet.getKey();
                List<ISlashCmd> slashCmds = entrySet.getValue();
                Guild guild = Lemi.getInstance().getShardManager().getGuildById(guildId);

                if (guildId == null || slashCmds == null || slashCmds.isEmpty() || guild == null) {
                    continue;
                }

                CommandListUpdateAction guildCommandUpdateAction = guild.updateCommands();

                for (ISlashCmd cmd : slashCmds) {
                    guildCommandUpdateAction = guildCommandUpdateAction.addCommands(cmd.getCommandData());
                }

                if (slashCmds.size() > 0) {
                     guildCommandUpdateAction.queue();
                }
            }
        } else {
            List<ISlashCmd> honeysSweetsCmds = registeredGuildCmds.get(Config.getLong("honeys_sweets_id"));

            if ((honeysSweetsCmds != null && !honeysSweetsCmds.isEmpty())) {
                Guild honeysSweetsGuild = Lemi.getInstance().getShardManager().getGuildById(Config.getLong("honeys_sweets_id"));

                if (honeysSweetsGuild == null) {
                    return;
                }

                CommandListUpdateAction honeysSweetsUpdateAction = honeysSweetsGuild.updateCommands();

                for (ISlashCmd cmd : honeysSweetsCmds) {
                    honeysSweetsUpdateAction.addCommands(cmd.getCommandData());
                }

                honeysSweetsUpdateAction.queue(success, failure);
            }
        }
    }

    private void updateCmdCategory() {
        for (ISlashCmd cmd : registeredCmds) {
            cmdsByCategory.put(cmd.getCategory(), cmd);
        }
        log.info("Updated all commands according to it's categories.");
    }

    public List<ISlashCmd> getAllCmds() {
        return allSlashCmds;
    }

    public List<ISlashCmd> getRegisteredCmds() {
        return registeredCmds;
    }

    public Map<Long, List<ISlashCmd>> getRegisteredGuildCmds() {
        return registeredGuildCmds;
    }

    public ISlashCmd getCmdByName(String name) {
        for (ISlashCmd cmd : getAllCmds()) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    public Collection<ISlashCmd> getCmdByCategory(CommandCategory category) {
        if (cmdsByCategory.get(category) == null) {
            return null;
        }
        return cmdsByCategory.get(category);
    }

    public List<String> getCmdNamesByCategory(Collection<ISlashCmd> cmdsByCategory) {
        List<String> cmdNames = new ArrayList<>();

        if (cmdsByCategory.isEmpty()) {
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
        event.deferReply().queue();

        Member member = event.getMember();

        if (!event.isFromGuild() || member.getUser().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        ISlashCmd command = null;

        if (registeredGuildCmds.containsKey(guild.getIdLong())) {
            List<ISlashCmd> guildCmds = registeredGuildCmds.get(guild.getIdLong());

            ISlashCmd guildCmd = guildCmds.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getName()))
                .findFirst()
                .orElse(null);

            if (guildCmd != null) {
                command = guildCmd;
            }
        }

        if (command == null) {
            ISlashCmd globalCmd = getRegisteredCmds()
                .stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getName()))
                .findFirst()
                .orElse(null);

            if (globalCmd != null) {
                command = globalCmd;
            }
        }

        if (command != null) {
            command.executeAction(event);
        }
    }
}
