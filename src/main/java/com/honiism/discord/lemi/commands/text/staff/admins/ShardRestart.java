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

package com.honiism.discord.lemi.commands.text.staff.admins;

import java.util.HashMap;
import java.util.List;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShardRestart extends TextCmd  {

    private static final Logger log = LoggerFactory.getLogger(ShardRestart.class);
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ShardRestart() {
        setName("shardrestart");
        setDesc("Restart a shard if it gets stuck.");
        setUsage("shardrestart [shard id]");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
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

            List<String> args = ctx.getArgs();

            if (!args.isEmpty() && !Tools.isInt(args.get(0))) {
                event.getMessage().reply(":crescent_moon: `[shard_id]` must be a valid id number.").queue();
                return;
            }

            if (args.isEmpty()) {
                log.info(author.getIdLong() + " is restarting all the shards.");

                event.getMessage().reply(":tulip: Restarting all the shards, see you in a bit :).").queue();

                Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
                    .getTextChannelById(Config.get("logs_channel_id"))
                    .sendMessage(author.getAsMention() + "Someone has reset all the shards. (<@" + author.getIdLong() + ">)")
                    .queue(
                        (success) -> {
                            Lemi.getInstance().getShardManager().restart();
                        }
                    );
                        
            } else if (Integer.parseInt(args.get(0)) < Lemi.getInstance().getShardManager().getShardsTotal()) {
                int shardId = Integer.parseInt(args.get(0));

                log.info(author.getIdLong() + " is restarting the shard(" + shardId +").");
                
                event.getMessage().reply(":tulip: Restarting the shard(" + shardId + "), see you in a bit :).").queue();

                Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_hive"))
                    .getTextChannelById(Config.get("logs_channel_id"))
                    .sendMessage(author.getAsMention() + " is restarting the shard(" + shardId +").")
                    .queue(
                        (success) -> {
                            Lemi.getInstance().getShardManager().restart(shardId);
                        }
                    );

            } else {
                event.getMessage().reply(":leaves: `[shard_id]` must be 1 less than the total shards.").queue();
            }    

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