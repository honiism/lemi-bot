package com.honiism.discord.lemi.database.managers;

import java.util.List;

import com.honiism.discord.lemi.database.LemiDbDs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface LemiDbManager {
    LemiDbManager INS = new LemiDbDs();

    // userban
    List<String> getReasons(SlashCommandInteractionEvent event);
    List<String> getAuthorIds(SlashCommandInteractionEvent event);
    List<String> getUserIds(SlashCommandInteractionEvent event);
    void addUserId(Member member, String reason, SlashCommandInteractionEvent event);
    void removeUserId(Member member, SlashCommandInteractionEvent event);

    // modifyadmins
    List<String> getAdminIds(SlashCommandInteractionEvent event);
    List<String> getAdminKeys(SlashCommandInteractionEvent event);
    void removeAdminId(Guild guild, User user, SlashCommandInteractionEvent event);
    void addAdminId(Guild guild, Member member, String key, SlashCommandInteractionEvent event);

    // modifymods
    List<String> getModIds(SlashCommandInteractionEvent event);
    List<String> getModKeys(SlashCommandInteractionEvent event);
    void removeModId(Guild guild, User user, SlashCommandInteractionEvent event);
    void addModId(Guild guild, Member member, String key, SlashCommandInteractionEvent event);

    // guildlistener
    void onGuildReady(GuildReadyEvent event);

    // slashcmdlistener
    void onSlashCommand(SlashCommandInteractionEvent event);

    // currencytools
    String getBalName(String guildId);

    // tools
    boolean isAuthorAdmin(User author, TextChannel channel);
    boolean isAuthorMod(User author, TextChannel channel);
    boolean isAuthorAdmin(Member member, SlashCommandInteractionEvent event);
    boolean isAuthorMod(Member member, SlashCommandInteractionEvent event);
}