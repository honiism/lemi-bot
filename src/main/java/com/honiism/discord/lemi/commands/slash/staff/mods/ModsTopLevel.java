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
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class ModsTopLevel extends SlashCmd {
    
    private Test testSubCmd;
    private GuildList guildListCmd;
    private ShardStatus shardStatusCmd;
    private AddCurrProfile addCurrProfileCmd;
    private ModifyBal modifyBalGroup;
    private ModifyItem modifyItemGroup;
    private ViewItems viewItemsCmd;

    public ModsTopLevel(Test testSubCmd, GuildList guildListCmd, ShardStatus shardStatusCmd,
                        AddCurrProfile addCurrProfileCmd, ModifyBal modifyBalGroup,
                        ModifyItem modifyItemGroup, ViewItems viewItemsCmd) {
        this.shardStatusCmd = shardStatusCmd;
        this.testSubCmd = testSubCmd;
        this.guildListCmd = guildListCmd;
        this.addCurrProfileCmd = addCurrProfileCmd;
        this.modifyBalGroup = modifyBalGroup;
        this.modifyItemGroup = modifyItemGroup;
        this.viewItemsCmd = viewItemsCmd;

        setCommandData(Commands.slash("mods", "Commands for the moderators of Lemi the discord bot.")
                .addSubcommands(
                        new SubcommandData(this.testSubCmd.getName(), this.testSubCmd.getDesc()),

                        new SubcommandData(this.guildListCmd.getName(), this.guildListCmd.getDesc())
                                .addOptions(this.guildListCmd.getOptions()),
                                     
                        new SubcommandData(this.shardStatusCmd.getName(), this.shardStatusCmd.getDesc())
                                .addOptions(this.shardStatusCmd.getOptions()),

                        new SubcommandData(this.addCurrProfileCmd.getName(), this.addCurrProfileCmd.getDesc())
                                .addOptions(this.addCurrProfileCmd.getOptions()),

                        new SubcommandData(this.viewItemsCmd.getName(), this.viewItemsCmd.getDesc())
                                .addOptions(this.viewItemsCmd.getOptions())
                )
                .addSubcommandGroups(
                        new SubcommandGroupData(this.modifyBalGroup.getName(), this.modifyBalGroup.getDesc())
                                .addSubcommands(this.modifyBalGroup.getSubCmds()),

                        new SubcommandGroupData(this.modifyItemGroup.getName(), this.modifyItemGroup.getDesc())
                                .addSubcommands(this.modifyItemGroup.getSubCmds())
                )
        );

        setUsage("/mods ((subcommand groups/subcommands))");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setGlobal(true);
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        String subCmdGroupName = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        if (subCmdGroupName != null) {
            switch (subCmdGroupName) {
                case "modifybal":
                    this.modifyBalGroup.action(event);
                    break;

                case "modifyitem":
                    this.modifyItemGroup.action(event);
            }
        } else {
            switch (subCmdName) {
                case "test":
                    this.testSubCmd.action(event);
                    break;

                case "guildlist":
                    this.guildListCmd.action(event);
                    break;

                case "shardstatus":
                    this.shardStatusCmd.action(event);
                    break;

                case "addcurrprofile":
                    this.addCurrProfileCmd.action(event);
                    break;

                case "viewitems":
                    this.viewItemsCmd.action(event);
            }
        }
    }    
}