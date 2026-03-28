package com.course.registration.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Student {
    private final String studentId;
    private final String name;
    private final int maxConcurrentCourses;
    private final Set<String> completedCourses;
    private final Set<String> currentCourses;

    public Student(String studentId, String name, int maxConcurrentCourses) {
        this.studentId = studentId;
        this.name = name;
        this.maxConcurrentCourses = maxConcurrentCourses;
        this.completedCourses = new HashSet<>();
        this.currentCourses = new HashSet<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public int getMaxConcurrentCourses() {
        return maxConcurrentCourses;
    }

    public Set<String> getCompletedCourses() {
        return Collections.unmodifiableSet(completedCourses);
    }

    public Set<String> getCurrentCourses() {
        return Collections.unmodifiableSet(currentCourses);
    }

    public boolean isAlreadyEnrolled(String courseId) {
        return currentCourses.contains(courseId);
    }

    public boolean canTakeMoreCourses() {
        return currentCourses.size() < maxConcurrentCourses;
    }

    public boolean hasCompleted(String courseId) {
        return completedCourses.contains(courseId);
    }

    public void enrollInCourse(String courseId) {
        if (!canTakeMoreCourses()) {
            throw new IllegalStateException("Student reached max course load: " + studentId);
        }
        currentCourses.add(courseId);
    }

    public void dropCourse(String courseId) {
        currentCourses.remove(courseId);
    }

    public void markCourseCompleted(String courseId) {
        currentCourses.remove(courseId);
        completedCourses.add(courseId);
    }

    @Override
    public String toString() {
        return String.format("%s - %s | Current: %d/%d | Completed: %s", studentId, name,
                currentCourses.size(), maxConcurrentCourses,
                completedCourses.isEmpty() ? "None" : completedCourses);
    }
}
