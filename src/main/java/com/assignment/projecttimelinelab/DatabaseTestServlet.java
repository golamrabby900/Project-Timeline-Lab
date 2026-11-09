package com.assignment.projecttimelinelab;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/db-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (Connection connection = DatabaseConnection.getConnection()) {

            response.getWriter().println(
                    "<h1>Database Connection Successful!</h1>"
            );

            response.getWriter().println(
                    "<p>Java is successfully connected to MySQL.</p>"
            );

            response.getWriter().println(
                    "<p>Database: " +
                            connection.getCatalog() +
                            "</p>"
            );

        } catch (SQLException e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().println(
                    "<h1>Database Connection Failed!</h1>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

            e.printStackTrace();
        }
    }
}