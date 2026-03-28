package com.learning;

import java.util.prefs.Preferences;

public class StudentWeeklyProgressStore {
    private final Preferences preferences;

    public StudentWeeklyProgressStore() {
        this.preferences = Preferences.userRoot().node("course_registration_week_progress");
    }

    public void saveCurrentWeek(String studentId, String courseId, int weekNumber) {
        if (studentId == null || studentId.isBlank() || courseId == null || courseId.isBlank()) {
            return;
        }

        int boundedWeek = Math.max(1, Math.min(8, weekNumber));
        String key = buildKey(studentId, courseId);
        int existing = preferences.getInt(key, 1);
        preferences.putInt(key, Math.max(existing, boundedWeek));
    }

    public int getCurrentWeek(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank() || courseId == null || courseId.isBlank()) {
            return 1;
        }
        return preferences.getInt(buildKey(studentId, courseId), 1);
    }

    private String buildKey(String studentId, String courseId) {
        return studentId + "::" + courseId;
    }
}
