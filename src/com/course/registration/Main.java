package com.course.registration;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.service.CourseRegistrationSystem;
import com.course.registration.service.RegistrationResult;
import com.course.registration.ui.CourseRegistrationFrame;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        CourseRegistrationSystem system = new CourseRegistrationSystem();
        seedData(system);

        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            runConsole(system);
        } else {
            launchGui(system);
        }
    }

    private static void launchGui(CourseRegistrationSystem system) {
        SwingUtilities.invokeLater(() -> {
            CourseRegistrationFrame frame = new CourseRegistrationFrame(system);
            frame.setVisible(true);
        });
    }

    private static void runConsole(CourseRegistrationSystem system) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    showCourses(system);
                    break;
                case "2":
                    showStudents(system);
                    break;
                case "3":
                    registerStudent(system, scanner);
                    break;
                case "4":
                    dropStudent(system, scanner);
                    break;
                case "5":
                    showStudentSchedule(system, scanner);
                    break;
                case "6":
                    completeCourse(system, scanner);
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting Course Registration System.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("===== Course Registration System =====");
        System.out.println("1. View all courses");
        System.out.println("2. View all students");
        System.out.println("3. Register student for a course");
        System.out.println("4. Drop student from a course");
        System.out.println("5. View a student's current schedule");
        System.out.println("6. Mark a student's course as completed");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    private static void showCourses(CourseRegistrationSystem system) {
        List<Course> courses = system.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }

        for (Course course : courses) {
            System.out.println(course);
        }
    }

    private static void showStudents(CourseRegistrationSystem system) {
        List<Student> students = system.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        for (Student student : students) {
            System.out.println(student);
        }
    }

    private static void registerStudent(CourseRegistrationSystem system, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();

        RegistrationResult result = system.registerStudentForCourse(studentId, courseId);
        System.out.println(result.getMessage());
    }

    private static void dropStudent(CourseRegistrationSystem system, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();

        RegistrationResult result = system.dropStudentFromCourse(studentId, courseId);
        System.out.println(result.getMessage());
    }

    private static void showStudentSchedule(CourseRegistrationSystem system, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();

        Student student = system.getStudent(studentId);
        if (student == null) {
            System.out.println("Student not found: " + studentId);
            return;
        }

        List<Course> courses = system.getStudentCurrentCourses(studentId);
        if (courses.isEmpty()) {
            System.out.println("No active courses for " + studentId);
        } else {
            System.out.println("Current courses for " + studentId + ":");
            for (Course course : courses) {
                System.out.println("- " + course.getCourseId() + " : " + course.getTitle());
            }
        }

        Set<String> completed = student.getCompletedCourses();
        System.out.println("Completed courses: " + (completed.isEmpty() ? "None" : completed));
    }

    private static void completeCourse(CourseRegistrationSystem system, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();

        RegistrationResult result = system.markCourseCompleted(studentId, courseId);
        System.out.println(result.getMessage());
    }

    private static void seedData(CourseRegistrationSystem system) {
        system.addCourse(new Course("CS101", "Introduction to Programming", 3, Set.of()));
        system.addCourse(new Course("CS102", "Data Structures", 3, Set.of("CS101")));
        system.addCourse(new Course("CS201", "Database Systems", 2, Set.of("CS101")));
        system.addCourse(new Course("CS301", "Algorithms", 2, Set.of("CS102")));

        system.addStudent(new Student("S001", "Alice", 3));
        system.addStudent(new Student("S002", "Bob", 2));
        system.addStudent(new Student("S003", "Charlie", 2));
    }
}
