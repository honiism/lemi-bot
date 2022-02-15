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

package com.honiism.discord.lemi.commands.slash.staff.admins;

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

public class AdminsTopLevel extends SlashCmd {
    
    private  HashMap<Long, Long> delay = new HashMap<>();
    
    private UserBan userBanGroup;
    private ShardRestart shardRestartSubCmd;
    private Embed embedGroup;
    private ResetCurrData resetCurrDataCmd;

    private long timeDelayed;

    public AdminsTopLevel(UserBan userBanGroup, ShardRestart shardRestartGroup, Embed embedGroup,
                          ResetCurrData resetCurrData) {
        this.userBanGroup = userBanGroup;
        this.shardRestartSubCmd = shardRestartGroup;
        this.embedGroup = embedGroup;
        this.resetCurrDataCmd = resetCurrData;

        this.name = "admins";
        this.desc = "Commands for the admins of Lemi the discord bot.";
        this.usage = "/admins ((subcommand group/subcommand))";
        this.category = CommandCategory.ADMINS;
        this.userCategory = UserCategory.ADMINS;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData(this.shardRestartSubCmd.getName(), this.shardRestartSubCmd.getDesc())
                                         .addOptions(this.shardRestartSubCmd.getOptions()),

                                     new SubcommandData(this.resetCurrDataCmd.getName(), this.resetCurrDataCmd.getDesc())
                                         .addOptions(this.resetCurrDataCmd.getOptions())
                                    );
        this.subCmdGroups = Arrays.asList(new SubcommandGroupData(this.userBanGroup.getName(), this.userBanGroup.getDesc())
                                              .addSubcommands(this.userBanGroup.getSubCmds()),

                                          new SubcommandGroupData(this.embedGroup.getName(), this.embedGroup.getDesc())
                                              .addSubcommands(this.embedGroup.getSubCmds())
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
                case "userban":
                    this.userBanGroup.action(event);
                    break;

                case "embed":
                    this.embedGroup.action(event);
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
                    
                case "shardrestart":
                    this.shardRestartSubCmd.action(event);
                    break;

                case "resetcurrdata":
                    this.resetCurrDataCmd.action(event);
            }
        }
    }
}