package com.course.registration.ui;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.model.User;
import com.course.registration.service.CourseRegistrationSystem;
import com.course.registration.service.RegistrationResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class StudentPortalFrame extends JFrame {
    private final CourseRegistrationSystem system;
    private final User currentUser;
    private final String studentId;
    private final DefaultTableModel availableCoursesModel;
    private final DefaultTableModel enrolledCoursesModel;
    private final JTable availableCoursesTable;
    private final JTable enrolledCoursesTable;
    private final JTextArea statusArea;
    private final Runnable onLogout;

    public StudentPortalFrame(CourseRegistrationSystem system, User currentUser, Runnable onLogout) {
        this.system = system;
        this.currentUser = currentUser;
        this.studentId = currentUser.getUsername();
        this.onLogout = onLogout;

        setTitle("Course Registration - Student Portal | " + studentId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel headerLabel = new JLabel("Student: " + studentId);
        headerLabel.setFont(headerLabel.getFont().deriveFont(14f));

        availableCoursesModel = new DefaultTableModel(new Object[]{
                "Course ID", "Title", "Credits", "Enrolled", "Capacity", "Prerequisites"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enrolledCoursesModel = new DefaultTableModel(new Object[]{
                "Course ID", "Title", "Credits", "Instructor"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        availableCoursesTable = new JTable(availableCoursesModel);
        availableCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        enrolledCoursesTable = new JTable(enrolledCoursesModel);
        enrolledCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane availableCoursePane = new JScrollPane(availableCoursesTable);
        availableCoursePane.setBorder(BorderFactory.createTitledBorder("Available Courses"));

        JScrollPane enrolledCoursePane = new JScrollPane(enrolledCoursesTable);
        enrolledCoursePane.setBorder(BorderFactory.createTitledBorder("My Enrolled Courses"));

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesPanel.add(availableCoursePane);
        tablesPanel.add(enrolledCoursePane);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton enrollButton = new JButton("Enroll in Selected");
        JButton dropButton = new JButton("Drop Selected");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");

        enrollButton.addActionListener(e -> enrollInSelectedCourse());
        dropButton.addActionListener(e -> dropSelectedCourse());
        refreshButton.addActionListener(e -> refreshCourseLists());
        logoutButton.addActionListener(e -> doLogout());

        actionPanel.add(enrollButton);
        actionPanel.add(dropButton);
        actionPanel.add(refreshButton);
        actionPanel.add(logoutButton);

        statusArea = new JTextArea(6, 50);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        JScrollPane statusPane = new JScrollPane(statusArea);
        statusPane.setBorder(BorderFactory.createTitledBorder("Status Log"));

        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(tablesPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.BEFORE_FIRST_LINE);
        mainPanel.add(statusPane, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        refreshCourseLists();
    }

    private void refreshCourseLists() {
        availableCoursesModel.setRowCount(0);
        enrolledCoursesModel.setRowCount(0);

        List<Course> allCourses = system.getAllCourses();
        Student student = system.getStudent(studentId);

        if (student == null) {
            appendStatus("Error: Student not found.");
            return;
        }

        // Populate enrolled courses
        for (Course course : allCourses) {
            if (student.isAlreadyEnrolled(course.getCourseId())) {
                enrolledCoursesModel.addRow(new Object[]{
                        course.getCourseId(),
                        course.getTitle(),
                        course.getCapacity(),
                        "TBD"
                });
            }
        }

        // Populate available courses (all courses not yet enrolled in)
        for (Course course : allCourses) {
            if (!student.isAlreadyEnrolled(course.getCourseId())) {
                availableCoursesModel.addRow(new Object[]{
                        course.getCourseId(),
                        course.getTitle(),
                        course.getCapacity(),
                        course.getEnrolledCount(),
                        course.getCapacity(),
                        formatPrerequisites(course.getPrerequisites())
                });
            }
        }

        appendStatus("Course lists refreshed.");
    }

    private void enrollInSelectedCourse() {
        int selectedRow = availableCoursesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to enroll.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) availableCoursesModel.getValueAt(selectedRow, 0);
        RegistrationResult result = system.registerStudentForCourse(studentId, courseId);
        appendStatus(result.getMessage());
        refreshCourseLists();
    }

    private void dropSelectedCourse() {
        int selectedRow = enrolledCoursesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to drop.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) enrolledCoursesModel.getValueAt(selectedRow, 0);
        String courseTitle = (String) enrolledCoursesModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + courseTitle + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            RegistrationResult result = system.dropStudentFromCourse(studentId, courseId);
            appendStatus(result.getMessage());
            refreshCourseLists();
        }
    }

    private String formatPrerequisites(java.util.Set<String> prerequisites) {
        return prerequisites.isEmpty() ? "None" : String.join(", ", prerequisites);
    }

    private void appendStatus(String message) {
        statusArea.append(message + "\n");
        SwingUtilities.invokeLater(() -> statusArea.setCaretPosition(statusArea.getDocument().getLength()));
    }

    private void doLogout() {
        dispose();
        if (onLogout != null) {
            onLogout.run();
        }
    }
}
