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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lemi-Bot.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.honiism.discord.lemi.data.embed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbedData {

    @JsonProperty("type")
    private final String type;
   
    private String id;
    private String content;

    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String desc;
    @JsonProperty("color")
    private int color;
    @JsonProperty("image")
    private ImageData image;
    @JsonProperty("thumbnail")
    private ThumbnailData thumbnail;
    @JsonProperty("author")
    private AuthorData author;
    @JsonProperty("footer")
    private FooterData footer;

    public EmbedData(@JsonProperty("type") String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }
 
    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setColor(String color) {
        this.color = Integer.decode(color);
    }

    public int getColor() {
        return color;
    }

    public void setImage(ImageData image) {
        this.image = image;
    }

    public ImageData getImage() {
        return image;
    }

    public void setThumbnail(ThumbnailData thumbnail) {
        this.thumbnail = thumbnail;
    }

    public ThumbnailData getThumbnail() {
        return thumbnail;
    }

    public void setAuthor(AuthorData author) {
        this.author = author;
    }

    public AuthorData getAuthor() {
        return author;
    }

    public void setFooter(FooterData footer) {
        this.footer = footer;
    }

    public FooterData getFooter() {
        return footer;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
