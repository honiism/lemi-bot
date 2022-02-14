package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.Arrays;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.listeners.BaseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Shutdown extends SlashCmd {

    private static final Logger log = LoggerFactory.getLogger(Shutdown.class);

    public Shutdown() {
        this.name = "shutdown";
        this.desc = "Shutsdown Lemi immediately.";
        this.usage = "/dev shutdown [true/false]";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.MODS;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.options = Arrays.asList(
                new OptionData(OptionType.BOOLEAN, "help", "Want a help guide for this command? (True = yes, false = no).").setRequired(false)
        );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();
        
        OptionMapping helpOption = event.getOption("help");

        if (helpOption != null && helpOption.getAsBoolean()) {
            hook.sendMessageEmbeds(this.getHelp(event)).queue();
            return;
        }
        
        log.info(author.getAsTag() + "(" + author.getIdLong() + ") initiated non-emergency shutdown!");
                        
        hook.getJDA().getShardManager().getGuildById(Config.get("honeys_sweets_id"))
            .getTextChannelById(Config.get("logs_channel_id"))
            .sendMessage(author.getAsMention() + " **received non-emergency shutdown request. :bell:**")
            .queue();

        Lemi.getInstance().shutdown(BaseListener.getJDA());
    }   
}