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

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public interface ISlashCmd {
    
    void executeAction(SlashCommandInteractionEvent event);
    String getName();
    String getDesc();
    CommandCategory getCategory();
    UserCategory getUserCategory();
    String getCategoryString();
    String getUserCategoryString();
    Permission[] getUserPerms();
    Permission[] getBotPerms();
    String getUserPermsString();
    String getBotPermsString();
    String getUsage();
    List<OptionData> getOptions();
    List<SubcommandData> getSubCmds();
    List<SubcommandGroupData> getSubCmdGroups();
    MessageEmbed getHelp(SlashCommandInteractionEvent event);
    EmbedBuilder getHelpBuilder(SlashCommandInteractionEvent event);
}