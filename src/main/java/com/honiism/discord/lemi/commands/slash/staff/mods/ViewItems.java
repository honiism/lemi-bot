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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lemi-Bot.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.Items;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ViewItems extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ViewItems() {
        this.name = "viewitems";
        this.desc = "View the currently available items in the internal list.";
        this.usage = "/mods viewitems";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER,
                            "page",
                            "The page number for the guild list you want to see.",
                            false)
        );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        
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
            
            List<String> items = new ArrayList<String>();

            for (Items item : CurrencyTools.getItems()) {
                if (CurrencyTools.checkIfItemExists(item.getName())) {
                   items.add(item.getEmoji() + " " + item.getName() + " | " + item.getId()); 
                } else {
                    items.add(item.getEmoji() + " " + item.getName() + " | " + item.getId() + " | **NOT IN DATABASE**");
                }
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setEmbedDesc("‧₊੭ :tulip: **ITEMS!** ♡ ⋆｡˚")
                .setItemsPerPage(10)
                .setItems(items)
                .useNumberedItems(true)
                .useTimestamp(true)
                .addAllowedUsers(author.getIdLong())
                .setColor(0xffd1dc)
                .setTimeout(1, TimeUnit.MINUTES);

            int page = 1;

            if (event.getOption("page") != null) {
                page = (int) event.getOption("page").getAsLong();
            }

            int finalPage = page;

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":seedling: Loading..."))
                .queue(message -> builder.build().paginate(message, finalPage));
                
        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}