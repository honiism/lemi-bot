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

package com.honiism.discord.lemi.commands.slash.currency;

import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Inventory extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Inventory() {
        this.name = "inventory";
        this.desc = "Shows the inventory of a user.";
        this.usage = "/currency inventory <user> [true/false]";
        this.category = CommandCategory.CURRENCY;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(new OptionData(OptionType.USER,
                                             "user",
                                             "The user you want to see the inventory of.")
                                        .setRequired(true),

                                     new OptionData(OptionType.INTEGER,
                                             "page",
                                             "Page of the help menu.")
                                        .setRequired(false),
                
                                     new OptionData(OptionType.BOOLEAN,
                                             "help",
                                             "Want a help guide for this command? (True = yes, false = no).")
                                         .setRequired(false)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();

        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                hook.sendMessageEmbeds(this.getHelp(event)).queue();
                return;
            }

            Member member = event.getOption("user").getAsMember();
            Guild guild = event.getGuild();
            TextChannel channel = hook.getInteraction().getTextChannel();

            if (member == null) {
                hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                return;
            }

            if (Tools.isEmpty(CurrencyTools.getOwnedItems(String.valueOf(member.getIdLong())))) {
                hook.editOriginal(":fish_cake: This user has no items! Sadge :(").queue();
                return;
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setTimeout(1, TimeUnit.MINUTES)
                .setItems(CurrencyTools.getOwnedItems(String.valueOf(member.getIdLong())))
                .useNumberedItems(true)
                .useTimestamp(true)
                .addAllowedUsers(author.getIdLong())
                .setEmbedDesc(Tools.processPlaceholders(
                        "‧₊੭ :tulip: %user%'s inventory ♡ ⋆｡˚",
                        member, guild, channel))
                .setColor(0xffd1dc)
                .setThumbnail(Tools.processPlaceholders("%user_avatar%", member, guild, channel));

            int page = 1;

            if (event.getOption("page") != null) {
                page = (int) event.getOption("page").getAsLong();
            }

            int finalPage = page;

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":snowflake: Loaded!"))
                .queue(message -> builder.build().paginate(message, finalPage));
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
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