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

package com.honiism.discord.lemi.commands.slash.fun;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class FunTopLevel extends SlashCmd {

    private TruthOrDare todCmd;

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public FunTopLevel(TruthOrDare todCMd) {
        this.todCmd = todCMd;

        setCommandData(Commands.slash("fun", "Commands for the fun category.")
                .addSubcommandGroups(
                        new SubcommandGroupData(this.todCmd.getName(), this.todCmd.getDesc())
                                .addSubcommands(this.todCmd.getSubCmds())
                )
        );
        setUsage("/fun ((subcommand groups/subcommands))");
        setCategory(CommandCategory.FUN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
    }

    @Override
    public void action(SlashCommandInteractionEvent event) throws JsonMappingException, JsonProcessingException {
        event.deferReply().queue();
        
        String subCmdGroupName = event.getSubcommandGroup();
        String subCmdName = event.getSubcommandName();

        if (subCmdGroupName != null) {
            switch (subCmdGroupName) {
                case "tod":
                    Guild guild = event.getGuild();

                    if (delay.containsKey(guild.getIdLong())) {
                        timeDelayed = System.currentTimeMillis() - delay.get(guild.getIdLong());
                    } else {
                        timeDelayed = (5 * 1000);
                    }
                        
                    if (timeDelayed >= (5 * 1000)) {        
                        if (delay.containsKey(guild.getIdLong())) {
                            delay.remove(guild.getIdLong());
                        }
                    
                        delay.put(guild.getIdLong(), System.currentTimeMillis());

                        this.todCmd.action(event);

                    } else {
                        String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                        User author = event.getUser();
                        InteractionHook hook = event.getHook();
                            
                        hook.sendMessageEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                                + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                                + author.getAsMention() 
                                + ", you can use this command again in `" + time + "`."))
                            .queue();
                    }
                    break;
            }
        } else {
            switch (subCmdName) {
                
            }
        }
    }
}