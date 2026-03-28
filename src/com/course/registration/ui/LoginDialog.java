package com.course.registration.ui;

import com.course.registration.model.User;
import com.course.registration.service.AuthenticationManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginDialog extends JDialog {
    private final AuthenticationManager authManager;
    private User authenticatedUser;

    private final JComboBox<String> roleSelector;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginDialog(AuthenticationManager authManager) {
        super((JFrame) null, "Course Registration System - Login", true);
        this.authManager = authManager;
        this.authenticatedUser = null;

        setSize(380, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Login to Course Registration System");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel roleLabel = new JLabel("Login as:");
        roleSelector = new JComboBox<>(new String[]{"Student", "Teacher"});

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        formPanel.add(roleLabel);
        formPanel.add(roleSelector);
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> doLogin());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = authManager.authenticate(username, password);

        String selectedRole = (String) roleSelector.getSelectedItem();
        boolean isTeacherMode = "Teacher".equals(selectedRole);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isTeacherMode && user.getRole() != User.Role.TEACHER) {
            JOptionPane.showMessageDialog(this, "This account is not a teacher account.", "Access Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isTeacherMode && user.getRole() != User.Role.STUDENT) {
            JOptionPane.showMessageDialog(this, "This account is not a student account.", "Access Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        authenticatedUser = user;
        dispose();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static User showLoginDialog(AuthenticationManager authManager) {
        LoginDialog dialog = new LoginDialog(authManager);
        dialog.setVisible(true);
        return dialog.getAuthenticatedUser();
    }
}
