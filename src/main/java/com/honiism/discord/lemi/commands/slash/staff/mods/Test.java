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
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Test extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Test.class);
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Test() {
        setCommandData(Commands.slash("test", "Test if Lemi is responding to commands correctly."));
        setUsage("/mods test");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setGlobal(true);
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

            hook.sendMessage(":dango: Hello there, command run correctly! :)").queue();
            
            log.info(author.getAsTag() + " tested me and I'm working fine (slash).");

            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                .getTextChannelById(Config.get("logs_channel_id"))
                .sendMessage(author.getAsTag() + "(" + author.getIdLong() + ") tested me and I'm working fine (slash).")
                .queue();
                
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