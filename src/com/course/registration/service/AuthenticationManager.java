package com.course.registration.service;

import com.course.registration.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationManager {
    private final Map<String, User> users;
    private static final String USERS_FILE_PATH = "src/data/users.csv";

    public AuthenticationManager() {
        this.users = new HashMap<>();
        loadUsersFromCSV();
    }

    private void loadUsersFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE_PATH))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    String username = values[0].trim();
                    String password = values[1].trim();
                    User.Role role = User.Role.valueOf(values[2].trim().toUpperCase());
                    users.put(username, new User(username, password, role));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // In a real application, you'd want to handle this more gracefully
        }
    }

    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String normalizedUsername = username.trim();
        User user = users.get(normalizedUsername);
        if (user == null) {
            user = users.get(normalizedUsername.toUpperCase());
        }
        if (user == null) {
            user = users.get(normalizedUsername.toLowerCase());
        }

        if (user != null && user.matchesCredentials(user.getUsername(), password.trim())) {
            return user;
        }
        return null;
    }

    public void registerStudent(String username, String password) {
        if (username == null || password == null) {
            return;
        }
        String normalizedUsername = username.trim().toUpperCase();
        if (!users.containsKey(normalizedUsername)) {
            users.put(normalizedUsername, new User(normalizedUsername, password.trim(), User.Role.STUDENT));
        }
    }

    public boolean studentExists(String username) {
        if (username == null) {
            return false;
        }
        String normalizedUsername = username.trim().toUpperCase();
        return users.containsKey(normalizedUsername)
                && users.get(normalizedUsername).getRole() == User.Role.STUDENT;
    }

    public List<String> getStudentUsernames() {
        List<String> studentUsernames = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() == User.Role.STUDENT) {
                studentUsernames.add(user.getUsername());
            }
        }
        studentUsernames.sort(String::compareTo);
        return studentUsernames;
    }
}
