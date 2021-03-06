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

package com.honiism.discord.lemi.commands.slash.main;

import java.util.HashMap;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Ping extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Ping() {
        setCommandData(Commands.slash("ping", "Shows the current pings for Lemi."));
        setUsage("/ping");
        setCategory(CommandCategory.MAIN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        InteractionHook hook = event.getHook();
        User user = event.getUser();

        if (delay.containsKey(user.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(user.getIdLong());
        } else {
            timeDelayed = (5 * 1000);
        }
            
        if (timeDelayed >= (5 * 1000)) {
            if (delay.containsKey(user.getIdLong())) {
                delay.remove(user.getIdLong());
            }
        
            delay.put(user.getIdLong(), System.currentTimeMillis());

            event.getJDA().getRestPing()
                .queue((ping) -> {
                    EmbedBuilder pingEmbed = new EmbedBuilder()
                        .setDescription("????????? :cherries: **PINGS!** ??? ????????"
                                + "\r\n????? ??????????????????????????????????? ????????.\r\n")
                        .addField(":sunflower: **Rest ping**",  
                                "`" + ping + "`",
                                false)
                        .addField(":seedling: **WS main thread ping**",  
                                "`" + event.getJDA().getGatewayPing() + "`",
                                false)
                        .addField(":snowflake: **Average shards ping**",  
                                "`" + Lemi.getInstance().getShardManager().getAverageGatewayPing() + "`",
                                false)
                        .addField(":cherry_blossom: **Total shards**",  
                                "`" + Lemi.getInstance().getShardManager().getShardsTotal() + "`",
                                false)
                        .setThumbnail(event.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl())
                        .setColor(0xffd1dc);

                    hook.sendMessageEmbeds(pingEmbed.build()).queue();
                });
                
        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription(("????????? :cherry_blossom: **CHILL!** ??? ????????\r\n" 
                        + "????? ??????????????????????????????????? ????????.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`."))
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }    
}