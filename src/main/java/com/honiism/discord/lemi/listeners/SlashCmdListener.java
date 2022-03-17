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

package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbBalManager;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCmdListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild() || event.getMember().getUser().isBot()) {
            return;
        }

        if (Lemi.getInstance().isDebug() && !Tools.isAuthorMod(event.getMember(), event)) {
            event.reply(":no_entry_sign: "
                    + "The bot is currently in debug mode and only whitelisted users can execute commands.")
                .queue();
            return;
        }

        LemiDbManager.INS.checkIfBanned(event);

        long userId = event.getMember().getIdLong();

        if (!LemiDbBalManager.INS.userHasData(userId)) {
            LemiDbBalManager.INS.addUserData(userId);
        }

        Lemi.getInstance().getSlashCmdManager().handle(event);
    }
}