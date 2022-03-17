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

import java.time.Instant;
import java.util.HashMap;
import java.util.function.Consumer;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Announce extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Announce() {
        setCommandData(Commands.slash("announce", "Makes an announcement.")
                .addOption(OptionType.STRING, "message", "The message you want to announce.", true)
                .addOption(OptionType.CHANNEL, "channel", "The channel where you want to announce.", true)
                .addOption(OptionType.BOOLEAN, "publishable", "Toggle if Lemi should publish this message.", false)
                .addOption(OptionType.ROLE, "role", "Role to ping while announcing.", false)
        );

        setUsage("/admins announce <message> <channel> [publishable] [role]");
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

            GuildChannel guildChannel = event.getOption("channel", OptionMapping::getAsGuildChannel);

            if (!guildChannel.getType().equals(ChannelType.NEWS)) {
                hook.sendMessage(":grapes: You can only send announcement messages in news channels.").queue();
                return;
            }
            
            String announceMsg = event.getOption("message", OptionMapping::getAsString);

            EmbedBuilder announceEmbed = new EmbedBuilder()
                .setTitle(":tulip: Ding ding!")
                .setDescription(":sunflower: An announcement has been made!\r\n" + "-\r\n" + "> " + announceMsg)
                .setAuthor(author.getAsTag(), null, author.getEffectiveAvatarUrl())
                .setThumbnail(event.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setColor(0xffd1dc)
                .setFooter("End of announcement!")
                .setTimestamp(Instant.now());

            NewsChannel newsChannel = event.getJDA().getNewsChannelById(guildChannel.getIdLong());

            Role roleToMention = event.getOption("role", OptionMapping::getAsRole);
            boolean doCrosspot = event.getOption("publishable", false, OptionMapping::getAsBoolean);

            if (roleToMention != null) {
                newsChannel.sendMessage(CustomEmojis.EXCLAMATION_MARK + " " + roleToMention.getAsMention())
                    .setEmbeds(announceEmbed.build())
                    .queue((msg) -> {
                        if (doCrosspot) {
                            crosspostMessage(msg,
                                    (success) -> hook.sendMessage(":seedling: Successfully published announcement message.").queue(),
                                    (error) -> hook.sendMessage(":blueberries: Unable to publish announcement message").queue());
                            return;
                        }

                        hook.sendMessage(":hibiscus: Announced the message!").queue();
                    });
                return;
            }

            newsChannel.sendMessageEmbeds(announceEmbed.build())
                .queue((msg) -> {
                    if (doCrosspot) {
                        crosspostMessage(msg,
                                (success) -> hook.sendMessage(":seedling: Successfully published announcement message.").queue(),
                                (error) -> hook.sendMessage(":blueberries: Unable to publish announcement message").queue());
                        return;
                    }

                    hook.sendMessage(":hibiscus: Announced the message!").queue();
                }); 
            
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

    private void crosspostMessage(Message message, Consumer<Message> success, Consumer<Throwable> failure) {
        message.crosspost().queue(success, failure);
    }
}