package com.course.registration.ui;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.model.User;
import com.course.registration.service.CourseRegistrationSystem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class TeacherPortalFrame extends JFrame {
    private final CourseRegistrationSystem system;
    private final User currentUser;
    private final JComboBox<String> courseSelector;
    private final DefaultTableModel enrollmentTableModel;
    private final JTextArea courseDetailsArea;
    private final Runnable onLogout;

    public TeacherPortalFrame(CourseRegistrationSystem system, User currentUser, Runnable onLogout) {
        this.system = system;
        this.currentUser = currentUser;
        this.onLogout = onLogout;

        setTitle("Course Registration - Teacher Portal | " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel headerLabel = new JLabel("Teacher: " + currentUser.getUsername() + " - Course Enrollment View");
        headerLabel.setFont(headerLabel.getFont().deriveFont(14f));

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel courseLabel = new JLabel("Select Course:");
        courseSelector = new JComboBox<>();
        courseSelector.setPreferredSize(new Dimension(250, 28));
        courseSelector.addActionListener(e -> refreshEnrollmentTable());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAll());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> doLogout());

        selectorPanel.add(courseLabel);
        selectorPanel.add(courseSelector);
        selectorPanel.add(refreshButton);
        selectorPanel.add(logoutButton);

        enrollmentTableModel = new DefaultTableModel(new Object[]{
                "Student ID", "Student Name", "Max Load", "Current Load"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable enrollmentTable = new JTable(enrollmentTableModel);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tablePane = new JScrollPane(enrollmentTable);
        tablePane.setBorder(BorderFactory.createTitledBorder("Students Enrolled in Selected Course"));

        courseDetailsArea = new JTextArea(8, 50);
        courseDetailsArea.setEditable(false);
        courseDetailsArea.setLineWrap(true);
        courseDetailsArea.setWrapStyleWord(true);
        courseDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane detailsPane = new JScrollPane(courseDetailsArea);
        detailsPane.setBorder(BorderFactory.createTitledBorder("Course Details"));

        JPanel southPanel = new JPanel(new GridLayout(1, 1, 10, 10));
        southPanel.add(detailsPane);

        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(selectorPanel, BorderLayout.BEFORE_FIRST_LINE);
        mainPanel.add(tablePane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        refreshAll();
    }

    private void refreshAll() {
        refreshCourseSelector();
        refreshEnrollmentTable();
    }

    private void refreshCourseSelector() {
        courseSelector.removeAllItems();
        for (Course course : system.getAllCourses()) {
            courseSelector.addItem(course.getCourseId() + " - " + course.getTitle());
        }
    }

    private void refreshEnrollmentTable() {
        enrollmentTableModel.setRowCount(0);
        courseDetailsArea.setText("");

        String selected = (String) courseSelector.getSelectedItem();
        if (selected == null) {
            courseDetailsArea.setText("No courses available.");
            return;
        }

        String courseId = selected.split(" - ")[0];
        Course course = system.getAllCourses().stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (course == null) {
            courseDetailsArea.setText("Course not found.");
            return;
        }

        displayCourseDetails(course);

        for (Student student : system.getAllStudents()) {
            if (student.isAlreadyEnrolled(courseId)) {
                enrollmentTableModel.addRow(new Object[]{
                        student.getStudentId(),
                        student.getName(),
                        student.getMaxConcurrentCourses(),
                        student.getCurrentCourses().size()
                });
            }
        }
    }

    private void displayCourseDetails(Course course) {
        StringBuilder sb = new StringBuilder();
        sb.append("Course ID: ").append(course.getCourseId()).append("\n");
        sb.append("Title: ").append(course.getTitle()).append("\n");
        sb.append("Capacity: ").append(course.getCapacity()).append("\n");
        sb.append("Enrolled: ").append(course.getEnrolledCount()).append("\n");
        sb.append("Available Seats: ").append(course.getCapacity() - course.getEnrolledCount()).append("\n");
        sb.append("Prerequisites: ").append(formatPrerequisites(course.getPrerequisites())).append("\n");
        if (course.requiresVideoWatch()) {
            sb.append("Weekly Videos:\n");
            for (int week = 1; week <= 8; week++) {
                sb.append("  Week ").append(week).append(": ").append(course.getVideoUrlForWeek(week)).append("\n");
            }
        } else {
            sb.append("Weekly Videos: None\n");
        }
        courseDetailsArea.setText(sb.toString());
    }

    private void doLogout() {
        dispose();
        if (onLogout != null) {
            onLogout.run();
        }
    }

    private String formatPrerequisites(java.util.Set<String> prerequisites) {
        return prerequisites.isEmpty() ? "None" : String.join(", ", prerequisites);
    }
}
