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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Emojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Announce extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Announce() {
        setName("announce");
        setUsage("announce <#channel> [@role] <message>");
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

        Message message = event.getMessage();
            
        if (timeDelayed >= (10 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());
            
            List<String> args = ctx.getArgs();

            if (args.size() < 2 || message.getMentions().getChannels().isEmpty()) {
                message.reply(":cherries: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            GuildChannel guildChannel = message.getMentions().getChannels().get(0);

            if (!guildChannel.getType().equals(ChannelType.TEXT)) {
                message.reply(":grapes: You can only send announcement messages in text channels.").queue();
                return;
            }

            Role roleToMention = (!message.getMentions().getRoles().isEmpty()) ? message.getMentions().getRoles().get(0) : null;
            String announceMsg = String.join(" ", args.subList((roleToMention != null) ? 2 : 1, args.size()));
            
            EmbedBuilder announceEmbed = new EmbedBuilder()
                .setTitle(":tulip: Ding ding!")
                .setDescription(":sunflower: An announcement has been made!\r\n" + "-\r\n" + "> " + announceMsg)
                .setAuthor(author.getAsTag(), null, author.getEffectiveAvatarUrl())
                .setThumbnail(event.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setColor(0xffd1dc)
                .setFooter("End of announcement!")
                .setTimestamp(Instant.now());

            TextChannel channel = event.getJDA().getTextChannelById(guildChannel.getIdLong());

            if (roleToMention != null) {
                channel.sendMessage(Emojis.EXCLAMATION_MARK + " " + roleToMention.getAsMention())
                    .setEmbeds(announceEmbed.build())
                    .queue((msg) -> {
                        message.reply(":sunflower: Sent the announcement message to " + channel.getAsMention()).queue();
                    });

            } else {
                channel.sendMessageEmbeds(announceEmbed.build())
                    .queue((msg) -> {
                        message.reply(":crescent_moon: Sent the announcement message to " + channel.getAsMention()).queue();
                    });
            }
            
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            message.replyEmbeds(EmbedUtils.errorEmbed("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + author.getAsMention() 
                    + ", you can use this command again in `" + time + "`."))
                .queue();
        }
    }
}