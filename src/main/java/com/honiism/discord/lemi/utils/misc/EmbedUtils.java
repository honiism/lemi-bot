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

package com.honiism.discord.lemi.utils.misc;

import java.awt.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EmbedUtils {
    private static final int DEFAULT_COLOR = 0xffd1dc;

    private static final int SUCCESS_COLOR = 0x3EB489;
    private static final int ERROR_COLOR = 0xc30a06;
    private static final int WARNING_COLOR = 0xFFEA17;

    private static final String SUCCESS_UNICODE = Emojis.CHECK_MARK;
    private static final String ERROR_UNICODE = Emojis.CROSS_MARK;
    private static final String WARNING_UNICODE = Emojis.EXCLAMATION_MARK;

    public static MessageEmbed getSimpleEmbed(CharSequence content) {
        return new EmbedBuilder()
            .setColor(0xffd1dc)
            .setDescription(content)
            .build();
    }

    public static MessageEmbed defaultEmbed(String content, Color color) {
        return new EmbedBuilder()
            .setDescription(content)
            .setColor(color)
            .build();
    } 

    public static MessageEmbed defaultEmbed(String content, int color) {
        return new EmbedBuilder()
            .setDescription(content)
            .setColor(color)
            .build();
    }

    public static MessageEmbed defaultEmbed(String content, byte r, byte g, byte b) {
        return new EmbedBuilder()
            .setDescription(content)
            .setColor(rgbToInt(r, g, b))
            .build();
    }


    public static MessageEmbed defaultEmbed(String content) {
        return new EmbedBuilder()
            .setDescription(content)
            .setColor(DEFAULT_COLOR)
            .build();
    }

    public static MessageEmbed errorEmbed(String content) {
        return new EmbedBuilder()
            .setDescription(ERROR_UNICODE + " " + content)
            .setColor(ERROR_COLOR)
            .build();
    }

    public static MessageEmbed warningEmbed(String content) {
        return new EmbedBuilder()
            .setDescription(WARNING_UNICODE + " " + content)
            .setColor(WARNING_COLOR)
            .build();
    }

    public static MessageEmbed successEmbed(String content) {
        return new EmbedBuilder()
            .setDescription(SUCCESS_UNICODE + " " + content)
            .setColor(SUCCESS_COLOR)
            .build();
    }

    public static EmbedBuilder errorEmbedBuilder(String content) {
        return new EmbedBuilder()
            .setDescription(ERROR_UNICODE + " " + content)
            .setColor(ERROR_COLOR);
    }

    public static EmbedBuilder warningEmbedBuilder(String content)
    {
        return new EmbedBuilder()
                .setDescription(WARNING_UNICODE + " " + content)
                .setColor(WARNING_COLOR);
    }

    public static EmbedBuilder successEmbedBuilder(String content) {
        return new EmbedBuilder()
            .setDescription(SUCCESS_UNICODE + " " + content)
            .setColor(SUCCESS_COLOR);
    }

    public static int rgbToInt(byte r, byte g, byte b) {
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;

        return rgb;
    }

    public static Color intToColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        
        return new Color(red, green, blue);
    }
}