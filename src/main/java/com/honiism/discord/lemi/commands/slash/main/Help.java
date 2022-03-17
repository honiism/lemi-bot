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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmdManager;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.EmbedPaginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Help extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Help() {
        setCommandData(Commands.slash("help", "Shows information about Lemi.")
                .addOption(OptionType.INTEGER, "page", "Page of the help menu.", false)
                .addOption(OptionType.STRING, "command_name", "The name of command/category.", false)
        );
        
        setUsage("/help [page number]");
        setCategory(CommandCategory.MAIN);
        setUserCategory(UserCategory.USERS);
        setUserPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
    }
    
    @Override
    public void action(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
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

            String cmdName = event.getOption("command_name", OptionMapping::getAsString);

            if (cmdName != null) {
                if (Lemi.getInstance().getSlashCmdManager().getCmdByName(cmdName) == null) {
                    hook.sendMessage(":tulip: That command doesn't exist.\r\n"
                            + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                            + ":sunflower: Don't include the category names when you're trying to " 
                            + "**search for a command and don't include the slashes and the command's category!**\r\n"
                            + CustomEmojis.PINK_CHECK_MARK + " `balance`\r\n"
                            + CustomEmojis.PINK_CROSS_MARK + " `currency balance`\r\n"
                            + CustomEmojis.PINK_CROSS_MARK + " `/balance`\r\n"
                            + CustomEmojis.PINK_CROSS_MARK + " `/currency balance`\r\n"
                            + "-\r\n"
                            + ":seedling: You can also get a category help menu!\r\n"
                            + CustomEmojis.PINK_CHECK_MARK + " `fun`\r\n"
                            + CustomEmojis.PINK_CROSS_MARK + " `/fun`")
                        .queue();
                    return;
                }

                if ((cmdName.equalsIgnoreCase("dev")
                        || cmdName.equalsIgnoreCase("admins")
                        || cmdName.equalsIgnoreCase("mods"))
                        && !Tools.isAuthorMod(event.getMember(), event)) {
                    hook.sendMessage(":tulip: Oops.. too bad! Can't open that menu.").queue();
                    return;
                }

                SlashCmd cmd = Lemi.getInstance().getSlashCmdManager().getCmdByName(cmdName);

                hook.sendMessageEmbeds(cmd.getHelp(event)).queue();
                return;
            }

            List<EmbedBuilder> items = new ArrayList<>();

            for (CommandCategory category : CommandCategory.values()) {

                if ((category.equals(CommandCategory.MODS)
                        || category.equals(CommandCategory.ADMINS)
                        || category.equals(CommandCategory.DEV))
                        && !Tools.isAuthorMod(author)) {
                    continue;
                }
                
                SlashCmdManager slashCmdManagerIns = Lemi.getInstance().getSlashCmdManager();
                
                items.add(new EmbedBuilder()
                    .setTitle("‧₊੭ :cherries: Lemi commands!")
                    .setDescription("- You can run `/help command_name` to see a guide for that specific command.\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
                    .appendDescription(":sunflower: Category : " + category.toString() + "\r\n \r\n" 
                        + String.join(", ", slashCmdManagerIns.getCmdNamesByCategory(slashCmdManagerIns.getCmdByCategory(category))))
                    .setThumbnail(hook.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setColor(0xffd1dc)
                );
            }

            EmbedPaginator.Builder builder = new EmbedPaginator.Builder(event.getJDA())
                .setEventWaiter(Lemi.getInstance().getEventWaiter())
                .setTimeout(1, TimeUnit.MINUTES)
                .setItems(items)
                .addAllowedUsers(author.getIdLong())
                .setFooter("© honiism#8022");

            int page = event.getOption("page", 1, OptionMapping::getAsInt);

            hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":snowflake: Finished!"))
                .queue(message -> builder.build().paginate(message, page));

        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherry_blossom: **CHILL!** ♡ ⋆｡˚\r\n"
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + ":fish_cake:" + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}