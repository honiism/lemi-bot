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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ShardStatus extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ShardStatus() {
        this.name = "shardstatus";
        this.desc = "View the status of all shards.";
        this.usage = "/mods shardstatus [true/false] [page number]";
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
                                             "The page number for the shard status you want to see.")
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

            if (event.getOption("help") != null && event.getOption("help").getAsBoolean()) {
                hook.sendMessageEmbeds(getHelp(event)).queue();
                return;
            }

            List<JDA> shards = new ArrayList<>(Lemi.getInstance().getShardManager().getShardCache().asList());
            Collections.reverse(shards);

            List<String> shardDetails = new ArrayList<>();
            StringBuilder shardDetailsBuilder = new StringBuilder();

            for (JDA shard : shards) {
                int shardId = shard.getShardInfo().getShardId();

                shardDetailsBuilder.append("Shard id : " + shardId
                        + " | status : " + Lemi.getInstance().getShardManager().getShardById(shardId).getStatus()
                        + " | cached guilds : " + Lemi.getInstance().getShardManager().getShardById(shardId).getGuildCache().size()
                        + " | cached members : " + Lemi.getInstance().getShardManager().getShardById(shardId).getUserCache().size()
                        + " | gateway ping : " + Lemi.getInstance().getShardManager().getShardById(shardId).getGatewayPing() + " ms");

                shardDetails.add(shardDetailsBuilder.toString());
                shardDetailsBuilder.setLength(0);
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEmbedDesc("‧₊੭ :candy: **SHARD STATUS** ♡ ⋆｡˚\r\n"
                        + ":sunflower: Total shards : " + shards.size() + "\r\n"
                        + ":seedling: Total guilds : " + Lemi.getInstance().getShardManager().getGuilds().size() + "\r\n"
                        + ":snowflake: Cached users : " + Lemi.getInstance().getShardManager().getUserCache().size() + "\r\n")
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setItems(shardDetails)
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

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":umbrella2: Loading..."))
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