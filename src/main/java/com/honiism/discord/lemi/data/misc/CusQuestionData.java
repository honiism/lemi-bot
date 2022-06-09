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

package com.honiism.discord.lemi.data.misc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CusQuestionData {

    @JsonProperty("guild_id")
    private final long guildId;
    @JsonProperty("author_id")
    private final long authorId;

    private List<QuestionData> questions = new ArrayList<>();

    public CusQuestionData(@JsonProperty("guild_id") long guildId, @JsonProperty("author_id" )long authorId) {
        this.guildId = guildId;
        this.authorId = authorId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public List<QuestionData> getQuestions() {
        return questions;
    }

    public void addQuestion(QuestionData question) {
        getQuestions().add(question);
    }

    public void setQuestions(List<QuestionData> questions) {
        this.questions = questions;
    }
}