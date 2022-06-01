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

import java.util.HashMap;
import java.util.List;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.text.handler.CommandContext;
import com.honiism.discord.lemi.commands.text.handler.TextCmd;
import com.honiism.discord.lemi.data.UserDataManager;
import com.honiism.discord.lemi.utils.embeds.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ModifyBal extends TextCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyBal() {
        setName("modifybal");
        setDesc("Add or remove some currency from a user.");
        setUsage("modifybal ((add <@user> <amount>|remove <@user> <amount>))");
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

            List<String> args = ctx.getArgs();

            if (args.size() < 3 || event.getMessage().getMentions().getUsers().isEmpty()) {
                event.getMessage().reply(":snowflake: Usage: `" + getUsage() + "`!").queue();
                return;
            }

            String subCmdName = args.get(0);

            switch (subCmdName) {
                case "add":
                    if (!Tools.isLong(args.get(2))) {
                        event.getMessage().reply(":grapes: `<amount>` must be a valid number").queue();
                        return;
                    }

                    long amount = Long.parseLong(args.get(2));

                    if (amount < 0 || amount == 0) {
                        event.getMessage()
                            .reply(":sunflower: You cannot give less or equal to 0 amount of currency.")
                            .queue();
                        return;
                    } 
                
                    //TODO: check the difference between getMembers() and getUsers()
                    User target = event.getMessage().getMentions().getUsers().get(0);

                    if (target == null) {
                        event.getMessage().reply(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    setUserDataManager(target.getIdLong());

                    UserDataManager dataManager = getUserDataManager();
            
                    dataManager.addBalToUser(amount);
            
                    event.getMessage().reply(":cherry_blossom: " 
                            + target.getAsMention() 
                            + ", you have received " + amount 
                            + " " + Tools.getBalName() + " from " 
                            + author.getAsMention() + "!\r\n"
                            + ":blueberries: You now have " + dataManager.getBal()
                            + " " + Tools.getBalName() + ".")
                        .queue();
                    break;

                case "remove":
                    amount = Long.parseLong(args.get(2));

                    if (amount < 0 || amount == 0) {
                        event.getMessage()
                            .reply(":sunflower: You cannot remove less or equal to 0 amount of currency.")
                            .queue();
                        return;
                    } 
                        
                    target = event.getMessage().getMentions().getUsers().get(0);

                    if (target == null) {
                        event.getMessage().reply(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    setUserDataManager(target.getIdLong());

                    dataManager = getUserDataManager();
            
                    dataManager.removeBalFromUser(amount);
            
                    event.getMessage().reply(":cherry_blossom: " 
                            + target.getAsMention() 
                            + ", " + author.getAsMention()
                            + " has taken " + amount + " " 
                            + Tools.getBalName() + " from " + "you" + "!\r\n"
                            + ":blueberries: You now have " + dataManager.getBal()
                            + " " + Tools.getBalName() + ".")
                        .queue();
            }
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