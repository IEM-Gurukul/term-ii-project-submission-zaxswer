package com.course.registration.ui;

import com.course.registration.model.Course;
import com.course.registration.model.Student;
import com.course.registration.model.User;
import com.course.quiz.QuizQuestion;
import com.course.quiz.WeeklyQuizService;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentPortalFrame extends JFrame {
    private static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    private static final String TOPIC_PICKER_VIEW = "TOPIC_PICKER_VIEW";
    private static final String TOPIC_DETAIL_VIEW = "TOPIC_DETAIL_VIEW";

    private final CourseRegistrationSystem system;
    private final String studentId;
    private final Runnable onLogout;
    private final WeeklyQuizService weeklyQuizService;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final DefaultListModel<Course> topicGalleryModel;
    private final DefaultListModel<String> weekPlanModel;
    private final JList<Course> topicGalleryList;
    private final JList<String> weekPlanList;

    private final JLabel selectedTopicTitle;
    private final JTextArea dashboardProgressArea;
    private final JavaFxWebVideoPanel videoEmbedPanel;
    private final JTextArea statusArea;
    private final JButton continueCourseButton;

    private Course selectedTopic;
    private final Map<String, String> selectedTopicWeekVideos;

    public StudentPortalFrame(CourseRegistrationSystem system, User currentUser, Runnable onLogout) {
        this.system = system;
        this.studentId = currentUser.getUsername();
        this.onLogout = onLogout;
        this.weeklyQuizService = new WeeklyQuizService();

        setTitle("Course Registration - Student Portal | " + studentId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);

        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        this.topicGalleryModel = new DefaultListModel<>();
        this.weekPlanModel = new DefaultListModel<>();
        this.selectedTopicWeekVideos = new LinkedHashMap<>();

        this.topicGalleryList = new JList<>(topicGalleryModel);
        this.topicGalleryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.topicGalleryList.setCellRenderer(new CourseCardRenderer());

        this.weekPlanList = new JList<>(weekPlanModel);
        this.weekPlanList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.selectedTopicTitle = new JLabel("No topic selected");
        this.selectedTopicTitle.setFont(selectedTopicTitle.getFont().deriveFont(Font.BOLD, 18f));

        this.dashboardProgressArea = new JTextArea(12, 80);
        this.dashboardProgressArea.setEditable(false);
        this.dashboardProgressArea.setLineWrap(true);
        this.dashboardProgressArea.setWrapStyleWord(true);

        this.videoEmbedPanel = new JavaFxWebVideoPanel();

        this.statusArea = new JTextArea(6, 80);
        this.statusArea.setEditable(false);
        this.statusArea.setLineWrap(true);
        this.statusArea.setWrapStyleWord(true);

        this.continueCourseButton = new JButton("Continue Course");

        cardPanel.add(buildDashboardPanel(), DASHBOARD_VIEW);
        cardPanel.add(buildTopicPickerPanel(), TOPIC_PICKER_VIEW);
        cardPanel.add(buildTopicDetailPanel(), TOPIC_DETAIL_VIEW);

        setContentPane(cardPanel);
        wireSelectionHandlers();
        refreshCourseData();
        restoreSelectedTopic();
        refreshDashboard();
        showDashboard();
    }

    private void wireSelectionHandlers() {
        topicGalleryList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Course chosen = topicGalleryList.getSelectedValue();
            if (chosen != null) {
                selectedTopicTitle.setText(chosen.getCourseId() + " - " + chosen.getTitle());
            }
        });

        topicGalleryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedTopicFromGallery();
                }
            }
        });
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Student Progress Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Track progress first, then continue your selected course.");

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(title, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.CENTER);

        JScrollPane progressPane = new JScrollPane(dashboardProgressArea);
        progressPane.setBorder(BorderFactory.createTitledBorder("Progress"));

        JButton chooseTopicButton = new JButton("Choose Topic");
        JButton refreshDashboardButton = new JButton("Refresh Progress");
        JButton logoutButton = new JButton("Logout");

        continueCourseButton.addActionListener(e -> openContinueCourse());
        chooseTopicButton.addActionListener(e -> showTopicPicker());
        refreshDashboardButton.addActionListener(e -> refreshDashboard());
        logoutButton.addActionListener(e -> doLogout());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(continueCourseButton);
        actions.add(chooseTopicButton);
        actions.add(refreshDashboardButton);
        actions.add(logoutButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(progressPane, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTopicPickerPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Choose A Topic");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JTextArea subtitle = new JTextArea("Pick one topic from the gallery. Selection is saved and shown on your dashboard.");
        subtitle.setEditable(false);
        subtitle.setLineWrap(true);
        subtitle.setWrapStyleWord(true);
        subtitle.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(title, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.CENTER);

        JScrollPane galleryPane = new JScrollPane(topicGalleryList);
        galleryPane.setBorder(BorderFactory.createTitledBorder("Topic Gallery"));

        JButton openTopicButton = new JButton("Save Selected Topic");
        JButton backButton = new JButton("Go Back");
        JButton refreshButton = new JButton("Refresh Topics");
        JButton logoutButton = new JButton("Logout");

        openTopicButton.addActionListener(e -> openSelectedTopicFromGallery());
        backButton.addActionListener(e -> showDashboard());
        refreshButton.addActionListener(e -> refreshCourseData());
        logoutButton.addActionListener(e -> doLogout());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(openTopicButton);
        actions.add(backButton);
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

        JButton backButton = new JButton("Go Back");
        backButton.addActionListener(e -> showDashboard());

        JPanel detailHeader = new JPanel(new BorderLayout(6, 6));
        detailHeader.add(backButton, BorderLayout.WEST);
        detailHeader.add(new JLabel("Selected Topic"), BorderLayout.NORTH);
        detailHeader.add(selectedTopicTitle, BorderLayout.CENTER);

        JScrollPane weekListPane = new JScrollPane(weekPlanList);
        weekListPane.setBorder(BorderFactory.createTitledBorder("Week 1 - Week 8"));

        JScrollPane embedPane = new JScrollPane(videoEmbedPanel);
        embedPane.setBorder(BorderFactory.createTitledBorder("Video Link"));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, weekListPane, embedPane);
        centerSplit.setResizeWeight(0.25);
        centerSplit.setDividerLocation(240);

        JButton playVideoButton = new JButton("Go To YouTube Video");
        JButton markWeekCompletedButton = new JButton("Mark Week As Completed");
        JButton takeQuizButton = new JButton("Take Quiz (MCQ)");

        playVideoButton.addActionListener(e -> playSelectedWeekVideo());
        markWeekCompletedButton.addActionListener(e -> markSelectedWeekCompleted());
        takeQuizButton.addActionListener(e -> takeSelectedWeekQuiz());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionPanel.add(playVideoButton);
        actionPanel.add(markWeekCompletedButton);
        actionPanel.add(takeQuizButton);

        JScrollPane statusPane = new JScrollPane(statusArea);
        statusPane.setBorder(BorderFactory.createTitledBorder("Status"));

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(actionPanel, BorderLayout.NORTH);
        south.add(statusPane, BorderLayout.CENTER);

        panel.add(detailHeader, BorderLayout.NORTH);
        panel.add(centerSplit, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCourseData() {
        topicGalleryModel.clear();

        Student student = system.getStudent(studentId);
        if (student == null) {
            appendStatus("Error: Student not found.");
            return;
        }

        List<Course> allCourses = system.getAllCourses();
        for (Course course : allCourses) {
            topicGalleryModel.addElement(course);
        }
    }

    private void restoreSelectedTopic() {
        String savedTopicId = system.getSelectedTopic(studentId);
        if (savedTopicId == null || savedTopicId.isBlank()) {
            selectedTopic = null;
            selectedTopicTitle.setText("No topic selected");
            return;
        }

        Course savedTopic = system.getCourseById(savedTopicId);
        if (savedTopic == null) {
            selectedTopic = null;
            selectedTopicTitle.setText("No topic selected");
            appendStatus("Previously selected topic is no longer available.");
            return;
        }

        selectedTopic = savedTopic;
        selectedTopicTitle.setText(savedTopic.getCourseId() + " - " + savedTopic.getTitle());
        topicGalleryList.setSelectedValue(savedTopic, true);
    }

    private void refreshDashboard() {
        if (selectedTopic == null) {
            dashboardProgressArea.setText(
                    "Student: " + studentId + "\n"
                            + "Selected Topic: None\n"
                            + "Current Week: -\n"
                            + "Enrollment: -\n"
                            + "Video Completion: -\n\n"
                            + "Next Step:\n"
                            + "1. Click 'Choose Topic'\n"
                            + "2. Save a topic\n"
                            + "3. Click 'Continue Course'\n");
            continueCourseButton.setEnabled(false);
            return;
        }

        Student student = system.getStudent(studentId);
        boolean enrolled = student != null && student.isAlreadyEnrolled(selectedTopic.getCourseId());
        boolean watched = system.hasWatchedCourseVideo(studentId, selectedTopic.getCourseId());
        int week = system.getStudentCurrentWeek(studentId, selectedTopic.getCourseId());
        String weekProgress = week <= 0 ? "Not completed yet" : "Week " + week + " of 8";

        dashboardProgressArea.setText(
                "Student: " + studentId + "\n"
                        + "Selected Topic: " + selectedTopic.getCourseId() + " - " + selectedTopic.getTitle() + "\n"
                    + "Current Week: " + weekProgress + "\n"
                        + "Enrollment: " + (enrolled ? "Enrolled" : "Not Enrolled") + "\n"
                        + "Video Completion Requirement: " + (watched ? "Completed" : "Pending") + "\n\n"
                        + "Next Step:\n"
                    + "Open a week's YouTube link, then click 'Mark Week As Completed'.\n");

        continueCourseButton.setEnabled(true);
    }

    private void openContinueCourse() {
        if (selectedTopic == null) {
            appendStatus("No topic selected yet. Choose a topic first.");
            showTopicPicker();
            return;
        }
        refreshTopicDetail();
        cardLayout.show(cardPanel, TOPIC_DETAIL_VIEW);
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

        selectedTopic = chosen;
        selectedTopicTitle.setText(chosen.getCourseId() + " - " + chosen.getTitle());
        topicGalleryList.setSelectedValue(chosen, true);
        refreshDashboard();
        showDashboard();
    }

    private void populateWeekPlanForSelectedTopic() {
        weekPlanModel.clear();
        selectedTopicWeekVideos.clear();

        if (selectedTopic == null) {
            showVideoPlaceholder("No topic selected.");
            return;
        }

        for (int week = 1; week <= 8; week++) {
            String weekLabel = "Week " + week;
            String weekUrl = system.getCourseVideoUrl(selectedTopic.getCourseId(), week);
            if (weekUrl == null || weekUrl.isBlank()) {
                weekUrl = system.getCourseVideoUrl(selectedTopic.getCourseId());
            }
            weekPlanModel.addElement(weekLabel);
            selectedTopicWeekVideos.put(weekLabel, weekUrl);
        }

        boolean hasAnyVideo = selectedTopicWeekVideos.values().stream().anyMatch(url -> url != null && !url.isBlank());
        if (!hasAnyVideo) {
            showVideoPlaceholder("No video configured for this topic.");
            return;
        }

        int currentWeek = system.getStudentCurrentWeek(studentId, selectedTopic.getCourseId());
        int initialIndex = currentWeek <= 0 ? 0 : Math.max(0, Math.min(7, currentWeek - 1));
        weekPlanList.setSelectedIndex(initialIndex);
        showVideoPlaceholder("Week selected. Click 'Go To YouTube Video' to open the normal YouTube link.");
    }

    private void showVideoPlaceholder(String message) {
        videoEmbedPanel.showPlaceholder(message);
    }

    private void refreshTopicDetail() {
        refreshCourseData();

        if (selectedTopic == null) {
            showVideoPlaceholder("No topic selected.");
            return;
        }

        Course latest = system.getCourseById(selectedTopic.getCourseId());
        if (latest != null) {
            selectedTopic = latest;
            selectedTopicTitle.setText(latest.getCourseId() + " - " + latest.getTitle());
        }

        populateWeekPlanForSelectedTopic();
    }

    private void playSelectedWeekVideo() {
        if (selectedTopic == null) {
            appendStatus("Select a topic first.");
            return;
        }

        String selectedWeek = weekPlanList.getSelectedValue();
        if (selectedWeek == null) {
            appendStatus("Select a week first (Week 1 to Week 8).");
            return;
        }

        String videoUrl = selectedTopicWeekVideos.get(selectedWeek);
        if (videoUrl == null || videoUrl.isBlank()) {
            appendStatus("No video configured for " + selectedWeek + ".");
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                appendStatus("Desktop browsing is not supported on this system.");
                return;
            }
            Desktop.getDesktop().browse(URI.create(videoUrl));
        } catch (Exception ex) {
            appendStatus("Unable to open video link in browser.");
            return;
        }

        appendStatus("Opened YouTube link for " + selectedWeek + " in browser.");
    }

    private void markSelectedWeekCompleted() {
        if (selectedTopic == null) {
            appendStatus("Select a topic first.");
            return;
        }

        String selectedWeek = weekPlanList.getSelectedValue();
        if (selectedWeek == null) {
            appendStatus("Select a week first (Week 1 to Week 8).");
            return;
        }

        int weekNumber = parseWeekNumber(selectedWeek);
        RegistrationResult watchResult = system.markCourseVideoWatched(studentId, selectedTopic.getCourseId());
        RegistrationResult progressResult = system.updateStudentCurrentWeek(studentId, selectedTopic.getCourseId(), weekNumber);

        if (watchResult.isSuccess() && progressResult.isSuccess()) {
            appendStatus("Week " + weekNumber + " is completed.");
        } else {
            appendStatus(watchResult.getMessage());
            appendStatus(progressResult.getMessage());
        }

        refreshDashboard();
    }

    private void takeSelectedWeekQuiz() {
        if (selectedTopic == null) {
            appendStatus("Select a topic first.");
            return;
        }

        String selectedWeek = weekPlanList.getSelectedValue();
        if (selectedWeek == null) {
            appendStatus("Select a week first (Week 1 to Week 8).");
            return;
        }

        int weekNumber = parseWeekNumber(selectedWeek);
        int completedWeek = system.getStudentCurrentWeek(studentId, selectedTopic.getCourseId());
        if (completedWeek < weekNumber) {
            appendStatus("Complete " + selectedWeek + " before taking its quiz.");
            return;
        }

        List<QuizQuestion> questions = weeklyQuizService.getQuestionsForWeek(selectedTopic.getCourseId(), weekNumber);
        if (questions.isEmpty()) {
            appendStatus("No quiz available for " + selectedWeek + ".");
            return;
        }

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            int selectedOption = JOptionPane.showOptionDialog(
                    this,
                    question.getPrompt(),
                    "Quiz " + (i + 1) + "/" + questions.size() + " - " + selectedTopic.getCourseId() + " " + selectedWeek,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    question.getOptions().toArray(),
                    question.getOptions().get(0)
            );

            if (selectedOption < 0) {
                appendStatus("Quiz cancelled.");
                return;
            }

            if (selectedOption == question.getCorrectOptionIndex()) {
                score++;
            }
        }

        appendStatus("Quiz result for " + selectedWeek + ": " + score + "/" + questions.size() + " correct.");
    }

    private int parseWeekNumber(String weekLabel) {
        if (weekLabel == null) {
            return 1;
        }
        String cleaned = weekLabel.replace("Week", "").trim();
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private void showDashboard() {
        refreshDashboard();
        cardLayout.show(cardPanel, DASHBOARD_VIEW);
    }

    private void showTopicPicker() {
        cardLayout.show(cardPanel, TOPIC_PICKER_VIEW);
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
                        + course.getTitle() + "</b><br/>Seats: " + course.getEnrolledCount() + "/"
                        + course.getCapacity() + "</div></html>");
                label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            }
            return label;
        }
    }
}
