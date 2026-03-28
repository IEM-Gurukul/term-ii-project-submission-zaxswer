package com.course.registration.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Course {
    private final String courseId;
    private final String title;
    private final int capacity;
    private final Set<String> prerequisites;
    private final String requiredVideoUrl;
    private int enrolledCount;

    public Course(String courseId, String title, int capacity, Set<String> prerequisites) {
        this(courseId, title, capacity, prerequisites, null);
    }

    public Course(String courseId, String title, int capacity, Set<String> prerequisites, String requiredVideoUrl) {
        this.courseId = courseId;
        this.title = title;
        this.capacity = capacity;
        this.prerequisites = new HashSet<>(prerequisites);
        this.requiredVideoUrl = normalizeVideoUrl(requiredVideoUrl);
        this.enrolledCount = 0;
    }

    private String normalizeVideoUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public Set<String> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }

    public String getRequiredVideoUrl() {
        return requiredVideoUrl;
    }

    public boolean requiresVideoWatch() {
        return requiredVideoUrl != null;
    }

    public boolean hasAvailableSeat() {
        return enrolledCount < capacity;
    }

    public void enroll() {
        if (!hasAvailableSeat()) {
            throw new IllegalStateException("Course is full: " + courseId);
        }
        enrolledCount++;
    }

    public void drop() {
        if (enrolledCount <= 0) {
            throw new IllegalStateException("No students enrolled in: " + courseId);
        }
        enrolledCount--;
    }

    @Override
    public String toString() {
        return String.format("%s - %s | Seats: %d/%d | Prerequisites: %s | Video Required: %s",
            courseId,
            title,
            enrolledCount,
            capacity,
            prerequisites.isEmpty() ? "None" : prerequisites,
            requiresVideoWatch() ? "Yes" : "No");
    }
}
