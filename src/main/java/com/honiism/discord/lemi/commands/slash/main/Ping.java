package com.honiism.discord.lemi.commands.slash.main;

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.misc.CustomEmojis;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Ping extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Ping() {
        this.name = "ping";
        this.desc = "Shows the current pings for Lemi.";
        this.usage = "/ping [true/false]";
        this.category = CommandCategory.MAIN;
        this.userCategory = UserCategory.USERS;
        this.userPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.botPermissions = new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY};
        this.options = Arrays.asList(
                new OptionData(OptionType.BOOLEAN, "help", "Want a help guide for this command? (True = yes, false = no).")
                    .setRequired(false)
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
                hook.sendMessageEmbeds(getHelp(event)).queue();
                return;
            }

            event.getJDA().getRestPing()
                .queue((ping) -> {
                    EmbedBuilder pingEmbed = new EmbedBuilder()
                        .setDescription("‧₊੭ :cherries: **PINGS!** ♡ ⋆｡˚"
                                + "\r\n˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n")
                        .addField("**Rest ping**" + CustomEmojis.PINK_DASH,  
                                "`" + ping + "`",
                                false)
                        .addField("**WS main thread ping**" + CustomEmojis.PINK_DASH,  
                                "`" + event.getJDA().getGatewayPing() + "`",
                                false)
                        .addField("**Average shards ping**" + CustomEmojis.PINK_DASH,  
                                "`" + Lemi.getInstance().getShardManager().getAverageGatewayPing() + "`",
                                false)
                        .addField("**Total shards**" + CustomEmojis.PINK_DASH,  
                                "`" + Lemi.getInstance().getShardManager().getShardsTotal() + "`",
                                false)
                        .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
                        .setColor(0xffd1dc);

                    hook.sendMessageEmbeds(pingEmbed.build()).queue();
                });
                
        } else {
            String time = Tools.secondsToTime(((5 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription(("‧₊੭ :cherry_blossom: **CHILL!** ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + user.getAsMention() 
                        + ", you can use this command again in `" + time + "`."))
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }    
}