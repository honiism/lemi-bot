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

package com.honiism.discord.lemi.commands.slash.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmdManager;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.EmbedPaginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Help extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Help() {
        this.name = "help";
        this.desc = "Shows information about Lemi.";
        this.usage = "/help [page number]";
        this.category = CommandCategory.MAIN;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER, "page", "Page of the help menu.").setRequired(false)
        );
    }
    
    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();
        
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

            List<EmbedBuilder> items = new ArrayList<>();

            for (CommandCategory category : CommandCategory.values()) {

                if ((category.equals(CommandCategory.MODS)
                        || category.equals(CommandCategory.ADMINS)
                        || category.equals(CommandCategory.DEV))
                        && !Tools.isAuthorMod(author, hook.getInteraction().getTextChannel())) {
                    continue;
                }
                
                items.add(new EmbedBuilder()
                    .setTitle("‧₊੭ :cherries: Lemi commands!")
                    .setDescription("- You can run `/command help` to see a guide for that specific command.\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
                    .appendDescription(":sunflower: Category : " + category.toString() + "\r\n \r\n" 
                        + String.join(", ", SlashCmdManager.getCmdNames(SlashCmdManager.getCmdByCategory(category))))
                    .setThumbnail(hook.getJDA().getSelfUser().getAvatarUrl())
                    .setColor(0xffd1dc)
                );
            }

            EmbedPaginator.Builder builder = new EmbedPaginator.Builder(event.getJDA())
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setTimeout(1, TimeUnit.MINUTES)
                .setItems(items)
                .addAllowedUsers(author.getIdLong())
                .setFooter("© honiism#8022");

            int page = 1;

            if (event.getOption("page") != null) {
                page = (int) event.getOption("page").getAsLong();
            }

            int finalPage = page;

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":snowflake: Finished!"))
                .queue(message -> builder.build().paginate(message, finalPage));

        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherry_blossom: **CHILL!** ♡ ⋆｡˚\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + ":fish_cake:" + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}