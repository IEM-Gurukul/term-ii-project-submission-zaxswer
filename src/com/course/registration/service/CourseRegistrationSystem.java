package com.course.registration.service;

import com.learning.CourseVideoProgressTracker;
import com.learning.StudentTopicSelectionStore;
import com.learning.StudentWeeklyProgressStore;
import com.course.registration.model.Course;
import com.course.registration.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CourseRegistrationSystem {
    private final Map<String, Course> courses;
    private final Map<String, Student> students;
    private final CourseVideoProgressTracker videoProgressTracker;
    private final StudentTopicSelectionStore topicSelectionStore;
    private final StudentWeeklyProgressStore weeklyProgressStore;

    public CourseRegistrationSystem() {
        this.courses = new HashMap<>();
        this.students = new HashMap<>();
        this.videoProgressTracker = new CourseVideoProgressTracker();
        this.topicSelectionStore = new StudentTopicSelectionStore();
        this.weeklyProgressStore = new StudentWeeklyProgressStore();
    }

    public void addCourse(Course course) {
        courses.put(course.getCourseId(), course);
    }

    public void addStudent(Student student) {
        students.put(student.getStudentId(), student);
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>(courses.values());
        list.sort(Comparator.comparing(Course::getCourseId));
        return list;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>(students.values());
        list.sort(Comparator.comparing(Student::getStudentId));
        return list;
    }

    public RegistrationResult registerStudentForCourse(String studentId, String courseId) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        Course course = courses.get(courseId);
        if (course == null) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        if (student.isAlreadyEnrolled(courseId)) {
            return RegistrationResult.failure("Student is already enrolled in " + courseId);
        }

        if (course.requiresVideoWatch() && !videoProgressTracker.hasWatched(studentId, courseId)) {
            return RegistrationResult.failure("Watch the required course video before enrolling in " + courseId + ".");
        }

        if (!student.canTakeMoreCourses()) {
            return RegistrationResult.failure("Student has reached max course load.");
        }

        if (!course.hasAvailableSeat()) {
            return RegistrationResult.failure("Course is full.");
        }

        Set<String> completed = student.getCompletedCourses();
        for (String prerequisite : course.getPrerequisites()) {
            if (!completed.contains(prerequisite)) {
                return RegistrationResult.failure("Missing prerequisite: " + prerequisite);
            }
        }

        student.enrollInCourse(courseId);
        course.enroll();
        return RegistrationResult.success("Registration successful for " + studentId + " in " + courseId);
    }

    public RegistrationResult markCourseVideoWatched(String studentId, String courseId) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        Course course = courses.get(courseId);
        if (course == null) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        if (!course.requiresVideoWatch()) {
            return RegistrationResult.failure("No required video is configured for " + courseId + ".");
        }

        videoProgressTracker.markWatched(studentId, courseId);
        return RegistrationResult.success("Video marked as watched for " + studentId + " in " + courseId);
    }

    public boolean hasWatchedCourseVideo(String studentId, String courseId) {
        Course course = courses.get(courseId);
        if (course == null) {
            return false;
        }
        if (!course.requiresVideoWatch()) {
            return true;
        }
        return videoProgressTracker.hasWatched(studentId, courseId);
    }

    public String getCourseVideoUrl(String courseId) {
        Course course = courses.get(courseId);
        if (course == null) {
            return null;
        }
        return course.getRequiredVideoUrl();
    }

    public RegistrationResult selectTopic(String studentId, String courseId) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        if (!courses.containsKey(courseId)) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        topicSelectionStore.saveSelectedTopic(studentId, courseId);
        return RegistrationResult.success("Selected topic " + courseId + " for " + studentId);
    }

    public String getSelectedTopic(String studentId) {
        return topicSelectionStore.getSelectedTopic(studentId);
    }

    public RegistrationResult updateStudentCurrentWeek(String studentId, String courseId, int weekNumber) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        if (!courses.containsKey(courseId)) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        if (weekNumber < 1 || weekNumber > 8) {
            return RegistrationResult.failure("Week number must be between 1 and 8.");
        }

        weeklyProgressStore.saveCurrentWeek(studentId, courseId, weekNumber);
        return RegistrationResult.success("Progress updated to week " + weekNumber + " for " + studentId + ".");
    }

    public int getStudentCurrentWeek(String studentId, String courseId) {
        return weeklyProgressStore.getCurrentWeek(studentId, courseId);
    }

    public Course getCourseById(String courseId) {
        return courses.get(courseId);
    }

    public RegistrationResult dropStudentFromCourse(String studentId, String courseId) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        Course course = courses.get(courseId);
        if (course == null) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        if (!student.isAlreadyEnrolled(courseId)) {
            return RegistrationResult.failure("Student is not enrolled in " + courseId);
        }

        student.dropCourse(courseId);
        course.drop();
        return RegistrationResult.success("Dropped " + studentId + " from " + courseId);
    }

    public RegistrationResult markCourseCompleted(String studentId, String courseId) {
        Student student = students.get(studentId);
        if (student == null) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }

        if (!courses.containsKey(courseId)) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }

        if (!student.isAlreadyEnrolled(courseId)) {
            return RegistrationResult.failure("Student must be currently enrolled in " + courseId + " to complete it.");
        }

        student.markCourseCompleted(courseId);
        Course course = courses.get(courseId);
        course.drop();
        return RegistrationResult.success("Marked " + courseId + " as completed for " + studentId);
    }

    public List<Course> getStudentCurrentCourses(String studentId) {
        Student student = students.get(studentId);
        List<Course> result = new ArrayList<>();
        if (student == null) {
            return result;
        }

        for (String courseId : student.getCurrentCourses()) {
            Course course = courses.get(courseId);
            if (course != null) {
                result.add(course);
            }
        }

        result.sort(Comparator.comparing(Course::getCourseId));
        return result;
    }

    public Student getStudent(String studentId) {
        return students.get(studentId);
    }
}
