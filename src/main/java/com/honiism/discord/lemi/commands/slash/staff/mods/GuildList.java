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

package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MiscUtil;

public class GuildList extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public GuildList() {
        this.name = "guildlist";
        this.desc = "View the list of guilds that Lemi is in.";
        this.usage = "/mods guildlist [true/false] [page number]";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(new OptionData(OptionType.BOOLEAN,
                                             "help",
                                             "Want a help guide for this command? (True = yes, false = no).")
                                         .setRequired(false),

                                     new OptionData(OptionType.INTEGER,
                                             "page",
                                             "The page number for the guild list you want to see.")
                                         .setRequired(false)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User user = event.getUser();
        
        if (delay.containsKey(user.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(user.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {
            if (delay.containsKey(user.getIdLong())) {
                delay.remove(user.getIdLong());
            }
        
            delay.put(user.getIdLong(), System.currentTimeMillis());

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                hook.sendMessageEmbeds(getHelp(event)).queue();
                return;
            }
            
            List<Guild> guilds = new ArrayList<>(Lemi.getInstance().getShardManager().getGuilds());
	    Collections.reverse(guilds);

            List<String> guildDetails = new ArrayList<>();
            StringBuilder guildDetailsBuilder = new StringBuilder();

            for (Guild guild : guilds) {
                guildDetailsBuilder.append(guild.getName() + " | id: " + guild.getIdLong() 
                        + " | members in cache: " + guild.getMemberCache());

                guild.retrieveOwner(true)
                    .queue(
                        (owner) -> {
                            guildDetailsBuilder.append(" | owner: " + owner.getAsMention() 
                                    + " | shard id: " 
                                    + MiscUtil.getShardForGuild(guild, Lemi.getInstance().getShardManager().getShardsTotal()));
                        },
                        (empty) -> {
                            guildDetailsBuilder.append(" | owner: " + "not found!" 
                                    + " | shard id: " 
                                    + MiscUtil.getShardForGuild(guild, Lemi.getInstance().getShardManager().getShardsTotal()));
                        }
                    );

                guildDetails.add(guildDetailsBuilder.toString());
                guildDetailsBuilder.setLength(0);
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEmbedDesc("‧₊੭ :snowflake: **GUILD LIST!** ♡ ⋆｡˚")
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setItems(guildDetails)
                .useNumberedItems(true)
                .useTimestamp(true)
                .addAllowedUsers(event.getUser().getIdLong())
                .setColor(0xffd1dc)
                .setTimeout(1, TimeUnit.MINUTES);

            int page = 1;

            if (event.getOption("page") != null) {
                page = (int) event.getOption("page").getAsLong();
            }

            int finalPage = page;

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                .queue(message -> builder.build().paginate(message, finalPage));

        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}