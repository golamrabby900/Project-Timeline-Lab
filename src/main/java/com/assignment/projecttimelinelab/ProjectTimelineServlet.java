package com.assignment.projecttimelinelab;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/timeline")
public class ProjectTimelineServlet extends HttpServlet {

    // Display Project Timeline Form
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Project Timeline Management</title>

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
                            max-width: 950px;
                            margin: auto;
                            background: white;
                            padding: 30px;
                            border: 1px solid #e2e8f0;
                        }

                        h1 {
                            margin-top: 0;
                            font-size: 28px;
                        }

                        .subtitle {
                            color: #64748b;
                            margin-bottom: 30px;
                        }

                        label {
                            display: block;
                            font-weight: bold;
                            margin-bottom: 8px;
                        }

                        input {
                            width: 100%;
                            padding: 12px;
                            border: 1px solid #cbd5e1;
                            font-size: 15px;
                            margin-bottom: 20px;
                        }

                        .task {
                            border: 1px solid #e2e8f0;
                            padding: 20px;
                            margin-bottom: 15px;
                        }

                        .task-header {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 15px;
                        }

                        .task-header h3 {
                            margin: 0;
                        }

                        .task-fields {
                            display: grid;
                            grid-template-columns: repeat(3, 1fr);
                            gap: 15px;
                        }

                        .task-fields input {
                            margin-bottom: 0;
                        }

                        button {
                            padding: 12px 20px;
                            border: 1px solid #2563eb;
                            background: white;
                            color: #2563eb;
                            cursor: pointer;
                            font-size: 15px;
                        }

                        button:hover {
                            background: #eff6ff;
                        }

                        .remove {
                            border-color: #dc2626;
                            color: #dc2626;
                        }

                        .remove:hover {
                            background: #fef2f2;
                        }

                        .submit {
                            background: #2563eb;
                            color: white;
                            border-color: #2563eb;
                            margin-top: 25px;
                            width: 100%;
                        }

                        .submit:hover {
                            background: #1d4ed8;
                            color: white;
                        }

                        @media(max-width: 650px) {

                            body {
                                padding: 12px;
                            }

                            .task-fields {
                                grid-template-columns: 1fr;
                            }

                        }

                    </style>

                </head>

                <body>

                    <div class="container">

                        <h1>Create Project Timeline</h1>

                        <p class="subtitle">
                            Create a project and manage its tasks.
                        </p>

                        <form method="POST"
                              action="timeline"
                              onsubmit="return validateForm()">

                            <label>Project Name</label>

                            <input type="text"
                                   name="projectName"
                                   placeholder="Enter project name"
                                   required
                                   maxlength="255">

                            <h2>Project Tasks</h2>

                            <div id="taskContainer">

                                <div class="task">

                                    <div class="task-header">
                                        <h3>Task 1</h3>
                                    </div>

                                    <div class="task-fields">

                                        <div>
                                            <label>Task Name</label>

                                            <input type="text"
                                                   name="taskName"
                                                   placeholder="Task name"
                                                   required
                                                   maxlength="255">
                                        </div>

                                        <div>
                                            <label>Start Date</label>

                                            <input type="date"
                                                   name="startDate"
                                                   required>
                                        </div>

                                        <div>
                                            <label>End Date</label>

                                            <input type="date"
                                                   name="endDate"
                                                   required>
                                        </div>

                                    </div>

                                </div>

                            </div>

                            <button type="button"
                                    onclick="addTask()">
                                + Add Task
                            </button>

                            <button type="submit" class="submit">
                                Save Project & Generate Gantt Chart
                            </button>

                        </form>

                    </div>

                    <script>

                        let taskCount = 1;

                        function addTask() {

                            taskCount++;

                            const container =
                                document.getElementById("taskContainer");

                            const task = document.createElement("div");

                            task.className = "task";

                            task.innerHTML = `

                                <div class="task-header">

                                    <h3>Task ${taskCount}</h3>

                                    <button type="button"
                                            class="remove"
                                            onclick="removeTask(this)">
                                        Remove
                                    </button>

                                </div>

                                <div class="task-fields">

                                    <div>
                                        <label>Task Name</label>

                                        <input type="text"
                                               name="taskName"
                                               placeholder="Task name"
                                               required
                                               maxlength="255">
                                    </div>

                                    <div>
                                        <label>Start Date</label>

                                        <input type="date"
                                               name="startDate"
                                               required>
                                    </div>

                                    <div>
                                        <label>End Date</label>

                                        <input type="date"
                                               name="endDate"
                                               required>
                                    </div>

                                </div>

                            `;

                            container.appendChild(task);
                        }

                        function removeTask(button) {

                            const task = button.closest(".task");

                            task.remove();

                            updateTaskNumbers();
                        }

                        function updateTaskNumbers() {

                            const tasks =
                                document.querySelectorAll(".task");

                            taskCount = tasks.length;

                            tasks.forEach((task, index) => {

                                task.querySelector("h3").textContent =
                                    "Task " + (index + 1);

                            });
                        }

                        function validateForm() {

                            const tasks =
                                document.querySelectorAll(".task");

                            if (tasks.length === 0) {

                                alert("Please add at least one task.");

                                return false;
                            }

                            for (const task of tasks) {

                                const start =
                                    task.querySelector(
                                        '[name="startDate"]'
                                    ).value;

                                const end =
                                    task.querySelector(
                                        '[name="endDate"]'
                                    ).value;

                                if (end < start) {

                                    alert(
                                        "End date cannot be earlier than start date."
                                    );

                                    return false;
                                }
                            }

                            return true;
                        }

                    </script>

                </body>
                </html>
                """);
    }


    // Save Project and Tasks into MySQL
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String projectName =
                request.getParameter("projectName");

        String[] taskNames =
                request.getParameterValues("taskName");

        String[] startDates =
                request.getParameterValues("startDate");

        String[] endDates =
                request.getParameterValues("endDate");


        // Validate Project Name
        if (projectName == null || projectName.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Project name is required."
            );

            return;
        }


        // Validate Task Data
        if (taskNames == null ||
                startDates == null ||
                endDates == null ||
                taskNames.length == 0 ||
                taskNames.length != startDates.length ||
                taskNames.length != endDates.length) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid task information."
            );

            return;
        }


        List<TaskData> tasks = new ArrayList<>();

        try {

            for (int i = 0; i < taskNames.length; i++) {

                String taskName = taskNames[i].trim();

                if (taskName.isEmpty() ||
                        taskName.length() > 255) {

                    throw new IllegalArgumentException(
                            "Invalid task name."
                    );
                }

                LocalDate start =
                        LocalDate.parse(startDates[i]);

                LocalDate end =
                        LocalDate.parse(endDates[i]);

                if (end.isBefore(start)) {

                    throw new IllegalArgumentException(
                            "End date cannot be earlier than start date."
                    );
                }

                tasks.add(
                        new TaskData(taskName, start, end)
                );
            }

        } catch (RuntimeException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid task information: " + e.getMessage()
            );

            return;
        }


        // Database Transaction
        String projectSQL =
                "INSERT INTO projects (project_name) VALUES (?)";

        String taskSQL = """
                INSERT INTO project_tasks
                (project_id, task_name, start_date, end_date)
                VALUES (?, ?, ?, ?)
                """;


        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int projectId;

                // Insert Project
                try (PreparedStatement projectStatement =
                             connection.prepareStatement(
                                     projectSQL,
                                     PreparedStatement.RETURN_GENERATED_KEYS
                             )) {

                    projectStatement.setString(
                            1,
                            projectName.trim()
                    );

                    projectStatement.executeUpdate();

                    try (ResultSet keys =
                                 projectStatement.getGeneratedKeys()) {

                        if (!keys.next()) {

                            throw new SQLException(
                                    "Project ID was not generated."
                            );
                        }

                        projectId = keys.getInt(1);
                    }
                }


                // Insert Multiple Tasks
                try (PreparedStatement taskStatement =
                             connection.prepareStatement(taskSQL)) {

                    for (TaskData task : tasks) {

                        taskStatement.setInt(1, projectId);

                        taskStatement.setString(
                                2,
                                task.taskName()
                        );

                        taskStatement.setDate(
                                3,
                                Date.valueOf(task.startDate())
                        );

                        taskStatement.setDate(
                                4,
                                Date.valueOf(task.endDate())
                        );

                        taskStatement.addBatch();
                    }

                    taskStatement.executeBatch();
                }


                // Commit Transaction
                connection.commit();

                // Automatically Open Gantt Chart
                response.sendRedirect(
                        request.getContextPath()
                                + "/gantt?projectId=" + projectId
                );

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }

        } catch (Exception e) {

            log("Project saving failed.", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save project. Check the server logs."
            );
        }
    }


    // Task Data Structure
    private record TaskData(
            String taskName,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

}