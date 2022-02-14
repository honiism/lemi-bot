package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.misc.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Test extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Test.class);
    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Test() {
        this.name = "test";
        this.desc = "Test if Lemi is responding to commands correctly.";
        this.usage = "/mods test [true/false]";
        this.category = CommandCategory.MODS;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(
                new OptionData(OptionType.BOOLEAN, "help", "Want a help guide for this command? (True = yes, false = no).").setRequired(false)
        );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User user = event.getUser();
        
        if (delay.containsKey(user.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(user.getIdLong());
        } else {
            timeDelayed = (5 * 1000);
        }
            
        if (timeDelayed >= (5 * 1000)) {
            if (delay.containsKey(user.getIdLong())) {
                delay.remove(user.getIdLong());
            }
        
            delay.put(user.getIdLong(), System.currentTimeMillis());

            OptionMapping helpOption = event.getOption("help");

            if (helpOption != null && helpOption.getAsBoolean()) {
                hook.sendMessageEmbeds(this.getHelp(event)).queue();
                return;
            }
            
            hook.sendMessage(":dango: Hello there, command run correctly! :)").queue();
            
            log.info(user.getAsTag() + " tested me and I'm working fine (slash).");

            Lemi.getInstance().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
                .getTextChannelById(Config.get("logs_channel_id"))
                .sendMessage(user.getAsTag() + "(" + user.getIdLong() + ") tested me and I'm working fine (slash).")
                .queue();
                
        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}