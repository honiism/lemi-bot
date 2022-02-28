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

package com.honiism.discord.lemi.commands.slash.staff.dev;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class DevTopLevel extends SlashCmd {

    private ModifyAdmins modifyAdminsGroup;
    private ModifyMods modifyModsGroup;
    private Shutdown shutdownCmd;
    private Compile compileCmd;
    private ManageItems manageItemsGroup;

    public DevTopLevel(ModifyAdmins modifyAdminsGroup, ModifyMods modifyModsGroup, Shutdown shutdownCmd,
                       Compile compileCmd, ManageItems manageItemsGroup) {
        this.compileCmd = compileCmd;
        this.modifyModsGroup = modifyModsGroup;
        this.modifyAdminsGroup = modifyAdminsGroup;
        this.shutdownCmd = shutdownCmd;
        this.manageItemsGroup = manageItemsGroup;

        setCommandData(Commands.slash("dev", "Commands for the developer of Lemi the discord bot.")
                .addSubcommands(
                        new SubcommandData(this.shutdownCmd.getName(), this.shutdownCmd.getDesc())
                                .addOptions(this.shutdownCmd.getOptions()),
                                     
                        new SubcommandData(this.compileCmd.getName(), this.compileCmd.getDesc())
                                .addOptions(this.compileCmd.getOptions())
                )
                .addSubcommandGroups(
                        new SubcommandGroupData(this.modifyAdminsGroup.getName(), this.modifyAdminsGroup.getDesc())
                                .addSubcommands(this.modifyAdminsGroup.getSubCmds()),

                        new SubcommandGroupData(this.modifyModsGroup.getName(), this.modifyModsGroup.getDesc())
                                .addSubcommands(this.modifyModsGroup.getSubCmds()),

                        new SubcommandGroupData(this.manageItemsGroup.getName(), this.manageItemsGroup.getDesc())
                                .addSubcommands(this.manageItemsGroup.getSubCmds())
                )
                .setDefaultEnabled(false)
        );

        setUsage("/dev ((subcommand groups/subcommands))");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        String subCmdGroupName = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        if (subCmdGroupName != null) {
            switch (subCmdGroupName) {
                case "modifyadmins":
                    this.modifyAdminsGroup.action(event);
                    break;

                case "modifymods":
                    this.modifyModsGroup.action(event);
                    break;

                case "manageitems":
                    this.manageItemsGroup.action(event);
            }
            
        } else {
            switch (subCmdName) {
                case "shutdown":
                    this.shutdownCmd.action(event);
                    break;

                case "compile":
                    this.compileCmd.action(event);
            }
        }
    }
}