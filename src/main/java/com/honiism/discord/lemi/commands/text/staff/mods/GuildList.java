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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MiscUtil;

public class GuildList extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public GuildList() {
        setName("guildlist");
        setDesc("View the list of guilds that Lemi is in.");
        setUsage("guildlist [page number]");
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
            
            List<Guild> guilds = new ArrayList<>(Lemi.getInstance().getShardManager().getGuilds());
	    Collections.reverse(guilds);

            List<String> guildDetails = new ArrayList<>();
            StringBuilder guildDetailsBuilder = new StringBuilder();

            for (Guild guild : guilds) {
                guildDetailsBuilder.append(guild.getName() + " | id: " + guild.getIdLong() 
                        + " | members in cache: " + guild.getMemberCache().size());

                guild.retrieveOwner(true).queue(
                        (owner) -> {
                            guildDetailsBuilder.append(" | owner: " + owner.getAsMention() );
                        },
                        (empty) -> {
                            guildDetailsBuilder.append(" | owner: " + "not found");
                        }
                );

                guildDetailsBuilder.append(" | shard id: " 
                        + MiscUtil.getShardForGuild(guild, Lemi.getInstance().getShardManager().getShardsTotal()));

                guildDetails.add(guildDetailsBuilder.toString());
                guildDetailsBuilder.setLength(0);
            }

            Paginator.Builder builder = new Paginator.Builder(event.getJDA())
                .setEmbedDesc("‧₊੭ :snowflake: **GUILD LIST!** ♡ ⋆｡˚")
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setItemsPerPage(10)
                .setItems(guildDetails)
                .useNumberedItems(true)
                .useTimestamp(true)
                .addAllowedUsers(author.getIdLong())
                .setColor(0xffd1dc)
                .setTimeout(1, TimeUnit.MINUTES);

            List<String> args = ctx.getArgs();
            int page = (!args.isEmpty() && Tools.isInt(args.get(0))) ? Integer.parseInt(args.get(0)) : 1;

            event.getMessage().replyEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
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