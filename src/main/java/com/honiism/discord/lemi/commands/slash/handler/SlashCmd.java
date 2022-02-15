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

import java.util.ArrayList;
import java.util.List;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public abstract class SlashCmd implements ISlashCmd {

    protected String name = "";
    protected String desc = "";
    protected String usage = "";
    protected CommandCategory category = CommandCategory.MAIN;
    protected UserCategory userCategory = UserCategory.USERS;
    protected Permission[] userPermissions = new Permission[0];
    protected Permission[] botPermissions = new Permission[0];
    protected List<OptionData> options = new ArrayList<>();
    protected List<SubcommandData> subCmds = new ArrayList<>();
    protected List<SubcommandGroupData> subCmdGroups = new ArrayList<>();

    @Override
    public void executeAction(SlashCommandInteractionEvent event) {
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

        action(event);      
    }

    public abstract void action(SlashCommandInteractionEvent event);

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CommandCategory getCategory() {
        return this.category;
    }

    @Override
    public UserCategory getUserCategory() {
        return this.userCategory;
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
        if (this.userPermissions.length == 0) {
            return "No user permissions needed.";
        }
        return Tools.parsePerms(this.userPermissions)
                + (this.userPermissions.length > 1 ? " permissions" : " permission");
    }

    @Override
    public String getBotPermsString() {
        if (this.botPermissions.length == 0) {
            return "No bot permissions needed.";
        }
        return Tools.parsePerms(this.botPermissions)
        + (this.botPermissions.length > 1 ? " permissions" : " permission");
    }

    @Override
    public Permission[] getUserPerms() {
        return (this.userPermissions.length == 0 ? null : this.userPermissions);
    }

    @Override
    public Permission[] getBotPerms() {
        return (this.botPermissions.length == 0 ? null : this.botPermissions);
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String getUsage() {
        return this.usage;
    }

    @Override
    public List<OptionData> getOptions() {
        return this.options;
    }

    @Override
    public List<SubcommandData> getSubCmds() {
        return this.subCmds;
    }

    @Override
    public List<SubcommandGroupData> getSubCmdGroups() {
        return this.subCmdGroups;
    }

    @Override
    public MessageEmbed getHelp(SlashCommandInteractionEvent event) {
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
                    "`" + String.join(", ", SlashCmdManager.getCmdNames(SlashCmdManager.getCmdByCategory(getCategory()))) 
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
                    "`" + String.join(", ", SlashCmdManager.getCmdNames(SlashCmdManager.getCmdByCategory(getCategory()))) 
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