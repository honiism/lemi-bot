package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyBal extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyBal() {
        this.name = "modifybal";
        this.desc = "Add or remove some currency from a user.";
        this.usage = "/mods modifybal ((subcommand))";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),
                                      
                                     new SubcommandData("add", "Add some currency to a user.")
                                         .addOption(OptionType.USER, "user", "The user you'd like to add some currency to.", true)
                                         .addOption(OptionType.INTEGER, "amount",
                                                 "The amount of currency you'd like to add.'",
                                                 true),
  
                                     new SubcommandData("remove", "Remove some currency from a user.")
                                         .addOption(OptionType.USER, "user",
                                                 "The user you'd like to remove some currency from.",
                                                 true)
                                         .addOption(OptionType.INTEGER, "amount",
                                                 "The amount of currency you'd like to remove.", true)
                                    );
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

            String subCmdName = event.getSubcommandName();
            Guild guild = event.getGuild();

            switch (subCmdName) {
                case "help":
                    hook.sendMessageEmbeds(this.getHelp(event)).queue();
                    break;

                case "add":
                    int amount = (int) event.getOption("amount").getAsLong();

                    if (amount < 0 || amount == 0) {
                        hook.sendMessage(":sunflower: You cannot give less or equal to 0 amount of currency.").queue();
                        return;
                    } 
                        
                    Member memberToAddCurr = event.getOption("user").getAsMember();
            
                    CurrencyTools.addBalToUser(String.valueOf(memberToAddCurr.getIdLong()),
                            CurrencyTools.getUserbal(String.valueOf(memberToAddCurr.getIdLong())), amount);
            
                    hook.sendMessage(":cherry_blossom: " 
                            + memberToAddCurr.getAsMention() 
                            + ", you have received " + amount 
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + " from " 
                            + author.getAsMention() + "!\r\n"
                            + ":blueberries: You now have " + CurrencyTools.getUserbal(String.valueOf(memberToAddCurr.getIdLong()))
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + ".")
                        .queue();
                    break;

                case "remove":
                    int removeAmount = (int) event.getOption("amount").getAsLong();

                    if (removeAmount < 0 || removeAmount == 0) {
                        hook.sendMessage(":sunflower: You cannot remove less or equal to 0 amount of currency.").queue();
                        return;
                    } 
                        
                    Member memberToRemoveCurr = event.getOption("user").getAsMember();
            
                    CurrencyTools.removeBalFromUser(String.valueOf(memberToRemoveCurr.getIdLong()),
                            CurrencyTools.getUserbal(String.valueOf(memberToRemoveCurr.getIdLong())), removeAmount);
            
                    hook.sendMessage(":cherry_blossom: " 
                            + memberToRemoveCurr.getAsMention() 
                            + ", " + author.getAsMention()
                            + " has taken " + removeAmount + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + " from " 
                            + "you" + "!\r\n"
                            + ":blueberries: You now have " + CurrencyTools.getUserbal(String.valueOf(memberToRemoveCurr.getIdLong()))
                            + " " + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + ".")
                        .queue();
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }        
    }    
}