package com.course.registration;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.model.User;
import com.course.registration.service.AuthenticationManager;
import com.course.registration.service.CourseRegistrationSystem;
import com.course.registration.ui.LoginDialog;
import com.course.registration.ui.StudentPortalFrame;
import com.course.registration.ui.TeacherPortalFrame;

import javax.swing.SwingUtilities;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        CourseRegistrationSystem system = new CourseRegistrationSystem();
        seedData(system);
        launchGui(system);
    }

    private static void launchGui(CourseRegistrationSystem system) {
        SwingUtilities.invokeLater(() -> showLoginAndPortal(system));
    }

    private static void showLoginAndPortal(CourseRegistrationSystem system) {
        AuthenticationManager authManager = new AuthenticationManager();
        User authenticatedUser = LoginDialog.showLoginDialog(authManager);

        if (authenticatedUser != null) {
            Runnable logoutCallback = () -> SwingUtilities.invokeLater(() -> showLoginAndPortal(system));

            if (authenticatedUser.getRole() == User.Role.STUDENT) {
                StudentPortalFrame frame = new StudentPortalFrame(system, authenticatedUser, logoutCallback);
                frame.setVisible(true);
            } else if (authenticatedUser.getRole() == User.Role.TEACHER) {
                TeacherPortalFrame frame = new TeacherPortalFrame(system, authenticatedUser, logoutCallback);
                frame.setVisible(true);
            }
        }
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
