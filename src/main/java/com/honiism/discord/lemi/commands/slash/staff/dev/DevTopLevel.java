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

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class DevTopLevel extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();

    private ModifyAdmins modifyAdminsGroup;
    private ModifyMods modifyModsGroup;
    private Shutdown shutdownCmd;
    private Compile compileCmd;
    private ManageItems manageItemsGroup;

    private long timeDelayed;

    public DevTopLevel(ModifyAdmins modifyAdminsGroup, ModifyMods modifyModsGroup, Shutdown shutdownCmd,
                       Compile compileCmd, ManageItems manageItemsGroup) {
        this.compileCmd = compileCmd;
        this.modifyModsGroup = modifyModsGroup;
        this.modifyAdminsGroup = modifyAdminsGroup;
        this.shutdownCmd = shutdownCmd;
        this.manageItemsGroup = manageItemsGroup;

        this.name = "dev";
        this.desc = "Commands for the developer of Lemi the discord bot.";
        this.usage = "/dev ((subcommand group/subcommand))";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.DEV;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData(this.shutdownCmd.getName(), this.shutdownCmd.getDesc())
                                         .addOptions(this.shutdownCmd.getOptions()),
                                     
                                     new SubcommandData(this.compileCmd.getName(), this.compileCmd.getDesc())
                                         .addOptions(this.compileCmd.getOptions())
                                    );
        this.subCmdGroups = Arrays.asList(new SubcommandGroupData(this.modifyAdminsGroup.getName(), this.modifyAdminsGroup.getDesc())
                                              .addSubcommands(this.modifyAdminsGroup.getSubCmds()),

                                          new SubcommandGroupData(this.modifyModsGroup.getName(), this.modifyModsGroup.getDesc())
                                              .addSubcommands(this.modifyModsGroup.getSubCmds()),

                                          new SubcommandGroupData(this.manageItemsGroup.getName(), this.manageItemsGroup.getDesc())
                                              .addSubcommands(this.manageItemsGroup.getSubCmds())
                                         );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        String subCmdGroupName = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        if (subCmdGroupName != null) {
            switch (subCmdName) {
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

                case "shutdown":
                    this.shutdownCmd.action(event);
                    break;

                case "compile":
                    this.compileCmd.action(event);
                    break;
            }
        }
    }
}