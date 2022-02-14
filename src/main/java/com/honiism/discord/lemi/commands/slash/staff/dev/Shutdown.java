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

package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.Arrays;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.listeners.BaseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Shutdown extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Shutdown.class);

    public Shutdown() {
        this.name = "shutdown";
        this.desc = "Shutsdown Lemi immediately.";
        this.usage = "/dev shutdown [true/false]";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.options = Arrays.asList(
                new OptionData(OptionType.BOOLEAN, "help", "Want a help guide for this command? (True = yes, false = no).").setRequired(false)
        );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();
        
        OptionMapping helpOption = event.getOption("help");

        if (helpOption != null && helpOption.getAsBoolean()) {
            hook.sendMessageEmbeds(this.getHelp(event)).queue();
            return;
        }
        
        log.info(author.getAsTag() + "(" + author.getIdLong() + ") initiated non-emergency shutdown!");
                        
        hook.getJDA().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(author.getAsMention() + " **received non-emergency shutdown request. :bell:**")
            .queue();

        Lemi.getInstance().shutdown(BaseListener.getJDA());
    }   
}