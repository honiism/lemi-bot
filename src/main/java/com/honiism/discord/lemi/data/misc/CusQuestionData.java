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