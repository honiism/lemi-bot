package com.honiism.discord.lemi.commands.slash.currency;

import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Balance extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Balance() {
        this.name = "balance";
        this.desc = "Shows the balance of a user if provided.";
        this.usage = "/currency balance [true/false] [user]";
        this.category = CommandCategory.CURRENCY;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(new OptionData(OptionType.USER,
                                             "user",
                                             "The user you want to see the balance of.")
                                        .setRequired(true),
                
                                    new OptionData(OptionType.BOOLEAN,
                                             "help",
                                             "Want a help guide for this command? (True = yes, false = no).")
                                         .setRequired(false)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();

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

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                hook.sendMessageEmbeds(this.getHelp(event)).queue();
                return;
            }

            Member member = event.getOption("user").getAsMember();

            if (member == null) {
                hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                return;
            }

            EmbedBuilder userBal = new EmbedBuilder();
            Guild guild = event.getGuild();
            TextChannel channel = hook.getInteraction().getTextChannel();
            long bal = CurrencyTools.getUserbal(String.valueOf(member.getIdLong()));

            userBal.setDescription(Tools.processPlaceholders(
                    "‧₊੭ :cherry_blossom: %user%'s balance ♡ ⋆｡˚\r\n"
                    + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                    + CurrencyTools.getBalName(String.valueOf(guild.getIdLong())) + " " + bal,
                    member, guild, channel))
                .setColor(0xffd1dc)
                .setThumbnail(Tools.processPlaceholders("%user_avatar%", member, guild, channel))
                .setAuthor(Tools.processPlaceholders("%user_tag%", member, guild, channel),
                        null, Tools.processPlaceholders("%user_avatar%", member, guild, channel));

            hook.sendMessageEmbeds(userBal.build()).queue();
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
}