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

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public abstract class SlashCmd implements ISlashCmd {

    private SlashCommandData commandData;
    private boolean isGlobal = true;
    private String usage = "";
    private CommandCategory category = CommandCategory.MAIN;
    private UserCategory userCategory = UserCategory.USERS;
    private Permission[] userPermissions = new Permission[0];
    private Permission[] botPermissions = new Permission[0];

    @Override
    public void executeAction(SlashCommandInteractionEvent event) {
        LemiDbManager.INS.checkIfBanned(event);
        checkPerms(event);
        action(event);      
    }

    public abstract void action(SlashCommandInteractionEvent event);
    
    public void checkPerms(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();

        if (getUserCategory().equals(UserCategory.DEV) 
                && !Tools.isAuthorDev(event.getMember())) {
            return;
        }

        if (getUserCategory().equals(UserCategory.ADMINS) 
                && !Tools.isAuthorAdmin(event.getMember(), event)) {
            return;
        }

        if (getUserCategory().equals(UserCategory.MODS) 
                && !Tools.isAuthorMod(event.getMember(), event)) {
            return;
        }

        if (getUserPerms().length > 0 && !event.getMember().hasPermission(getUserPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n" + ":sunflower:" + event.getMember().getAsMention() + "\r\n"
                        + "> You don't have the " + getUserPermsString())
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(0xffd1dc);
            
            hook.sendMessageEmbeds(needUserPermsMsg.build()).queue();
            return;
        }

        if (getBotPerms().length > 0 
                && !event.getGuild().getSelfMember().hasPermission(getBotPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n" + event.getMember().getAsMention() + "\r\n" 
                        + getBotPermsString())
                .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
                .setColor(0xffd1dc);

            hook.sendMessageEmbeds(needUserPermsMsg.build()).queue();
            return;
        }
    }

    @Override
    public SlashCommandData getCommandData() {
        return commandData;    
    }

    @Override
    public void setCommandData(SlashCommandData commandData) {
        this.commandData = commandData;
    }

    @Override
    public String getName() {
        return getCommandData().getName();
    }

    @Override
    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public void setCategory(CommandCategory category) {
        this.category = category;
    }

    @Override
    public CommandCategory getCategory() {
        return category;
    }

    @Override
    public void setUserCategory(UserCategory userCategory) {
        this.userCategory = userCategory;
    }

    @Override
    public UserCategory getUserCategory() {
        return userCategory;
    }

    @Override
    public String getCategoryString() {
        return getCategory().toString();
    }

    @Override
    public String getUserCategoryString() {
        return getUserCategory().toString();
    }

    @Override
    public String getUserPermsString() {
        if (userPermissions.length == 0) {
            return "No user permissions needed.";
        }
        return Tools.parsePerms(userPermissions)
                + (userPermissions.length > 1 ? " permissions" : " permission");
    }

    @Override
    public String getBotPermsString() {
        if (botPermissions.length == 0) {
            return "No bot permissions needed.";
        }
        return Tools.parsePerms(botPermissions)
        + (botPermissions.length > 1 ? " permissions" : " permission");
    }

    @Override
    public void setUserPerms(Permission[] userPermissions) {
        this.userPermissions = userPermissions;
    }

    @Override
    public Permission[] getUserPerms() {
        return (userPermissions.length == 0 ? null : userPermissions);
    }

    @Override
    public void setBotPerms(Permission[] botPermissions) {
        this.botPermissions = botPermissions;
    }

    @Override
    public Permission[] getBotPerms() {
        return (botPermissions.length == 0 ? null : botPermissions);
    }

    @Override
    public String getDesc() {
        return getCommandData().getDescription();
    }

    @Override
    public void setUsage(String usage) {
        this.usage = usage;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public List<OptionData> getOptions() {
        return getCommandData().getOptions();
    }

    @Override
    public List<SubcommandData> getSubCmds() {
        return getCommandData().getSubcommands();
    }

    @Override
    public List<SubcommandGroupData> getSubCmdGroups() {
        return getCommandData().getSubcommandGroups();
    }

    @Override
    public MessageEmbed getHelp(SlashCommandInteractionEvent event) {
        SlashCmdManager slashCmdManagerIns = Lemi.getInstance().getSlashCmdManager();

        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("‧₊੭ :cherries: **HELP GUIDE** ♡ ⋆｡˚\r\n"
                    + "\r\n˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
            .addField(":sunflower: **Name**" + CustomEmojis.PINK_DASH, "`" + getName() + "`", false)
            .addField(":crescent_moon: **Description**" + CustomEmojis.PINK_DASH, "`" + getDesc() + "`", false)
            .addField(":seedling: **Usage**" + CustomEmojis.PINK_DASH, "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":butterfly: **Other usages**" + CustomEmojis.PINK_DASH,
                    "`" + String.join(", ", slashCmdManagerIns.getCmdNamesByCategory(
                            slashCmdManagerIns.getCmdByCategory(getCategory()))) 
                    + "`", false)
            .addField(":cherry_blossom: **Category**" + CustomEmojis.PINK_DASH, "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + CustomEmojis.PINK_DASH, "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + CustomEmojis.PINK_DASH,
                    "`" + getUserPermsString() + "`", false)
            .addField(":cake: **Bot permissions needed**" + CustomEmojis.PINK_DASH,
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed.build();
    }

    @Override
    public EmbedBuilder getHelpBuilder(SlashCommandInteractionEvent event) {
        SlashCmdManager slashCmdManagerIns = Lemi.getInstance().getSlashCmdManager();
        
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("‧₊੭ :cherries: **HELP GUIDE** ♡ ⋆｡˚\r\n"
                    + "\r\n˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
            .addField(":sunflower: **Name**" + CustomEmojis.PINK_DASH, "`" + getName() + "`", false)
            .addField(":crescent_moon: **Description**" + CustomEmojis.PINK_DASH, "`" + getDesc() + "`", false)
            .addField(":seedling: **Usage**" + CustomEmojis.PINK_DASH, "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":butterfly: **Other usages**" + CustomEmojis.PINK_DASH,
                    "`" + String.join(", ", slashCmdManagerIns.getCmdNamesByCategory(
                            slashCmdManagerIns.getCmdByCategory(getCategory()))) 
                    + "`", false)
            .addField(":cherry_blossom: **Category**" + CustomEmojis.PINK_DASH, "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + CustomEmojis.PINK_DASH, "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + CustomEmojis.PINK_DASH,
                    "`" + getUserPermsString() + "`", false)
            .addField(":cake: **Bot permissions needed**" + CustomEmojis.PINK_DASH,
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed;
    }
}