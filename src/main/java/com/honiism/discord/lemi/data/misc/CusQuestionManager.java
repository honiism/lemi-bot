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

package com.honiism.discord.lemi.data.misc;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.data.database.managers.LemiDbManager;

public class CusQuestionManager {

    private final long guildId;
    private final CusQuestionData questionData;

    public CusQuestionManager(long guildId, String dataJson) throws JsonMappingException, JsonProcessingException {
        this.guildId = guildId;
        this.questionData = Lemi.getInstance().getObjectMapper().readValue(dataJson, CusQuestionData.class);
    }

    public long getGuildId() {
        return guildId;
    }

    public CusQuestionData getData() {
        return questionData;
    }

    public List<QuestionData> getQuestions() {
        if (LemiDbManager.INS.hasQuestionData(guildId)) {
            LemiDbManager.INS.addQuestionData(guildId);
        }
        return getData().getQuestions();
    }

    public void addQuestion(QuestionData question) {
        getQuestions().add(question);

        getQuestions().forEach(questionA -> System.out.println(questionA.getQuestion()));

        try {
            LemiDbManager.INS.updateQuestionData(guildId, Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void deleteQuestion(QuestionData question) {
        getQuestions().remove(question);

        try {
            LemiDbManager.INS.updateQuestionData(guildId, Lemi.getInstance().getObjectMapper().writeValueAsString(getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public QuestionData getQuestionById(String id) {
        return getQuestions().stream()
            .filter(questionData -> questionData.getQuestionId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public QuestionData getRandomQuestion() {
        Random r = new Random();

        return getQuestions().get(r.nextInt(getQuestions().size()));
    }

    public QuestionData getRandomQuestion(String type, String rating) {
        Random r = new Random();

        List<QuestionData> questions = getQuestions().stream()
            .filter(question -> question.getType().equalsIgnoreCase(type) 
                             && question.getRating().equalsIgnoreCase(rating))
            .collect(Collectors.toList());

        return questions.get(r.nextInt(questions.size()));
    }
}