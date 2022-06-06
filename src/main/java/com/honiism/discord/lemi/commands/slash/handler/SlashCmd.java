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

package com.honiism.discord.lemi.commands.slash.handler;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.data.currency.UserDataManager;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public abstract class SlashCmd {

    private SlashCommandData commandData;
    private String usage = "";
    private CommandCategory category = CommandCategory.MAIN;
    private UserCategory userCategory = UserCategory.USERS;
    private Permission[] userPermissions = new Permission[0];
    private Permission[] botPermissions = new Permission[0];
    private UserDataManager userDataManager;
    private boolean whitelistOnly = false;
    private List<Long> customWhitelist = new ArrayList<>();

    public void preAction(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (isWhitelistOnly()) {
            if (!getCustomWhitelist().isEmpty() && !getCustomWhitelist().contains(guild.getIdLong())) {
                return;
            }

            if (getCustomWhitelist().isEmpty() && !Lemi.getInstance().getWhitelisted().contains(guild.getIdLong())) {
                return;
            }
        }

        if (getUserPerms().length > 0 && !member.hasPermission(getUserPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n" + ":sunflower:" + member.getAsMention() + "\r\n"
                        + "> You don't have the " + getUserPermsString())
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(0xffd1dc);
            
            event.replyEmbeds(needUserPermsMsg.build()).queue();
            return;
        }

        if (getBotPerms().length > 0 
                && !guild.getSelfMember().hasPermission(getBotPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n" + member.getAsMention() + "\r\n" 
                        + "> I don't have the " + getBotPermsString())
                .setThumbnail(guild.getSelfMember().getUser().getEffectiveAvatarUrl())
                .setColor(0xffd1dc);

            event.replyEmbeds(needUserPermsMsg.build()).queue();
            return;
        }

        try {
            action(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }      
    }

    public abstract void action(SlashCommandInteractionEvent event) throws JsonMappingException, JsonProcessingException;

    public List<Long> getCustomWhitelist() {
        return customWhitelist;
    }

    public void addCustomWhitelist(long... guildIds) {
        for (long guildId : guildIds) {
            getCustomWhitelist().add(guildId);
        }
    }

    public boolean isWhitelistOnly() {
        return whitelistOnly;
    }

    public void setWhitelist(boolean whitelistOnly) {
        this.whitelistOnly = whitelistOnly;
    }

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public void setUserDataManager(long userId) throws JsonProcessingException {
        this.userDataManager = new UserDataManager(userId, LemiDbBalManager.INS.getUserData(userId));
    }

    public SlashCommandData getCommandData() {
        return commandData;    
    }

    public void setCommandData(SlashCommandData commandData) {
        this.commandData = commandData;
    }

    public String getName() {
        return getCommandData().getName();
    }

    public void setCategory(CommandCategory category) {
        this.category = category;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public void setUserCategory(UserCategory userCategory) {
        this.userCategory = userCategory;
    }

    public UserCategory getUserCategory() {
        return userCategory;
    }

    public String getCategoryString() {
        return getCategory().toString();
    }

    public String getUserCategoryString() {
        return getUserCategory().toString();
    }

    public String getUserPermsString() {
        if (userPermissions.length == 0) {
            return "No user permissions needed.";
        }
        return Tools.parsePerms(userPermissions)
                + (userPermissions.length > 1 ? " permissions" : " permission");
    }

    public String getBotPermsString() {
        if (botPermissions.length == 0) {
            return "No bot permissions needed.";
        }
        return Tools.parsePerms(botPermissions)
                + (botPermissions.length > 1 ? " permissions" : " permission");
    }

    public void setUserPerms(Permission[] userPermissions) {
        this.userPermissions = userPermissions;
    }

    public Permission[] getUserPerms() {
        return (userPermissions.length == 0 ? null : userPermissions);
    }

    public void setBotPerms(Permission[] botPermissions) {
        this.botPermissions = botPermissions;
    }

    public Permission[] getBotPerms() {
        return (botPermissions.length == 0 ? null : botPermissions);
    }

    public String getDesc() {
        return getCommandData().getDescription();
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    public List<OptionData> getOptions() {
        return getCommandData().getOptions();
    }

    public List<SubcommandData> getSubCmds() {
        return getCommandData().getSubcommands();
    }

    public List<SubcommandGroupData> getSubCmdGroups() {
        return getCommandData().getSubcommandGroups();
    }

    public MessageEmbed getHelp(SlashCommandInteractionEvent event) {
        SlashCmdManager slashCmdManagerIns = Lemi.getInstance().getSlashCmdManager();

        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("‧₊੭ :cherries: **HELP GUIDE** ♡ ⋆｡˚\r\n"
                    + "\r\n˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
            .addField(":sunflower: **Name**" + " !! ", "`" + getName() + "`", false)
            .addField(":crescent_moon: **Description**" + " !! ", "`" + getDesc() + "`", false)
            .addField(":seedling: **Usage**" + " !! ", "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":butterfly: **Other usages**" + " !! ",
                    "`" + String.join(", ", slashCmdManagerIns.getCmdNamesByCategory(
                            slashCmdManagerIns.getCmdByCategory(getCategory()))) 
                    + "`", false)
            .addField(":cherry_blossom: **Category**" + " !! ", "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + " !! ", "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + " !! ",
                    "`" + getUserPermsString() + "`", false)
            .addField(":cake: **Bot permissions needed**" + " !! ",
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed.build();
    }

    public EmbedBuilder getHelpBuilder(SlashCommandInteractionEvent event) {
        SlashCmdManager slashCmdManagerIns = Lemi.getInstance().getSlashCmdManager();
        
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("‧₊੭ :cherries: **HELP GUIDE** ♡ ⋆｡˚\r\n"
                    + "\r\n˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
            .addField(":sunflower: **Name**" + " !! ", "`" + getName() + "`", false)
            .addField(":crescent_moon: **Description**" + " !! ", "`" + getDesc() + "`", false)
            .addField(":seedling: **Usage**" + " !! ", "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":butterfly: **Other usages**" + " !! ",
                    "`" + String.join(", ", slashCmdManagerIns.getCmdNamesByCategory(
                            slashCmdManagerIns.getCmdByCategory(getCategory()))) 
                    + "`", false)
            .addField(":cherry_blossom: **Category**" + " !! ", "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + " !! ", "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + " !! ",
                    "`" + getUserPermsString() + "`", false)
            .addField(":cake: **Bot permissions needed**" + " !! ",
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed;
    }
}