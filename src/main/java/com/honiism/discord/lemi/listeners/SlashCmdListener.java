package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.commands.slash.handler.SlashCmdManager;
import com.honiism.discord.lemi.database.managers.LemiDbManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCmdListener extends ListenerAdapter {
    
    private SlashCmdManager slashManager;

    public SlashCmdListener() {
        slashManager = new SlashCmdManager();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        LemiDbManager.INS.onSlashCommand(event);
        slashManager.handle(event);
    }
}