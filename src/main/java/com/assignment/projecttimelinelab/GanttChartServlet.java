package com.assignment.projecttimelinelab;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/gantt")
public class GanttChartServlet extends HttpServlet {

    private static final int DAY_WIDTH = 42;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("dd");

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMM");

    // Task data structure
    private record Task(
            String name,
            LocalDate start,
            LocalDate end
    ) {

        long duration() {
            return ChronoUnit.DAYS.between(start, end) + 1;
        }
    }

    // Display Gantt Chart
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String projectIdParameter =
                request.getParameter("projectId");

        if (projectIdParameter == null ||
                !projectIdParameter.matches("[0-9]+")) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "A valid project ID is required."
            );

            return;
        }

        int projectId;

        try {
            projectId = Integer.parseInt(projectIdParameter);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid project ID."
            );

            return;
        }

        String projectName;

        List<Task> tasks = new ArrayList<>();

        String projectSQL = """
                SELECT project_name
                FROM projects
                WHERE id = ?
                """;

        String taskSQL = """
                SELECT task_name, start_date, end_date
                FROM project_tasks
                WHERE project_id = ?
                ORDER BY start_date, id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            // Retrieve project name
            try (PreparedStatement statement =
                         connection.prepareStatement(projectSQL)) {

                statement.setInt(1, projectId);

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Project not found."
                        );

                        return;
                    }

                    projectName = result.getString("project_name");
                }
            }

            // Retrieve project tasks
            try (PreparedStatement statement =
                         connection.prepareStatement(taskSQL)) {

                statement.setInt(1, projectId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        tasks.add(new Task(
                                result.getString("task_name"),
                                result.getDate("start_date").toLocalDate(),
                                result.getDate("end_date").toLocalDate()
                        ));
                    }
                }
            }

        } catch (SQLException e) {

            log("Gantt Chart database error.", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve project information."
            );

            return;
        }

        if (tasks.isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "This project has no tasks."
            );

            return;
        }

        // Calculate project dates
        LocalDate earliest = tasks.stream()
                .map(Task::start)
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDate latest = tasks.stream()
                .map(Task::end)
                .max(LocalDate::compareTo)
                .orElseThrow();

        long projectDuration =
                ChronoUnit.DAYS.between(earliest, latest) + 1;

        // Add visual padding
        LocalDate chartStart = earliest.minusDays(2);
        LocalDate chartEnd = latest.plusDays(2);

        long totalDays =
                ChronoUnit.DAYS.between(chartStart, chartEnd) + 1;

        int timelineWidth = Math.toIntExact(totalDays * DAY_WIDTH);

        LocalDate today = LocalDate.now();

        // Generate HTML
        PrintWriter out = response.getWriter();

        out.println("""
                <!DOCTYPE html>
                <html>
                <head>

                    <title>Project Gantt Chart</title>

                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">

                    <style>

                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            padding: 30px;
                            background: #f5f7fb;
                            font-family: Arial, sans-serif;
                            color: #172033;
                        }

                        .container {
                            max-width: 1500px;
                            margin: auto;
                        }

                        .header {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 25px;
                            flex-wrap: wrap;
                            gap: 15px;
                        }

                        .header h1 {
                            margin: 0 0 8px;
                            font-size: 28px;
                        }

                        .subtitle {
                            color: #64748b;
                            margin: 0;
                        }

                        .actions {
                            display: flex;
                            gap: 10px;
                        }

                        .btn {
                            padding: 12px 18px;
                            background: #2563eb;
                            color: white;
                            text-decoration: none;
                            border: none;
                            cursor: pointer;
                            font-size: 14px;
                        }

                        .btn:hover {
                            background: #1d4ed8;
                        }

                        .btn.secondary {
                            background: white;
                            color: #2563eb;
                            border: 1px solid #2563eb;
                        }

                        .stats {
                            display: grid;
                            grid-template-columns:
                                repeat(4, 1fr);
                            gap: 18px;
                            margin-bottom: 25px;
                        }

                        .stat-card {
                            background: white;
                            padding: 22px;
                            border: 1px solid #e2e8f0;
                        }

                        .stat-card p {
                            color: #64748b;
                            margin: 0 0 12px;
                            font-size: 14px;
                        }

                        .stat-card h2 {
                            margin: 0;
                            font-size: 23px;
                        }

                        .chart-card {
                            background: white;
                            border: 1px solid #e2e8f0;
                            padding: 22px;
                        }

                        .chart-title {
                            margin: 0 0 20px;
                        }

                        .chart-scroll {
                            overflow-x: auto;
                            border: 1px solid #e2e8f0;
                        }

                        .chart-layout {
                            display: grid;
                            grid-template-columns: 250px auto;
                            min-width: max-content;
                        }

                        .task-heading {
                            background: #f8fafc;
                            padding: 15px;
                            height: 65px;
                            border-bottom: 1px solid #e2e8f0;
                            border-right: 1px solid #e2e8f0;
                            font-weight: bold;
                            display: flex;
                            align-items: center;
                        }

                        .calendar-heading {
                            height: 65px;
                            display: flex;
                            background: #f8fafc;
                            border-bottom: 1px solid #e2e8f0;
                        }

                        .day-cell {
                            min-width: 42px;
                            width: 42px;
                            border-right: 1px solid #e2e8f0;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            font-size: 12px;
                            color: #64748b;
                            gap: 5px;
                        }

                        .day-cell strong {
                            color: #172033;
                        }

                        .task-name {
                            height: 70px;
                            padding: 12px 15px;
                            display: flex;
                            flex-direction: column;
                            justify-content: center;
                            border-right: 1px solid #e2e8f0;
                            border-bottom: 1px solid #e2e8f0;
                            background: white;
                        }

                        .task-name strong {
                            font-size: 14px;
                        }

                        .task-name small {
                            margin-top: 5px;
                            color: #64748b;
                            font-size: 12px;
                        }

                        .timeline-row {
                            height: 70px;
                            position: relative;
                            border-bottom: 1px solid #e2e8f0;

                            background-image:
                                repeating-linear-gradient(
                                    to right,
                                    transparent 0,
                                    transparent 41px,
                                    #e2e8f0 41px,
                                    #e2e8f0 42px
                                );
                        }

                        .task-bar {
                            position: absolute;
                            top: 20px;
                            height: 30px;
                            background: #3b82f6;
                            border-radius: 4px;
                            color: white;
                            font-size: 11px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            white-space: nowrap;
                            overflow: hidden;
                        }

                        .task-bar:hover {
                            background: #1d4ed8;
                        }

                        .today-line {
                            position: absolute;
                            top: 0;
                            bottom: 0;
                            width: 2px;
                            background: #ef4444;
                            z-index: 5;
                        }

                        .today-label {
                            position: absolute;
                            top: 3px;
                            left: 4px;
                            color: #ef4444;
                            font-size: 10px;
                            font-weight: bold;
                        }

                        .legend {
                            margin-top: 20px;
                            color: #64748b;
                            font-size: 13px;
                        }

                        .legend span {
                            display: inline-block;
                            width: 12px;
                            height: 12px;
                            background: #3b82f6;
                            margin-right: 6px;
                        }

                        @media(max-width: 800px) {

                            body {
                                padding: 15px;
                            }

                            .stats {
                                grid-template-columns: repeat(2, 1fr);
                            }

                        }

                        @media print {

                            body {
                                background: white;
                                padding: 5px;
                            }

                            .actions {
                                display: none;
                            }

                            .chart-scroll {
                                overflow: visible;
                            }

                            .container {
                                max-width: none;
                            }

                        }

                    </style>

                </head>

                <body>
                """);

        out.println("<div class='container'>");

        // Header
        out.println("<div class='header'>");

        out.println("<div>");

        out.println("<h1>" + escapeHtml(projectName) + "</h1>");

        out.println("<p class='subtitle'>Project Timeline Overview</p>");

        out.println("</div>");

        out.println("<div class='actions'>");

        out.println("<a class='btn secondary' href='" +
                request.getContextPath() + "/timeline'>New Project</a>");

        out.println("<button class='btn' onclick='window.print()'>Print / Save PDF</button>");

        out.println("</div>");

        out.println("</div>");

        // Statistics
        out.println("<div class='stats'>");

        stat(out, "Total Tasks", String.valueOf(tasks.size()));

        stat(out, "Start Date", DATE_FORMAT.format(earliest));

        stat(out, "End Date", DATE_FORMAT.format(latest));

        stat(out, "Duration", projectDuration + " Days");

        out.println("</div>");

        // Chart
        out.println("<div class='chart-card'>");

        out.println("<h2 class='chart-title'>Project Schedule</h2>");

        out.println("<div class='chart-scroll'>");

        out.println("<div class='chart-layout'>");

        out.println("<div class='task-heading'>Task Name</div>");

        // Calendar header
        out.println("<div class='calendar-heading'>");

        for (long i = 0; i < totalDays; i++) {

            LocalDate date = chartStart.plusDays(i);

            out.println("<div class='day-cell'>");

            out.println("<strong>" + DAY_FORMAT.format(date) + "</strong>");

            out.println("<span>" + MONTH_FORMAT.format(date) + "</span>");

            out.println("</div>");
        }

        out.println("</div>");

        // Task rows
        for (Task task : tasks) {

            out.println("<div class='task-name'>");

            out.println("<strong>" + escapeHtml(task.name()) + "</strong>");

            out.println("<small>" +
                    DATE_FORMAT.format(task.start()) +
                    " – " +
                    DATE_FORMAT.format(task.end()) +
                    " (" + task.duration() + " days)</small>");

            out.println("</div>");

            out.println("<div class='timeline-row'>");

            long offset = ChronoUnit.DAYS.between(
                    chartStart,
                    task.start()
            );

            long left = offset * DAY_WIDTH;

            long width = task.duration() * DAY_WIDTH;

            out.println(
                    "<div class='task-bar' " +
                            "style='left:" + left + "px;width:" + width + "px;' " +
                            "title='" + escapeHtml(task.name()) + "'>" +
                            task.duration() + " Days</div>"
            );

            // Today indicator
            if (!today.isBefore(chartStart) &&
                    !today.isAfter(chartEnd)) {

                long todayOffset = ChronoUnit.DAYS.between(
                        chartStart,
                        today
                );

                long todayLeft = todayOffset * DAY_WIDTH;

                out.println(
                        "<div class='today-line' style='left:" +
                                todayLeft +
                                "px;'><span class='today-label'>TODAY</span></div>"
                );
            }

            out.println("</div>");
        }

        out.println("</div>");

        out.println("</div>");

        out.println("<div class='legend'>");

        out.println("<span></span> Scheduled Task");

        out.println("</div>");

        out.println("</div>");

        out.println("</div>");

        out.println("</body></html>");
    }

    // Statistics card
    private void stat(
            PrintWriter out,
            String title,
            String value
    ) {

        out.println("<div class='stat-card'>");

        out.println("<p>" + title + "</p>");

        out.println("<h2>" + escapeHtml(value) + "</h2>");

        out.println("</div>");
    }

    // HTML escaping
    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}