package com.learning;

import java.util.prefs.Preferences;

public class StudentTopicSelectionStore {
    private final Preferences preferences;

    public StudentTopicSelectionStore() {
        this.preferences = Preferences.userRoot().node("course_registration_selected_topics");
    }

    public void saveSelectedTopic(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank()) {
            return;
        }

        if (courseId == null || courseId.isBlank()) {
            preferences.remove(studentId);
            return;
        }

        preferences.put(studentId, courseId);
    }

    public String getSelectedTopic(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return null;
        }
        return preferences.get(studentId, null);
    }
}