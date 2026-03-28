package com.course.registration.service;

import com.course.registration.model.User;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationManager {
    private final Map<String, User> users;

    public AuthenticationManager() {
        this.users = new HashMap<>();
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Add default teachers with password "admin123"
        users.put("teacher1", new User("teacher1", "admin123", User.Role.TEACHER));
        users.put("teacher2", new User("teacher2", "admin123", User.Role.TEACHER));
        users.put("teacher3", new User("teacher3", "admin123", User.Role.TEACHER));
        users.put("teacher4", new User("teacher4", "admin123", User.Role.TEACHER));

        // Add sample students (username and password are the same)
        users.put("S001", new User("S001", "S001", User.Role.STUDENT));
        users.put("S002", new User("S002", "S002", User.Role.STUDENT));
        users.put("S003", new User("S003", "S003", User.Role.STUDENT));
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
}
