package com.assignment.projecttimelinelab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            System.getProperty(
                    "db.url",
                    "jdbc:mysql://localhost:3306/project_timeline_db"
            );

    private static final String USER =
            System.getProperty("db.user", "root");

    private static final String PASSWORD =
            System.getProperty("db.password", "");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {

        try {

            // Explicitly load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish database connection
            return DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

        } catch (ClassNotFoundException e) {

            throw new SQLException(
                    "MySQL JDBC Driver not found. Check WEB-INF/lib.",
                    e
            );
        }
    }
}