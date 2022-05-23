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

package com.honiism.discord.lemi.commands.text.staff.mods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.buttons.Paginator;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ShardStatus extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ShardStatus() {
        setCommandData(Commands.slash("shardstatus", "View the status of all shards.")
                .addOption(OptionType.INTEGER, "page", "The page number for the shard status you want to see.", false)
        );

        setUsage("/mods shardstatus [page number]");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        
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

            List<JDA> shards = new ArrayList<>(Lemi.getInstance().getShardManager().getShardCache().asList());
            Collections.reverse(shards);

            List<String> shardDetailsItems = new ArrayList<>();

            for (JDA shard : shards) {
                String shardDetails = "Shard id : " + shard.getShardInfo().getShardId()
                        + " | status : " + shard.getStatus()
                        + " | cached guilds : " + shard.getGuildCache().size()
                        + " | cached members : " + shard.getUserCache().size()
                        + " | gateway ping : " + shard.getGatewayPing() + " ms";

                shardDetailsItems.add(shardDetails);
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEmbedDesc("‧₊੭ :candy: **SHARD STATUS** ♡ ⋆｡˚\r\n"
                        + ":sunflower: Total shards : " + shards.size() + "\r\n"
                        + ":seedling: Total guilds : " + Lemi.getInstance().getShardManager().getGuilds().size() + "\r\n"
                        + ":snowflake: Cached users : " + Lemi.getInstance().getShardManager().getUserCache().size() + "\r\n")
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setItems(shardDetailsItems)
                .useNumberedItems(true)
                .useTimestamp(true)
                .addAllowedUsers(event.getUser().getIdLong())
                .setColor(0xffd1dc)
                .setTimeout(1, TimeUnit.MINUTES);

            int page = event.getOption("page", 1, OptionMapping::getAsInt);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":umbrella2: Loading..."))
                .queue(message -> builder.build().paginate(message, page));

        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            event.getMessage().replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }       
    }    
}