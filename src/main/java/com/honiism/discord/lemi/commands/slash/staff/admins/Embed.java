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

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Embed extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Embed() {
        setCommandData(Commands.slash("embed", "Add, remove or show an embed you created.")
                .addSubcommands(
                        new SubcommandData("create", "Create a custom embed."),

                        new SubcommandData("remove", "Remove an existing embed.")
                                .addOption(OptionType.STRING, "embed_id", "The id of an embed you want to remove.", true),

                        new SubcommandData("list", "Show all the existing embeds."),

                        new SubcommandData("show", "Show an existing embed.")
                                .addOption(OptionType.STRING, "embed_id", "The embed you want to show.", true)
                )
        );

        setUsage("/mods embed ((subcommands))");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        Guild guild = event.getGuild();
        
        if (delay.containsKey(guild.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(guild.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {        
            if (delay.containsKey(guild.getIdLong())) {
                delay.remove(guild.getIdLong());
            }
        
            delay.put(guild.getIdLong(), System.currentTimeMillis());

            String subCmdName = event.getSubcommandName();

            switch (subCmdName) {
                case "create":
                    Lemi.getInstance().getEmbedTools().askForId(hook);
                    break;

                case "remove":
                    String embedIdToDelete = event.getOption("embed_id").getAsString();
                    LemiDbEmbedManager.INS.deleteCustomEmbed(hook, embedIdToDelete);
                    break;

                case "list":
                    LemiDbEmbedManager.INS.showEmbedsList(hook);
                    break;

                case "show":
                    String embedIdToShow = event.getOption("embed_id").getAsString();
                    LemiDbEmbedManager.INS.showSavedEmbed(hook, embedIdToShow);
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
            User author = event.getUser();
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}