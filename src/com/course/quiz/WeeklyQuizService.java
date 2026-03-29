package com.course.quiz;

import java.util.List;

public class WeeklyQuizService {
    public List<QuizQuestion> getQuestionsForWeek(String courseId, int weekNumber) {
        if (courseId == null || weekNumber < 1 || weekNumber > 8) {
            return List.of();
        }

        String focus = getCourseFocus(courseId);
        if (focus == null) {
            return List.of();
        }

        return List.of(
                new QuizQuestion(
                        "Week " + weekNumber + " (" + courseId + "): In " + focus + ", which option best helps solve a problem efficiently?",
                        List.of("Choose a suitable algorithm", "Memorize syntax only", "Avoid planning", "Skip testing"),
                        0
                ),
                new QuizQuestion(
                        "Week " + weekNumber + " (" + courseId + "): Which practice most improves code quality?",
                        List.of("Use clear structure and naming", "Write everything in one method", "Ignore edge cases", "Never refactor"),
                        0
                ),
                new QuizQuestion(
                        "Week " + weekNumber + " (" + courseId + "): What should be done after implementing a solution?",
                        List.of("Run tests and validate output", "Immediately deploy without checks", "Delete sample inputs", "Skip reviewing results"),
                        0
                )
        );
    }

    private String getCourseFocus(String courseId) {
        switch (courseId) {
            case "CS101":
                return "programming fundamentals";
            case "CS102":
                return "data structures";
            case "CS201":
                return "database systems";
            case "CS301":
                return "algorithms";
            default:
                return null;
        }
    }
}
