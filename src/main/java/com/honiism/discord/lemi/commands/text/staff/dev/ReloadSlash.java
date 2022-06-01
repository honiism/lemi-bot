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

package com.honiism.discord.lemi.commands.text.staff.dev;

import java.util.HashMap;
import java.util.List;

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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReloadSlash extends TextCmd {

    private static final Logger log = LoggerFactory.getLogger(Shutdown.class);

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ReloadSlash() {
        setName("reload");
        setDesc("Reload Lemi's slash commands  .");
        setAliases(new String[] {"reloadslash", "reloadcmd", "reloadcmds"});
        setUsage("reload <is global commands (true|false)>");
        setCategory(CommandCategory.DEV);
        setUserCategory(UserCategory.DEV);
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

            if (args.isEmpty()) {
                event.getMessage().reply(":grapes: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            if (!args.get(0).equals("true") && !args.get(0).equals("false")) {
                event.getMessage().reply(":honey_pot: Only `true` or `false values.").queue();
                return;
            }

            boolean isGlobal = Boolean.parseBoolean(args.get(0));

            if (isGlobal) {
                Lemi.getInstance().getSlashCmdManager().reloadGlobalCmds();

                event.getMessage().reply(":strawberry: Global slash commands are now reloading.").queue();
                log.info("Slash commands are now reloading (global)!");
            } else {
                Guild guild = event.getGuild();

                Lemi.getInstance().getSlashCmdManager().reloadGuildCmds(guild);

                event.getMessage().reply(":herb: Commands are now reloaded for this guild only!").queue();
                log.info("Commands are now reloaded for a guild with the id of" + guild.getIdLong() + "only!");
            }

        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            event.getMessage().replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }
    }   
}