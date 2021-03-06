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

package com.honiism.discord.lemi.commands.text.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.data.currency.UserDataManager;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class TextCmd {
     
    private String name = "";
    private String description = "";
    private String usage = "";
    private String[] aliases = new String[0];
    private CommandCategory category = CommandCategory.MAIN;
    private UserCategory userCategory = UserCategory.USERS;
    private Permission[] userPermissions = new Permission[0];
    private Permission[] botPermissions = new Permission[0];
    private UserDataManager userDataManager;

    public void preAction(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
        Member member = event.getMember();

        if (getUserCategory().equals(UserCategory.DEV) 
                && !Tools.isAuthorDev(member)) {
            return;
        }

        if (getUserCategory().equals(UserCategory.ADMINS) 
                && !Tools.isAuthorAdmin(member.getUser())) {
            return;
        }

        if (getUserCategory().equals(UserCategory.MODS) 
                && !Tools.isAuthorMod(member.getUser())) {
            return;
        }
        
        MessageChannel channel = event.getChannel();

        if (getUserPerms().length > 0 && !member.hasPermission(getUserPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "????? ??????????????????????????????????? ????????.\r\n" + ":sunflower:" + member.getAsMention() + "\r\n"
                        + "> You don't have the " + getUserPermsString())
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(0xffd1dc);
            
            channel.sendMessageEmbeds(needUserPermsMsg.build()).queue();
            return;
        }

        if (getBotPerms().length > 0 
                && !event.getGuild().getSelfMember().hasPermission(getBotPerms())) {
            EmbedBuilder needUserPermsMsg = new EmbedBuilder()
                .setDescription(":cherries: **WAIT!**\r\n"
                        + "????? ??????????????????????????????????? ????????.\r\n" + member.getAsMention() + "\r\n" 
                        + "> I don't have the " + getBotPermsString())
                .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
                .setColor(0xffd1dc);

            channel.sendMessageEmbeds(needUserPermsMsg.build()).queue();
            return;
        }

        action(ctx);
    }

    public abstract void action(CommandContext ctx);

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public String getAliasesFormat() {
        return (aliases.length == 0) ? "No aliases available" : String.join(", ", aliases);
    }

    public String[] getAliases() {
        return aliases;
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

    public void setDesc(String description) {
        this.description = description;
    }

    public String getDesc() {
        return description;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    public UserDataManager getUserDataManager() {
        return userDataManager;
    }

    public void setUserDataManager(long userId) {
        try {
            this.userDataManager = new UserDataManager(userId, LemiDbBalManager.INS.getUserData(userId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public MessageEmbed getHelp(SlashCommandInteractionEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("????????? :cherries: **HELP GUIDE** ??? ????????\r\n"
                    + "\r\n????? ??????????????????????????????????? ????????.\r\n")
            .addField(":sunflower: **Name**" + " !! ", "`" + getName() + "`", false)
            .addField(":crescent_moon: **Aliases**" + " !! ", "`" + getAliasesFormat() + "`", false)
            .addField(":seedling: **Description**" + " !! ", "`" + getDesc() + "`", false)
            .addField(":butterfly: **Usage**" + " !! ", "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":cherry_blossom: **Category**" + " !! ", "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + " !! ", "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + " !! ",
                    "`" + getUserPermsString() + "`", false)
            .addField(":honey_pot: **Bot permissions needed**" + " !! ",
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed.build();
    }

    public MessageEmbed getHelp(MessageReceivedEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("????????? :cherries: **HELP GUIDE** ??? ????????\r\n"
                    + "\r\n????? ??????????????????????????????????? ????????.\r\n")
            .addField(":sunflower: **Name**" + " !! ", "`" + getName() + "`", false)
            .addField(":crescent_moon: **Aliases**" + " !! ", "`" + getAliasesFormat() + "`", false)
            .addField(":seedling: **Description**" + " !! ", "`" + getDesc() + "`", false)
            .addField(":butterfly: **Usage**" + " !! ", "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":cherry_blossom: **Category**" + " !! ", "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + " !! ", "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + " !! ",
                    "`" + getUserPermsString() + "`", false)
            .addField(":honey_pot: **Bot permissions needed**" + " !! ",
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed.build();
    }

    public EmbedBuilder getHelpBuilder(MessageReceivedEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setDescription("????????? :cherries: **HELP GUIDE** ??? ????????\r\n"
                    + "\r\n????? ??????????????????????????????????? ????????.\r\n")
            .addField(":sunflower: **Name**" + " !! ", "`" + getName() + "`", false)
            .addField(":crescent_moon: **Aliases**" + " !! ", "`" + getAliasesFormat() + "`", false)
            .addField(":seedling: **Description**" + " !! ", "`" + getDesc() + "`", false)
            .addField(":butterfly: **Usage**" + " !! ", "`" + getUsage() + "`\r\n"  
                    + "\r\n`[] : Optional argument(s).`\r\n"
                    + "`<> : Required argument(s).`\r\n"
                    + "`((. . .)) : pick the options given.`", false)
            .addField(":cherry_blossom: **Category**" + " !! ", "`" + getCategoryString() + "`", false)
            .addField(":grapes: **User category**" + " !! ", "`" + getUserCategoryString() + "`", false)
            .addField(":strawberry: **User permissions needed**" + " !! ",
                    "`" + getUserPermsString() + "`", false)
            .addField(":honey_pot: **Bot permissions needed**" + " !! ",
                    "`" + getBotPermsString() + "`", false)
            .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
            .setColor(0xffd1dc);

        return helpEmbed;
    }
}