package com.honiism.discord.lemi.listeners;

import com.honiism.discord.lemi.utils.currency.CurrencyTools;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberGuildListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();

        if (CurrencyTools.userHasCurrProfile(member) || member.getUser().isBot()) {
            return;
        }

        CurrencyTools.addAllProfiles(member);
    }
}