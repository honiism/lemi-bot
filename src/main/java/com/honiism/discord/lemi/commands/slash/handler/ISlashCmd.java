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