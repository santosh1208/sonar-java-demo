// src/main/java/com/example/demo/VulnerableApp.java
package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VulnerableApp {

    // SonarLint will flag this as a Hardcoded Credential (java:S2068)
    private static final String DATABASE_PASSWORD = "MySecretPassword123!"; // VULNERABILITY 1

    public static void main(String[] args) {
        System.out.println("Starting VulnerableApp demo...");
        // This input is intentionally malicious for the demo to show SQL Injection
        String userId = "admin'; DROP TABLE users;--";
        fetchUser(userId);
        System.out.println("VulnerableApp demo finished.");
    }

    public static void fetchUser(String userId) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Establish a dummy connection to an in-memory H2 database
            // SonarLint might flag the hardcoded password here too
            conn = DriverManager.getConnection("jdbc:h2:mem:testdb;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC\\; CREATE TABLE IF NOT EXISTS PUBLIC.users(id VARCHAR(255), name VARCHAR(255));", "sa", DATABASE_PASSWORD); // Using the hardcoded password

            // SonarLint will flag this as a SQL Injection (java:S5147)
            String sqlQuery = "SELECT * FROM users WHERE id = '" + userId + "'"; // VULNERABILITY 2
            System.out.println("Executing query: " + sqlQuery); // For demo clarity

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                System.out.println("User found: ID=" + rs.getString("id") + ", Name=" + rs.getString("name"));
            } else {
                System.out.println("No user found with ID: " + userId);
            }

        } catch (SQLException e) {
            System.err.println("Database error during fetchUser: " + e.getMessage());
            // In a real app, log the full stack trace and handle gracefully.
        } finally {
            // Close resources to prevent leaks (good practice)
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}