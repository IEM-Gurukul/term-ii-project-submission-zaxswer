package com.course.quiz;

import java.util.Collections;
import java.util.List;

public class QuizQuestion {
    private final String prompt;
    private final List<String> options;
    private final int correctOptionIndex;

    public QuizQuestion(String prompt, List<String> options, int correctOptionIndex) {
        this.prompt = prompt;
        this.options = Collections.unmodifiableList(options);
        this.correctOptionIndex = correctOptionIndex;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
}
