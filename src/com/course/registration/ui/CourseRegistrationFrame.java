package com.course.registration.ui;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.service.CourseRegistrationSystem;
import com.course.registration.service.RegistrationResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
import java.util.Set;

public class CourseRegistrationFrame extends JFrame {
    private final CourseRegistrationSystem system;

    private final JComboBox<String> studentSelector;
    private final JComboBox<String> courseSelector;

    private final DefaultTableModel coursesTableModel;
    private final DefaultTableModel studentsTableModel;

    private final JTextArea studentDetailsArea;
    private final JTextArea statusArea;

    public CourseRegistrationFrame(CourseRegistrationSystem system) {
        this.system = system;

        setTitle("Course Registration System - Swing GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        topPanel.add(buildSelectionPanel());
        topPanel.add(buildActionPanel());

        coursesTableModel = new DefaultTableModel(new Object[] {
                "Course ID", "Title", "Enrolled", "Capacity", "Prerequisites"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentsTableModel = new DefaultTableModel(new Object[] {
                "Student ID", "Name", "Current", "Max Load", "Completed"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable coursesTable = new JTable(coursesTableModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTable studentsTable = new JTable(studentsTableModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane coursesPane = new JScrollPane(coursesTable);
        coursesPane.setBorder(BorderFactory.createTitledBorder("Courses"));

        JScrollPane studentsPane = new JScrollPane(studentsTable);
        studentsPane.setBorder(BorderFactory.createTitledBorder("Students"));

        JSplitPane tablesSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, coursesPane, studentsPane);
        tablesSplit.setResizeWeight(0.58);

        studentDetailsArea = new JTextArea(7, 30);
        studentDetailsArea.setEditable(false);
        studentDetailsArea.setLineWrap(true);
        studentDetailsArea.setWrapStyleWord(true);
        studentDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        statusArea = new JTextArea(7, 30);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomPanel.add(wrapArea(studentDetailsArea, "Selected Student Schedule"));
        bottomPanel.add(wrapArea(statusArea, "Status"));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablesSplit, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        studentSelector = (JComboBox<String>) ((JPanel) topPanel.getComponent(0)).getComponent(1);
        courseSelector = (JComboBox<String>) ((JPanel) topPanel.getComponent(0)).getComponent(3);

        refreshAll();
        statusArea.setText("GUI ready. Select student/course and perform actions.\n");
    }

    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JLabel studentLabel = new JLabel("Student:", SwingConstants.LEFT);
        JComboBox<String> studentBox = new JComboBox<>();
        studentBox.setPreferredSize(new Dimension(180, 28));

        JLabel courseLabel = new JLabel("Course:", SwingConstants.LEFT);
        JComboBox<String> courseBox = new JComboBox<>();
        courseBox.setPreferredSize(new Dimension(180, 28));

        panel.add(studentLabel);
        panel.add(studentBox);
        panel.add(courseLabel);
        panel.add(courseBox);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JButton registerButton = new JButton("Register");
        JButton dropButton = new JButton("Drop");
        JButton completeButton = new JButton("Mark Completed");
        JButton scheduleButton = new JButton("Show Schedule");
        JButton refreshButton = new JButton("Refresh");

        registerButton.addActionListener(e -> doRegister());
        dropButton.addActionListener(e -> doDrop());
        completeButton.addActionListener(e -> doComplete());
        scheduleButton.addActionListener(e -> showSelectedStudentSchedule());
        refreshButton.addActionListener(e -> {
            refreshAll();
            appendStatus("Data refreshed.");
        });

        panel.add(registerButton);
        panel.add(dropButton);
        panel.add(completeButton);
        panel.add(scheduleButton);
        panel.add(refreshButton);

        return panel;
    }

    private JScrollPane wrapArea(JTextArea area, String title) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createTitledBorder(title));
        return pane;
    }

    private void doRegister() {
        String studentId = selectedId(studentSelector, "student");
        String courseId = selectedId(courseSelector, "course");
        if (studentId == null || courseId == null) {
            return;
        }

        RegistrationResult result = system.registerStudentForCourse(studentId, courseId);
        appendStatus(result.getMessage());
        refreshAll();
    }

    private void doDrop() {
        String studentId = selectedId(studentSelector, "student");
        String courseId = selectedId(courseSelector, "course");
        if (studentId == null || courseId == null) {
            return;
        }

        RegistrationResult result = system.dropStudentFromCourse(studentId, courseId);
        appendStatus(result.getMessage());
        refreshAll();
    }

    private void doComplete() {
        String studentId = selectedId(studentSelector, "student");
        String courseId = selectedId(courseSelector, "course");
        if (studentId == null || courseId == null) {
            return;
        }

        RegistrationResult result = system.markCourseCompleted(studentId, courseId);
        appendStatus(result.getMessage());
        refreshAll();
    }

    private String selectedId(JComboBox<String> comboBox, String label) {
        Object selected = comboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a " + label + ".", "Missing Selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String value = selected.toString();
        int separator = value.indexOf(" - ");
        return separator >= 0 ? value.substring(0, separator) : value;
    }

    private void refreshAll() {
        String previousStudent = idFromCombinedValue((String) studentSelector.getSelectedItem());
        String previousCourse = idFromCombinedValue((String) courseSelector.getSelectedItem());

        refreshSelectors(previousStudent, previousCourse);
        refreshTables();
        showSelectedStudentSchedule();
    }

    private void refreshSelectors(String previousStudent, String previousCourse) {
        studentSelector.removeAllItems();
        for (Student student : system.getAllStudents()) {
            studentSelector.addItem(student.getStudentId() + " - " + student.getName());
        }

        courseSelector.removeAllItems();
        for (Course course : system.getAllCourses()) {
            courseSelector.addItem(course.getCourseId() + " - " + course.getTitle());
        }

        restoreSelection(studentSelector, previousStudent);
        restoreSelection(courseSelector, previousCourse);
    }

    private void restoreSelection(JComboBox<String> selector, String preferredId) {
        if (selector.getItemCount() == 0) {
            return;
        }

        if (preferredId != null) {
            for (int i = 0; i < selector.getItemCount(); i++) {
                String item = selector.getItemAt(i);
                if (item.startsWith(preferredId + " - ")) {
                    selector.setSelectedIndex(i);
                    return;
                }
            }
        }

        selector.setSelectedIndex(0);
    }

    private void refreshTables() {
        coursesTableModel.setRowCount(0);
        studentsTableModel.setRowCount(0);

        for (Course course : system.getAllCourses()) {
            coursesTableModel.addRow(new Object[] {
                    course.getCourseId(),
                    course.getTitle(),
                    course.getEnrolledCount(),
                    course.getCapacity(),
                    formatSet(course.getPrerequisites())
            });
        }

        for (Student student : system.getAllStudents()) {
            studentsTableModel.addRow(new Object[] {
                    student.getStudentId(),
                    student.getName(),
                    student.getCurrentCourses().size(),
                    student.getMaxConcurrentCourses(),
                    formatSet(student.getCompletedCourses())
            });
        }
    }

    private void showSelectedStudentSchedule() {
        String studentId = selectedIdSilently(studentSelector);
        if (studentId == null) {
            studentDetailsArea.setText("No student selected.");
            return;
        }

        Student student = system.getStudent(studentId);
        if (student == null) {
            studentDetailsArea.setText("Student not found: " + studentId);
            return;
        }

        List<Course> currentCourses = system.getStudentCurrentCourses(studentId);
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(student.getStudentId()).append(" - ").append(student.getName()).append("\n");
        sb.append("Current load: ").append(currentCourses.size()).append("/")
                .append(student.getMaxConcurrentCourses()).append("\n\n");

        sb.append("Current courses:\n");
        if (currentCourses.isEmpty()) {
            sb.append("- None\n");
        } else {
            for (Course course : currentCourses) {
                sb.append("- ").append(course.getCourseId()).append(" : ").append(course.getTitle()).append("\n");
            }
        }

        sb.append("\nCompleted courses:\n");
        if (student.getCompletedCourses().isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String completed : student.getCompletedCourses()) {
                sb.append("- ").append(completed).append("\n");
            }
        }

        studentDetailsArea.setText(sb.toString());
    }

    private String selectedIdSilently(JComboBox<String> comboBox) {
        Object selected = comboBox.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return idFromCombinedValue(selected.toString());
    }

    private String idFromCombinedValue(String combined) {
        if (combined == null) {
            return null;
        }
        int separator = combined.indexOf(" - ");
        return separator >= 0 ? combined.substring(0, separator) : combined;
    }

    private String formatSet(Set<String> values) {
        return values.isEmpty() ? "None" : values.toString();
    }

    private void appendStatus(String message) {
        statusArea.append(message + "\n");
        SwingUtilities.invokeLater(() -> statusArea.setCaretPosition(statusArea.getDocument().getLength()));
    }
}
