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

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.data.database.managers.LemiDbEmbedManager;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Embed extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Embed() {
        setName("embed");
        setDesc("Add, remove or show an embed you created.");
        setUsage("embed ((create|remove <embed_id>|list|show <embed_id>))");
        setCategory(CommandCategory.ADMINS);
        setUserCategory(UserCategory.ADMINS);
        setUserPerms(new Permission[] {Permission.ADMINISTRATOR});
        setBotPerms(new Permission[] {Permission.ADMINISTRATOR});
        
    }

    @Override
    public void action(CommandContext ctx) {
        MessageReceivedEvent event = ctx.getEvent();
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

            List<String> args = ctx.getArgs();
            Message message = event.getMessage();

            if (args.isEmpty()) {
                message.reply(":seedling: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            String subCmdName = args.get(0);
            String embedId;

            switch (subCmdName) {
                case "create":
                    Lemi.getInstance().getEmbedTools().askForId(event.getAuthor(), event.getTextChannel(), event);
                    break;

                case "remove":
                    if (args.size() < 2) {
                        message.reply(":snowflake: Usage: `" + getUsage() + "`!").queue();
                        return;
                    }

                    embedId = args.get(1);

                    LemiDbEmbedManager.INS.deleteCustomEmbed(message, embedId);
                    break;

                case "list":
                    LemiDbEmbedManager.INS.showEmbedsList(message);
                    break;

                case "show":
                    if (args.size() < 2) {
                        message.reply(":tulip: Usage: `" + getUsage() + "`!").queue();
                        return;
                    }

                    embedId = args.get(1);

                    LemiDbEmbedManager.INS.showSavedEmbed(message, embedId, event);
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            event.getMessage().replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + event.getAuthor().getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }
    }
}