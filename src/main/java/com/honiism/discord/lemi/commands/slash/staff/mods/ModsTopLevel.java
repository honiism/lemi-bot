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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class ModsTopLevel extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    
    private Test testSubCmd;
    private GuildList guildListCmd;
    private ShardStatus shardStatusCmd;
    private AddCurrProfile addCurrProfileCmd;
    private ModifyBal modifyBalGroup;
    private ModifyItem modifyItemGroup;
    private ViewItems viewItemsCmd;

    private long timeDelayed;

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

        this.name = "mods";
        this.desc = "Commands for the moderators of Lemi the discord bot.";
        this.usage = "/mods ((subcommand group/subcommand))";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData(this.testSubCmd.getName(), this.testSubCmd.getDesc())
                                         .addOptions(this.testSubCmd.getOptions()),

                                     new SubcommandData(this.guildListCmd.getName(), this.guildListCmd.getDesc())
                                         .addOptions(this.guildListCmd.getOptions()),
                                     
                                     new SubcommandData(this.shardStatusCmd.getName(), this.shardStatusCmd.getDesc())
                                         .addOptions(this.shardStatusCmd.getOptions()),

                                     new SubcommandData(this.addCurrProfileCmd.getName(), this.addCurrProfileCmd.getDesc())
                                         .addOptions(this.addCurrProfileCmd.getOptions()),

                                     new SubcommandData(this.viewItemsCmd.getName(), this.viewItemsCmd.getDesc())
                                         .addOptions(this.viewItemsCmd.getOptions())
                                    );
        this.subCmdGroups = Arrays.asList(new SubcommandGroupData(this.modifyBalGroup.getName(), this.modifyBalGroup.getDesc())
                                              .addSubcommands(this.modifyBalGroup.getSubCmds()),

                                          new SubcommandGroupData(this.modifyItemGroup.getName(), this.modifyItemGroup.getDesc())
                                              .addSubcommands(this.modifyItemGroup.getSubCmds())
                                         );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
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