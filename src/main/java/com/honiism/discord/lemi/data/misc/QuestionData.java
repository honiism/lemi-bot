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

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionData {

    @JsonProperty("question_id")
    private final String questionId;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("author_id")
    private final long authorId;

    @JsonProperty("rating")
    private String rating;
    @JsonProperty("question")
    private String question;

    public QuestionData(@JsonProperty("question_id") String questionId,
            @JsonProperty("type") String type, @JsonProperty("author_id") long authorId) {
        this.questionId = questionId;
        this.type = type;
        this.authorId = authorId;
    }

    public String getQuestionId() {
        return questionId;
    }
    
    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public String getType() {
        return type;
    }

    public long getAuthorId() {
        return authorId;
    }
}