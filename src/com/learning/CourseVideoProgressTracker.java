package com.learning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CourseVideoProgressTracker {
    private final Map<String, Set<String>> watchedVideosByStudent;

    public CourseVideoProgressTracker() {
        this.watchedVideosByStudent = new HashMap<>();
    }

    public void markWatched(String studentId, String courseId) {
        watchedVideosByStudent
                .computeIfAbsent(studentId, key -> new HashSet<>())
                .add(courseId);
    }

    public boolean hasWatched(String studentId, String courseId) {
        Set<String> watched = watchedVideosByStudent.get(studentId);
        return watched != null && watched.contains(courseId);
    }
}
