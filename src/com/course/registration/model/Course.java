package com.course.registration.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Course {
    private final String courseId;
    private final String title;
    private final int capacity;
    private final Set<String> prerequisites;
    private final String requiredVideoUrl;
    private final List<String> weeklyVideoUrls;
    private int enrolledCount;

    public Course(String courseId, String title, int capacity, Set<String> prerequisites) {
        this(courseId, title, capacity, prerequisites, (String) null);
    }

    public Course(String courseId, String title, int capacity, Set<String> prerequisites, String requiredVideoUrl) {
        this.courseId = courseId;
        this.title = title;
        this.capacity = capacity;
        this.prerequisites = new HashSet<>(prerequisites);
        this.requiredVideoUrl = normalizeVideoUrl(requiredVideoUrl);
        this.weeklyVideoUrls = buildWeeklyUrls(this.requiredVideoUrl);
        this.enrolledCount = 0;
    }

    public Course(String courseId, String title, int capacity, Set<String> prerequisites, List<String> weeklyVideoUrls) {
        this.courseId = courseId;
        this.title = title;
        this.capacity = capacity;
        this.prerequisites = new HashSet<>(prerequisites);
        this.weeklyVideoUrls = normalizeWeeklyUrls(weeklyVideoUrls);
        this.requiredVideoUrl = this.weeklyVideoUrls.isEmpty() ? null : this.weeklyVideoUrls.get(0);
        this.enrolledCount = 0;
    }

    private String normalizeVideoUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url;
    }

    private List<String> normalizeWeeklyUrls(List<String> weeklyUrls) {
        if (weeklyUrls == null || weeklyUrls.isEmpty()) {
            return Collections.emptyList();
        }

        java.util.ArrayList<String> normalized = new java.util.ArrayList<>();
        for (String url : weeklyUrls) {
            String clean = normalizeVideoUrl(url);
            if (clean != null) {
                normalized.add(clean);
            }
        }

        return Collections.unmodifiableList(normalized);
    }

    private List<String> buildWeeklyUrls(String fallbackUrl) {
        if (fallbackUrl == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(java.util.Collections.nCopies(8, fallbackUrl));
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
        return !weeklyVideoUrls.isEmpty();
    }

    public String getVideoUrlForWeek(int weekNumber) {
        if (weeklyVideoUrls.isEmpty()) {
            return null;
        }
        int index = Math.max(0, Math.min(weeklyVideoUrls.size() - 1, weekNumber - 1));
        return weeklyVideoUrls.get(index);
    }

    public List<String> getWeeklyVideoUrls() {
        return weeklyVideoUrls;
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
