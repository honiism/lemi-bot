/*
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

package com.honiism.discord.lemi.utils.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.honiism.discord.lemi.Config;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;

import me.duncte123.botcommons.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Tools {

    public static StringBuilder replaceAllSb(StringBuilder sb, String find, String replace){
        return new StringBuilder(Pattern.compile(find).matcher(sb).replaceAll(replace));
    }

    public static String getRandomEntry(String[] array) {
        int randomEntry = new Random().nextInt(array.length);
        return array[randomEntry];
    }


    public static String processPlaceholders(String msgToProcess, Member member, Guild guild, TextChannel channel) {
        StringBuilder stringBuilder = new StringBuilder();
        String temp;

        stringBuilder.append(msgToProcess);

        if (stringBuilder.indexOf("%user%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user%", member.getUser().getAsMention()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%user_tag%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_tag%", member.getUser().getAsTag()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_name%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_name%", member.getUser().getName()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_avatar%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_avatar%", member.getUser().getEffectiveAvatarUrl()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%user_discrim%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_discrim%", member.getUser().getDiscriminator()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_id%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_id%", String.valueOf(member.getUser().getIdLong())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_nick%") != -1) {
            temp = replaceAllSb(stringBuilder, "%user_nick%", member.getEffectiveName()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_createdate%") != -1) {
            OffsetDateTime createdTime = member.getUser().getTimeCreated();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            temp = replaceAllSb(stringBuilder, "%user_createdate%", fmt.format(createdTime)).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_joindate%") != -1) {
            OffsetDateTime joinTime = member.getTimeJoined();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            temp = replaceAllSb(stringBuilder, "%user_joindate%", fmt.format(joinTime)).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%user_boostsince%") != -1) {
            OffsetDateTime boostTime = member.getTimeBoosted();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            temp = replaceAllSb(stringBuilder, "%user_boostsince%", fmt.format(boostTime)).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }
        
        if (stringBuilder.indexOf("%server_currency%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_currency%", CurrencyTools.getBalName()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_name%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_name%", guild.getName()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_id%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_id%", String.valueOf(guild.getIdLong())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_membercount_ordinal%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_membercount_ordinal%", getOrdinalNum(guild.getMemberCount())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_membercount_nobots%") != -1) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    long nonBots = memberList.stream().filter(m -> !m.getUser().isBot()).count();
                    String specificTemp = replaceAllSb(stringBuilder,
                            "%server_membercount_nobots%", Long.toString(nonBots)).toString();

                    stringBuilder.setLength(0);
                    stringBuilder.append(specificTemp);
                });
        }

        if (stringBuilder.indexOf("%server_membercount_nobots_ordinal%") != -1) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    long nonBots = memberList.stream().filter(m -> !m.getUser().isBot()).count();
                    String specificTemp = replaceAllSb(stringBuilder,
                            "%server_membercount_nobots_ordinal%", getOrdinalNum((int) nonBots)).toString();

                    stringBuilder.setLength(0);
                    stringBuilder.append(specificTemp);
                });
        }

        if (stringBuilder.indexOf("%server_botcount%") != -1) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    long bots = memberList.stream().filter(m -> m.getUser().isBot()).count();
                    String specificTemp = replaceAllSb(stringBuilder,
                            "%server_botcount%", Long.toString(bots)).toString();

                    stringBuilder.setLength(0);
                    stringBuilder.append(specificTemp);
                });
        }

        if (stringBuilder.indexOf("%server_botcount_ordinal%") != -1) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    long bots = memberList.stream().filter(m -> m.getUser().isBot()).count();
                    String specificTemp = replaceAllSb(stringBuilder,
                            "%server_botcount_ordinal%", getOrdinalNum((int) bots)).toString();

                    stringBuilder.setLength(0);
                    stringBuilder.append(specificTemp);
                });
        }

        if (stringBuilder.indexOf("%server_icon%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_icon%", guild.getIconUrl()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_rolecount%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_rolecount%", Integer.toString(guild.getRoles().size())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_channelcount%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_channelcount%", Integer.toString(guild.getChannels().size())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_randommember%") != -1) {
            List<Member> members = guild.getMembers();
            Random rand = new Random();
            Member randomMember = members.get(rand.nextInt(members.size()));

            temp = replaceAllSb(stringBuilder, "%server_randommember%", randomMember.getAsMention()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_randommember_tag%") != -1) {
            List<Member> members = guild.getMembers();
            Random rand = new Random();
            Member randomMember = members.get(rand.nextInt(members.size()));

            temp = replaceAllSb(stringBuilder, "%server_randommember_tag%", randomMember.getUser().getAsTag()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_randommember_nobots%") != -1) {
            guild.loadMembers()
                .onSuccess((memberList) -> {
                    List<Member> nonBots = memberList.stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toList());
                    Random rand = new Random();
                    Member randomMember = nonBots.get(rand.nextInt(nonBots.size()));

                    String specificTemp = replaceAllSb(stringBuilder,
                            "%server_randommember_nobots%", randomMember.getAsMention()).toString();

                    stringBuilder.setLength(0);
                    stringBuilder.append(specificTemp);
                });
        }

        if (stringBuilder.indexOf("%server_owner%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_owner%", guild.getOwner().getAsMention()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_owner_id%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_owner_id%", guild.getOwnerId()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_createdate%") != -1) {
            OffsetDateTime createdTime = guild.getTimeCreated();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            temp = replaceAllSb(stringBuilder, "%server_createdate%", fmt.format(createdTime)).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_boostlevel%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_boostlevel%", guild.getBoostTier().toString()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%server_boostcount%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_boostcount%", Integer.toString(guild.getBoostCount())).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%channel%") != -1) {
            temp = replaceAllSb(stringBuilder, "%server_boostlevel%", channel.getAsMention()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%channel_name%") != -1) {
            temp = replaceAllSb(stringBuilder, "%channel_name%", channel.getName()).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%channel_createdate%") != -1) {
            OffsetDateTime createdTime = channel.getTimeCreated();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            temp = replaceAllSb(stringBuilder, "%channel_createdate%", fmt.format(createdTime)).toString();

            stringBuilder.setLength(0);
            stringBuilder.append(temp);
        }

        if (stringBuilder.indexOf("%date%") != -1) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");  
                LocalDateTime now = LocalDateTime.now();  
    
                temp = replaceAllSb(stringBuilder, "%date%", dtf.format(now)).toString();
    
                stringBuilder.setLength(0);
                stringBuilder.append(temp);
        }

        return stringBuilder.toString();
    }

    public static String formatTime(long timeInMillis) {
        long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static boolean isUrl(String argument) {
        if (argument.startsWith("http://") || argument.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static long diffInMinutes(String dateToCompare) {
        Calendar oldDate = Calendar.getInstance(); // old datetime or expiration date

        try {
            oldDate.setTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateToCompare));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        Calendar now = Calendar.getInstance(); // current datetime
        long diffInMillis = now.getTimeInMillis() - oldDate.getTimeInMillis();
        
        return diffInMillis / (60 * 1000) % 60;
    }

    public static long diffInHours(String dateToCompare) {
        Calendar oldDate = Calendar.getInstance(); // old datetime or expiration date

        try {
            oldDate.setTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateToCompare));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        Calendar now = Calendar.getInstance(); // current datetime
        long diffInMillis = now.getTimeInMillis() - oldDate.getTimeInMillis();
        
        return diffInMillis / (60 * 60 * 1000) % 24;
    }
        
    public static String secondsToTime(long timeseconds) {
        StringBuilder cooldownMsg = new StringBuilder();
        int years = (int) (timeseconds / (60 * 60 * 24 * 365));
    
        if (years > 0) {
            cooldownMsg.append(years).append(" years, ");
            timeseconds = timeseconds % (60 * 60 * 24 * 365);
        }
            
        int weeks = (int) (timeseconds / (60 * 60 * 24 * 365));
    
        if (weeks > 0) {
            cooldownMsg.append(weeks).append(" weeks, ");
            timeseconds = timeseconds % (60 * 60 * 24 * 7);
        }
    
        int days = (int) (timeseconds / (60 * 60 * 24));
            
        if (days > 0) {
            cooldownMsg.append(days).append(" days, ");
            timeseconds = timeseconds % (60 * 60 * 24);
        }
    
        int hours = (int) (timeseconds / (60 * 60));
    
        if (hours > 0) {
            cooldownMsg.append(hours).append(" hours, ");
            timeseconds = timeseconds % (60 * 60);
        }
    
        int minutes = (int) (timeseconds / (60));
    
        if (minutes > 0) {
            cooldownMsg.append(minutes).append(" minutes, ");
            timeseconds = timeseconds % (60);
        }
    
        if (timeseconds > 0) {
            cooldownMsg.append(timeseconds).append(" seconds");
        }
    
        String str = cooldownMsg.toString();
    
        if (str.endsWith(", ")) {
            str = str.substring(0, str.length() - 2);
        }
            
        if (str.equals("")) {
            str = "no time";
        }
            
        return str;
    }
    
    public static boolean isAuthorDev(User author) {
        Long authorId = author.getIdLong();
        return authorId.equals(Long.parseLong(Config.get("dev_id")));
    }
    
    public static boolean isAuthorAdmin(User author, TextChannel channel) {
        return LemiDbManager.INS.isAuthorAdmin(author, channel);
    }
    
    public static boolean isAuthorMod(User author, TextChannel channel) {
        return LemiDbManager.INS.isAuthorMod(author, channel);
    }

    public static boolean isAuthorDev(Member member) {
        Long memberId = member.getIdLong();
        return memberId.equals(Long.parseLong(Config.get("dev_id")));
    }
    
    public static boolean isAuthorAdmin(Member member, SlashCommandInteractionEvent event) {
        return LemiDbManager.INS.isAuthorAdmin(member, event);
    }
    
    public static boolean isAuthorMod(Member member, SlashCommandInteractionEvent event) {
        return LemiDbManager.INS.isAuthorMod(member, event);
    }
    
    public static String getOrdinalNum(int num) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
    
        switch (num % 100) {
            case 11:
            case 12:
            case 13:
                return num + "th";
            default:
                return num + suffixes[num % 10];
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection... collections) {
        for (Collection collection : collections) {
            if (collection == null || collection.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    public static String parsePerms(Permission[] perms) {
        String neededPerms = Arrays.stream(perms)
            .map(Permission::getName)
            .collect(Collectors.joining(", "));
    
        return StringUtils.replaceLast(neededPerms, ", ", " and ");
    }
    
    public static String handleAliases(String[] aliases, String cmdName) {
        if (aliases == null) {
            return "No aliases available!";
        }
    
        String modifiedAliases = Arrays.stream(aliases).collect(Collectors.joining(","));

        return StringUtils.replaceLast(modifiedAliases, ", ", " and ");
    }
}