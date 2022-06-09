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