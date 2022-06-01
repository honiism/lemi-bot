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
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.utils.buttons.Paginator;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShardStatus extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ShardStatus() {
        setName("shardstatus");
        setDesc("View the status of all shards.");
        setUsage("shardstatus [page number]");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
    }

    @Override
    public void action(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
        User author = event.getAuthor();
        
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
                .addAllowedUsers(author.getIdLong())
                .setColor(0xffd1dc)
                .setTimeout(1, TimeUnit.MINUTES);

            List<String> args = ctx.getArgs();
            int page = (!args.isEmpty() && Tools.isInt(args.get(0))) ? Integer.parseInt(args.get(0)) : 1;

            event.getMessage().replyEmbeds(EmbedUtils.getSimpleEmbed(":umbrella2: Loading..."))
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