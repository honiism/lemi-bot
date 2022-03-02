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

package com.honiism.discord.lemi.commands.slash.staff.admins;

import java.util.HashMap;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ShardRestart extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(ShardRestart.class);
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ShardRestart() {
        setCommandData(Commands.slash("shardrestart", "Restart a shard if it gets stuck.")
                .addOption(OptionType.INTEGER, "shard_id", "The shard id to restart", false)
        );

        setUsage("/admins shardrestart [shard id]");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
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

            Integer shardId = event.getOption("shard_id", OptionMapping::getAsInt);

            if (shardId == null) {
                log.info(author.getIdLong() + " is restarting all the shards.");

                hook.sendMessage(":tulip: Restarting all the shards, see you in a bit :).").queue();

                Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                    .getTextChannelById(Config.get("logs_channel_id"))
                    .sendMessage(author.getAsMention() + "Someone has reset all the shards. (<@" + author.getIdLong() + ">)")
                    .queue(
                        (success) -> {
                            Lemi.getInstance().getShardManager().restart();
                        }
                    );
                        
            } else if (shardId != null && shardId < Lemi.getInstance().getShardManager().getShardsTotal()) {
                log.info(author.getIdLong() + " is restarting the shard(" + shardId +").");
                
                hook.sendMessage(":tulip: Restarting the shard(" + shardId + "), see you in a bit :).").queue();

                Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                    .getTextChannelById(Config.get("logs_channel_id"))
                    .sendMessage(author.getAsMention() + " is restarting the shard(" + shardId +").")
                    .queue(
                        (success) -> {
                            Lemi.getInstance().getShardManager().restart(shardId);
                        }
                    );
            }

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