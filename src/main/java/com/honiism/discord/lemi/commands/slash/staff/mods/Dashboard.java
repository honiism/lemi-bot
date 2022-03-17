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

package com.honiism.discord.lemi.commands.slash.staff.mods;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.handler.UserCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class Dashboard extends SlashCmd {

    private HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public Dashboard() {
        setCommandData(Commands.slash("dashboard", "View details about Lemi's memory usage, uptime, etc."));
        setUsage("/mods dashboard");
        setCategory(CommandCategory.MODS);
        setUserCategory(UserCategory.MODS);
        setUserPerms(new Permission[] {Permission.MESSAGE_MANAGE});
        setBotPerms(new Permission[] {Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY});
        
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
        
            EmbedBuilder dashboardEmbed = new EmbedBuilder()
                .setTitle(":tulip: Lemi Dashboard Info!")
                .setThumbnail(event.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .addField(":sunflower: Memory info", getMemoryInfo(), false)
                .addField(":seedling: OS Info", getOSInfo(), false)
                .addField(":snowflake: CPU Info", getCPUInfo(), false)
                .addField(":grapes: Uptime", getUptimeInfo(), false)
                .setColor(0xffd1dc)
                .setTimestamp(Instant.now());

            hook.sendMessageEmbeds(dashboardEmbed.build()).queue();
                
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

    private String getMemoryInfo() {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1000;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1000;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1000;

        String info = "- Max memory: `" + maxMemory + "`\r\n"
                + "- Total memory: `" + totalMemory + "`\r\n"
                + "- Free memory: `" + freeMemory + "`";

        return info;
    }

    private String getOSInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        
        String info = "- OS Name: `" + osName + "`\r\n"
                + "- OS Version: `" + osVersion + "`\r\n"
                + "- OS architecture `" + osArch + "`";

        return info;
    }

    private String getCPUInfo() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();

        CentralProcessor.ProcessorIdentifier processorIdentifier = processor.getProcessorIdentifier();

        String vendor = processorIdentifier.getVendor();
        String name = processorIdentifier.getName();
        String id = processorIdentifier.getIdentifier();
        String microArch = processorIdentifier.getMicroarchitecture();
        
        long frequencyHz = processorIdentifier.getVendorFreq();
        double frequencyGHz = processorIdentifier.getVendorFreq() / 1000000000.0;

        String info = "- Processor vendor: `" + vendor + "`\r\n"
                + "- Processor name: `" + name + "`\r\n"
                + "- Processor identifier: `" + id + "`\r\n"
                + "- Processor microarchitecture: `" + microArch + "`\r\n"
                + "- Frequency (Hz): `" + frequencyHz + "`\r\n"
                + "- Frequency (GHz): `" + frequencyGHz + "`\r\n";

        return info;
    }

    private String getUptimeInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        
        long uptime = runtimeMXBean.getUptime();
        long uptimeSecs = uptime / 1000;
        long hours = uptimeSecs / (60 * 60);
        long minutes = (uptimeSecs / 60) - (hours * 60);
        long seconds = uptimeSecs % 60;

        String info = hours + " hour(s), " 
                + minutes + " minute(s), " 
                + seconds + " second(s).";
        
        return info;
    }
}