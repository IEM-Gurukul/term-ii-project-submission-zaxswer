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
import java.util.List;
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
        system.addCourse(new Course("CS101", "Introduction to Programming", 3, Set.of(), List.of(
            "https://www.youtube.com/watch?v=8PopR3x-VMY",
            "https://www.youtube.com/watch?v=eIrMbAQSU34",
            "https://www.youtube.com/watch?v=grEKMHGYyns",
            "https://www.youtube.com/watch?v=1Y43xbfxP4Y",
            "https://www.youtube.com/watch?v=2qZABrY3S5s",
            "https://www.youtube.com/watch?v=GoXwIVyNvX0",
            "https://www.youtube.com/watch?v=Qgl81fPcLc8",
            "https://www.youtube.com/watch?v=kMJiA8Qe5zM"
        )));
        system.addCourse(new Course("CS102", "Data Structures", 3, Set.of("CS101"), List.of(
            "https://www.youtube.com/watch?v=RBSGKlAvoiM",
            "https://www.youtube.com/watch?v=zg9ih6SVACc",
            "https://www.youtube.com/watch?v=bum_19loj9A",
            "https://www.youtube.com/watch?v=09_LlHjoEiY",
            "https://www.youtube.com/watch?v=B31LgI4Y4DQ",
            "https://www.youtube.com/watch?v=7m1DMYAbdiY",
            "https://www.youtube.com/watch?v=0IAPZzGSbME",
            "https://www.youtube.com/watch?v=2ZLl8GAk1X4"
        )));
        system.addCourse(new Course("CS201", "Database Systems", 2, Set.of("CS101"), List.of(
            "https://www.youtube.com/watch?v=HXV3zeQKqGY",
            "https://www.youtube.com/watch?v=7S_tz1z_5bA",
            "https://www.youtube.com/watch?v=ztHopE5Wnpc",
            "https://www.youtube.com/watch?v=quTtmqbF0FM",
            "https://www.youtube.com/watch?v=PrhRtxo7-fI",
            "https://www.youtube.com/watch?v=z5fUkck_RZM",
            "https://www.youtube.com/watch?v=wgRwITQHszU",
            "https://www.youtube.com/watch?v=5OdVJbNCSso"
        )));
        system.addCourse(new Course("CS301", "Algorithms", 2, Set.of("CS102"), List.of(
            "https://www.youtube.com/watch?v=8hly31xKli0",
            "https://www.youtube.com/watch?v=Hoixgm4-P4M",
            "https://www.youtube.com/watch?v=Vtckgz38QHs",
            "https://www.youtube.com/watch?v=EFg3u_E6eHU",
            "https://www.youtube.com/watch?v=Yk0z8qYd5a4",
            "https://www.youtube.com/watch?v=ZA-tUyM_y7s",
            "https://www.youtube.com/watch?v=Gc4snm2aJfQ",
            "https://www.youtube.com/watch?v=ddTC4Zovtbc"
        )));

        system.addStudent(new Student("S001", "Alice", 3));
        system.addStudent(new Student("S002", "Bob", 2));
        system.addStudent(new Student("S003", "Charlie", 2));
    }
}
