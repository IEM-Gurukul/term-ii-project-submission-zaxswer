package com.course.registration.ui;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.model.User;
import com.course.registration.service.CourseRegistrationSystem;
import com.course.registration.service.RegistrationResult;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URI;
import java.util.List;
import java.util.Set;

public class StudentPortalFrame extends JFrame {
    private static final String TOPIC_PICKER_VIEW = "TOPIC_PICKER_VIEW";
    private static final String TOPIC_DETAIL_VIEW = "TOPIC_DETAIL_VIEW";

    private final CourseRegistrationSystem system;
    private final String studentId;
    private final Runnable onLogout;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final DefaultListModel<Course> topicGalleryModel;
    private final DefaultListModel<Course> enrolledCoursesModel;
    private final JList<Course> topicGalleryList;
    private final JList<Course> enrolledCoursesList;

    private final JLabel selectedTopicTitle;
    private final JTextArea selectedTopicDetails;
    private final JTextArea statusArea;

    private Course selectedTopic;

    public StudentPortalFrame(CourseRegistrationSystem system, User currentUser, Runnable onLogout) {
        this.system = system;
        this.studentId = currentUser.getUsername();
        this.onLogout = onLogout;

        setTitle("Course Registration - Student Portal | " + studentId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(null);

        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        this.topicGalleryModel = new DefaultListModel<>();
        this.enrolledCoursesModel = new DefaultListModel<>();

        this.topicGalleryList = new JList<>(topicGalleryModel);
        this.topicGalleryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.topicGalleryList.setCellRenderer(new CourseCardRenderer());

        this.enrolledCoursesList = new JList<>(enrolledCoursesModel);
        this.enrolledCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.enrolledCoursesList.setCellRenderer(new CourseCardRenderer());

        this.selectedTopicTitle = new JLabel("No topic selected");
        this.selectedTopicTitle.setFont(selectedTopicTitle.getFont().deriveFont(Font.BOLD, 18f));

        this.selectedTopicDetails = new JTextArea(8, 40);
        this.selectedTopicDetails.setEditable(false);
        this.selectedTopicDetails.setLineWrap(true);
        this.selectedTopicDetails.setWrapStyleWord(true);

        this.statusArea = new JTextArea(6, 80);
        this.statusArea.setEditable(false);
        this.statusArea.setLineWrap(true);
        this.statusArea.setWrapStyleWord(true);

        cardPanel.add(buildTopicPickerPanel(), TOPIC_PICKER_VIEW);
        cardPanel.add(buildTopicDetailPanel(), TOPIC_DETAIL_VIEW);

        setContentPane(cardPanel);
        refreshCourseData();
        restoreSelectedTopic();
    }

    private JPanel buildTopicPickerPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Choose A Topic");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JTextArea subtitle = new JTextArea("Pick one topic from the gallery. Your selection is remembered for your next login.");
        subtitle.setEditable(false);
        subtitle.setLineWrap(true);
        subtitle.setWrapStyleWord(true);
        subtitle.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(title, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.CENTER);

        JScrollPane galleryPane = new JScrollPane(topicGalleryList);
        galleryPane.setBorder(BorderFactory.createTitledBorder("Topic Gallery"));

        JButton openTopicButton = new JButton("Open Selected Topic");
        JButton refreshButton = new JButton("Refresh Topics");
        JButton logoutButton = new JButton("Logout");

        openTopicButton.addActionListener(e -> openSelectedTopicFromGallery());
        refreshButton.addActionListener(e -> refreshCourseData());
        logoutButton.addActionListener(e -> doLogout());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(openTopicButton);
        actions.add(refreshButton);
        actions.add(logoutButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(galleryPane, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTopicDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel detailHeader = new JPanel(new BorderLayout(6, 6));
        detailHeader.add(new JLabel("Selected Topic"), BorderLayout.NORTH);
        detailHeader.add(selectedTopicTitle, BorderLayout.CENTER);

        JScrollPane detailsPane = new JScrollPane(selectedTopicDetails);
        detailsPane.setBorder(BorderFactory.createTitledBorder("Topic Details"));

        JScrollPane enrolledPane = new JScrollPane(enrolledCoursesList);
        enrolledPane.setBorder(BorderFactory.createTitledBorder("My Enrolled Courses"));

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.add(detailsPane);
        center.add(enrolledPane);

        JButton watchVideoButton = new JButton("Watch Topic Video");
        JButton enrollButton = new JButton("Enroll In Topic");
        JButton dropButton = new JButton("Drop Selected Enrolled Topic");
        JButton chooseAnotherButton = new JButton("Choose Another Topic");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");

        watchVideoButton.addActionListener(e -> watchSelectedTopicVideo());
        enrollButton.addActionListener(e -> enrollInSelectedTopic());
        dropButton.addActionListener(e -> dropSelectedEnrolledTopic());
        chooseAnotherButton.addActionListener(e -> showTopicPicker());
        refreshButton.addActionListener(e -> refreshTopicDetail());
        logoutButton.addActionListener(e -> doLogout());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionPanel.add(watchVideoButton);
        actionPanel.add(enrollButton);
        actionPanel.add(dropButton);
        actionPanel.add(chooseAnotherButton);
        actionPanel.add(refreshButton);
        actionPanel.add(logoutButton);

        JScrollPane statusPane = new JScrollPane(statusArea);
        statusPane.setBorder(BorderFactory.createTitledBorder("Status"));

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(actionPanel, BorderLayout.NORTH);
        south.add(statusPane, BorderLayout.CENTER);

        panel.add(detailHeader, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCourseData() {
        topicGalleryModel.clear();
        enrolledCoursesModel.clear();

        Student student = system.getStudent(studentId);
        if (student == null) {
            appendStatus("Error: Student not found.");
            return;
        }

        List<Course> allCourses = system.getAllCourses();
        for (Course course : allCourses) {
            topicGalleryModel.addElement(course);
            if (student.isAlreadyEnrolled(course.getCourseId())) {
                enrolledCoursesModel.addElement(course);
            }
        }
    }

    private void restoreSelectedTopic() {
        String savedTopicId = system.getSelectedTopic(studentId);
        if (savedTopicId == null || savedTopicId.isBlank()) {
            showTopicPicker();
            appendStatus("Select a topic from the gallery.");
            return;
        }

        Course savedTopic = system.getCourseById(savedTopicId);
        if (savedTopic == null) {
            showTopicPicker();
            appendStatus("Previously selected topic is no longer available.");
            return;
        }

        setSelectedTopic(savedTopic);
        cardLayout.show(cardPanel, TOPIC_DETAIL_VIEW);
        appendStatus("Restored your selected topic: " + savedTopic.getCourseId());
    }

    private void openSelectedTopicFromGallery() {
        Course chosen = topicGalleryList.getSelectedValue();
        if (chosen == null) {
            JOptionPane.showMessageDialog(this, "Please select a topic first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        RegistrationResult result = system.selectTopic(studentId, chosen.getCourseId());
        appendStatus(result.getMessage());
        if (!result.isSuccess()) {
            return;
        }

        setSelectedTopic(chosen);
        cardLayout.show(cardPanel, TOPIC_DETAIL_VIEW);
    }

    private void setSelectedTopic(Course course) {
        selectedTopic = course;
        selectedTopicTitle.setText(course.getCourseId() + " - " + course.getTitle());
        refreshTopicDetail();
    }

    private void refreshTopicDetail() {
        refreshCourseData();

        if (selectedTopic == null) {
            selectedTopicDetails.setText("No topic selected.");
            return;
        }

        Course latest = system.getCourseById(selectedTopic.getCourseId());
        if (latest != null) {
            selectedTopic = latest;
        }

        boolean watched = system.hasWatchedCourseVideo(studentId, selectedTopic.getCourseId());
        Student student = system.getStudent(studentId);
        boolean enrolled = student != null && student.isAlreadyEnrolled(selectedTopic.getCourseId());

        StringBuilder details = new StringBuilder();
        details.append("Course ID: ").append(selectedTopic.getCourseId()).append("\n");
        details.append("Title: ").append(selectedTopic.getTitle()).append("\n");
        details.append("Seats: ").append(selectedTopic.getEnrolledCount()).append("/").append(selectedTopic.getCapacity())
                .append("\n");
        details.append("Prerequisites: ").append(formatPrerequisites(selectedTopic.getPrerequisites())).append("\n");
        details.append("Video Required: ").append(selectedTopic.requiresVideoWatch() ? "Yes" : "No").append("\n");
        details.append("Video Watched: ").append(watched ? "Yes" : "No").append("\n");
        details.append("Enrolled: ").append(enrolled ? "Yes" : "No").append("\n");
        if (selectedTopic.requiresVideoWatch() && selectedTopic.getRequiredVideoUrl() != null) {
            details.append("Video URL: ").append(selectedTopic.getRequiredVideoUrl()).append("\n");
        }

        selectedTopicDetails.setText(details.toString());
    }

    private void watchSelectedTopicVideo() {
        if (selectedTopic == null) {
            appendStatus("Select a topic first.");
            return;
        }

        String videoUrl = system.getCourseVideoUrl(selectedTopic.getCourseId());
        if (videoUrl == null || videoUrl.isBlank()) {
            appendStatus("No required video configured for " + selectedTopic.getCourseId() + ".");
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                appendStatus("Desktop browsing is not supported on this system.");
                return;
            }

            Desktop.getDesktop().browse(URI.create(videoUrl));
            RegistrationResult watchedResult = system.markCourseVideoWatched(studentId, selectedTopic.getCourseId());
            appendStatus("Opened video: " + videoUrl);
            appendStatus(watchedResult.getMessage());
            refreshTopicDetail();
        } catch (Exception ex) {
            appendStatus("Could not open video URL: " + ex.getMessage());
        }
    }

    private void enrollInSelectedTopic() {
        if (selectedTopic == null) {
            appendStatus("Select a topic first.");
            return;
        }

        RegistrationResult result = system.registerStudentForCourse(studentId, selectedTopic.getCourseId());
        appendStatus(result.getMessage());
        refreshTopicDetail();
    }

    private void dropSelectedEnrolledTopic() {
        Course enrolled = enrolledCoursesList.getSelectedValue();
        if (enrolled == null) {
            JOptionPane.showMessageDialog(this, "Select an enrolled topic to drop.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Drop " + enrolled.getCourseId() + " - " + enrolled.getTitle() + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        RegistrationResult result = system.dropStudentFromCourse(studentId, enrolled.getCourseId());
        appendStatus(result.getMessage());
        refreshTopicDetail();
    }

    private void showTopicPicker() {
        cardLayout.show(cardPanel, TOPIC_PICKER_VIEW);
    }

    private String formatPrerequisites(Set<String> prerequisites) {
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

    private static class CourseCardRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Course) {
                Course course = (Course) value;
                label.setText("<html><div style='padding:6px;'><b>" + course.getCourseId() + " - "
                        + course.getTitle()
                        + "</b><br/>Seats: " + course.getEnrolledCount() + "/" + course.getCapacity() + "</div></html>");
                label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            }
            return label;
        }
    }
}
