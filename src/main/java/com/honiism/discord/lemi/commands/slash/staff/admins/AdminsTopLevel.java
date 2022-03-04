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

package com.honiism.discord.lemi.commands.slash.staff.admins;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class AdminsTopLevel extends SlashCmd {
    
    private UserBan userBanGroup;
    private ShardRestart shardRestartSubCmd;
    private Embed embedGroup;
    private ResetCurrData resetCurrDataCmd;
    private Announce announceCmd;

    public AdminsTopLevel(UserBan userBanGroup, ShardRestart shardRestartGroup, Embed embedGroup,
                          ResetCurrData resetCurrDataCmd, Announce announceCmd) {
        this.userBanGroup = userBanGroup;
        this.shardRestartSubCmd = shardRestartGroup;
        this.embedGroup = embedGroup;
        this.resetCurrDataCmd = resetCurrDataCmd;
        this.announceCmd = announceCmd;

        setCommandData(Commands.slash("admins", "Commands for the admins of Lemi the discord bot.")
                .addSubcommands(
                        new SubcommandData(this.shardRestartSubCmd.getName(), this.shardRestartSubCmd.getDesc())
                                .addOptions(this.shardRestartSubCmd.getOptions()),

                        new SubcommandData(this.resetCurrDataCmd.getName(), this.resetCurrDataCmd.getDesc())
                                .addOptions(this.resetCurrDataCmd.getOptions()),

                        new SubcommandData(this.announceCmd.getName(), this.announceCmd.getDesc())
                                .addOptions(this.announceCmd.getOptions())
                )
                .addSubcommandGroups(
                        new SubcommandGroupData(this.userBanGroup.getName(), this.userBanGroup.getDesc())
                                .addSubcommands(this.userBanGroup.getSubCmds()),

                        new SubcommandGroupData(this.embedGroup.getName(), this.embedGroup.getDesc())
                                .addSubcommands(this.embedGroup.getSubCmds())
                )
                .setDefaultEnabled(false)
        );

        setUsage("/admins ((subcommand groups/subcommands))");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        String subCmdGroupName = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        if (subCmdGroupName != null) {
            switch (subCmdGroupName) {
                case "userban":
                    this.userBanGroup.action(event);
                    break;

                case "embed":
                    this.embedGroup.action(event);
            }
        } else {
            switch (subCmdName) {
                case "shardrestart":
                    this.shardRestartSubCmd.action(event);
                    break;

                case "resetcurrdata":
                    this.resetCurrDataCmd.action(event);
                    break;

                case "announce":
                    this.announceCmd.action(event);
            }
        }
    }
}