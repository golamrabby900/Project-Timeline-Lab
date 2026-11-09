package com.assignment.projecttimelinelab;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Locale;

@WebServlet("/game-whiz")
public class GameServlet extends HttpServlet {

    private final GameDAO gameDAO = new GameDAO();

    private final Random random = new Random();

    private static final String GAME_KEY = "activeGame";

    private static final String QUESTIONS_KEY = "gameQuestions";

    private static final String INDEX_KEY = "questionIndex";


    // ==========================================
    // QUESTION MODEL
    // ==========================================

    private static class Question implements java.io.Serializable {

        String operation;
        String question;

        String option1;
        String option2;
        String option3;

        String correctAnswer;

        int marks;
    }


    // ==========================================
    // GET REQUEST
    // ==========================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");

        if ("leaderboard".equals(action)) {

            showLeaderboard(response);
            return;
        }

        showWelcomePage(response);
    }


    // ==========================================
    // POST REQUEST
    // ==========================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");

        if ("start".equals(action)) {
            startGame(request, response);
            return;
        }

        if ("answer".equals(action)) {
            submitAnswer(request, response);
            return;
        }

        if ("continue".equals(action)) {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute(GAME_KEY) == null) {
                response.sendRedirect(request.getContextPath() + "/game-whiz");
                return;
            }

            GameSession game = (GameSession) session.getAttribute(GAME_KEY);

            @SuppressWarnings("unchecked")
            List<Question> questions =
                    (List<Question>) session.getAttribute(QUESTIONS_KEY);

            Integer index = (Integer) session.getAttribute(INDEX_KEY);

            if (questions == null || index == null || index < 0 ||
                    index > questions.size()) {
                response.sendRedirect(request.getContextPath() + "/game-whiz");
                return;
            }

            if (index == questions.size()) {
                session.setAttribute("gameCompleted", true);
                showResult(response, session, game);
                return;
            }

            showQuestion(response, game, questions.get(index), index + 1);
            return;
        }

        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid game action."
        );
    }


    // ==========================================
    // 1. WELCOME PAGE
    // ==========================================

    private void showWelcomePage(
            HttpServletResponse response
    ) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                <title>GolamRabby Game Whiz</title>

                <style>

                body {
                    background: #e8e8e8;
                    color: black;
                    font-family: "Courier New", monospace;
                    text-align: center;
                    margin: 0;
                }

                .header {
                    background: #000080;
                    color: white;
                    padding: 20px;
                    border-bottom: 5px solid gold;
                }

                .box {
                    width: 420px;
                    margin: 80px auto;
                    padding: 30px;
                    background: white;
                    border: 3px double black;
                }

                input {
                    padding: 12px;
                    width: 85%;
                    border: 2px solid black;
                    font-size: 16px;
                }

                button {
                    padding: 12px 25px;
                    background: navy;
                    color: white;
                    border: 2px solid black;
                    cursor: pointer;
                    font-weight: bold;
                }

                button:hover {
                    background: green;
                }

                a {
                    color: navy;
                }

                </style>
                </head>

                <body>

                <div class="header">
                    <h1>GOLAMRABBY GAME WHIZ</h1>
                    <h3>JAVA MATHEMATICAL CHALLENGE</h3>
                </div>

                <div class="box">

                    <h2>WELCOME PLAYER!</h2>

                    <p>Test Your Mathematical Knowledge</p>

                    <p>Four Questions | Ten Marks</p>

                    <form method="post">

                        <input type="hidden"
                               name="action"
                               value="start">

                        <p>Enter Your Name:</p>

                        <input type="text"
                               name="playerName"
                               maxlength="100"
                               required
                               placeholder="Your Name">

                        <br><br>

                        <button type="submit">
                            START GAME
                        </button>

                    </form>

                    <hr>

                    <p>
                    <a href="game-whiz?action=leaderboard">
                        VIEW TOP 10 LEADERBOARD
                    </a>
                    </p>

                </div>

                </body>
                </html>
                """);

        response.getWriter().write(html.toString());
    }


    // ==========================================
    // 2. START GAME
    // ==========================================

    private void startGame(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String playerName = request.getParameter("playerName");

        if (playerName == null || playerName.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Player name is required."
            );

            return;
        }

        playerName = playerName.trim();

        if (playerName.length() > 100) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Player name is too long."
            );

            return;
        }

        HttpSession session = request.getSession();

        GameSession game = new GameSession();

        game.setPlayerName(playerName);
        game.setDifficulty("NORMAL");

        game.setTotalQuestions(4);
        game.setCorrectAnswers(0);
        game.setWrongAnswers(0);
        game.setScore(0);

        try {

            // SAVE NEW SESSION IN DATABASE

            gameDAO.createSession(game);

            // GENERATE FOUR QUESTIONS

            List<Question> questions = generateQuestions();

            session.setAttribute(GAME_KEY, game);

            session.setAttribute(QUESTIONS_KEY, questions);

            session.setAttribute(INDEX_KEY, 0);

            showQuestion(response, game, questions.get(0), 1);

        } catch (SQLException e) {

            throw new IOException(
                    "Database error while starting game.", e
            );
        }
    }


    // ==========================================
    // 3. GENERATE FOUR QUESTIONS
    // ==========================================

    private List<Question> generateQuestions() {

        List<Question> questions = new ArrayList<>();

        // QUESTION 1: PRIME (2 MARKS)

        questions.add(generatePrimeQuestion());

        // QUESTION 2: MOD (2 MARKS)

        questions.add(generateModQuestion());

        // QUESTION 3: TRIGONOMETRY (3 MARKS)

        questions.add(generateTrigQuestion());

        // QUESTION 4: TRIGONOMETRY (3 MARKS)

        questions.add(generateTrigQuestion());

        return questions;
    }


    // ==========================================
    // PRIME QUESTION
    // ==========================================

    private Question generatePrimeQuestion() {

        Question q = new Question();

        q.operation = "PRIME";

        q.marks = 2;

        int number = random.nextInt(90) + 10;

        q.question = "Is " + number + " a prime number?";

        boolean prime = MathService.isPrime(number) == 1;

        q.correctAnswer = prime ? "YES" : "NO";

        q.option1 = "YES";
        q.option2 = "NO";

        return q;
    }


    // ==========================================
    // MODULUS QUESTION
    // ==========================================

    private Question generateModQuestion() {

        Question q = new Question();

        q.operation = "MOD";

        q.marks = 2;

        int a = random.nextInt(80) + 20;

        int b = random.nextInt(9) + 2;

        int answer = MathService.calculateMod(a, b);

        q.question = "Calculate " + a + " MOD " + b;

        q.correctAnswer = String.valueOf(answer);

        setThreeOptions(q, answer);

        return q;
    }


    // ==========================================
    // TRIGONOMETRY QUESTION
    // ==========================================

    private Question generateTrigQuestion() {

        Question q = new Question();

        q.marks = 3;

        String[] operations = {"SIN", "COS", "TAN"};

        String operation = operations[random.nextInt(3)];

        q.operation = operation;

        int[] angles = {0, 30, 45, 60, 90, 120, 135, 150, 180};

        int degree;

        do {

            degree = angles[random.nextInt(angles.length)];

        } while (operation.equals("TAN") &&
                (degree == 90));

        double answer;

        switch (operation) {

            case "SIN":

                answer = MathService.calculateSin(degree);
                break;

            case "COS":

                answer = MathService.calculateCos(degree);
                break;

            default:

                answer = MathService.calculateTan(degree);
                break;
        }

        q.question = operation + " (" + degree + " degrees)";

        q.correctAnswer = format(answer);

        setThreeOptions(q, Double.parseDouble(q.correctAnswer));

        return q;
    }


    // ==========================================
    // GENERATE THREE OPTIONS
    // ==========================================

    private void setThreeOptions(
            Question q,
            double answer
    ) {

        List<String> options = new ArrayList<>();

        String correct = format(answer);

        options.add(correct);

        while (options.size() < 3) {

            double wrong = answer +
                    (random.nextBoolean() ? 1 : -1) *
                            (0.2 + random.nextDouble() * 1.5);

            String wrongOption = format(wrong);

            if (!options.contains(wrongOption)) {

                options.add(wrongOption);
            }
        }

        Collections.shuffle(options);

        q.option1 = options.get(0);
        q.option2 = options.get(1);
        q.option3 = options.get(2);
    }


    // ==========================================
    // FORMAT DECIMAL ANSWERS
    // ==========================================

    private String format(double number) {

        return String.format(Locale.US, "%.3f", number);
    }


    // ==========================================
    // 4. DISPLAY QUESTION
    // ==========================================

    private void showQuestion(
            HttpServletResponse response,
            GameSession game,
            Question q,
            int questionNumber
    ) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>

                <title>Game Whiz - Question</title>

                <style>

                body {
                    background: #eeeeee;
                    color: black;
                    font-family: "Courier New", monospace;
                    text-align: center;
                }

                .header {
                    background: navy;
                    color: white;
                    padding: 15px;
                    border-bottom: 4px solid gold;
                }

                .box {
                    background: white;
                    width: 500px;
                    margin: 50px auto;
                    padding: 25px;
                    border: 3px double black;
                }

                .option {
                    display: block;
                    padding: 12px;
                    margin: 12px;
                    border: 1px solid black;
                    background: #f5f5f5;
                    cursor: pointer;
                }

                button {
                    padding: 12px 30px;
                    background: navy;
                    color: white;
                    border: 2px solid black;
                    cursor: pointer;
                }

                </style>

                </head>

                <body>

                <div class="header">
                    <h1>GOLAMRABBY GAME WHIZ</h1>
                </div>

                <div class="box">
                """);

        html.append("<h2>Welcome, ")
                .append(escape(game.getPlayerName()))
                .append("</h2>");

        html.append("<h3>QUESTION ")
                .append(questionNumber)
                .append(" OF 4</h3>");

        html.append("<p>Marks: ")
                .append(q.marks)
                .append("</p>");

        html.append("<hr>");

        html.append("<h2>")
                .append(escape(q.question))
                .append("</h2>");

        html.append("<form method='post'>");

        html.append("<input type='hidden' name='action' value='answer'>");

        appendOption(html, q.option1);
        appendOption(html, q.option2);

        if (q.option3 != null) {

            appendOption(html, q.option3);
        }

        html.append("<br>");

        html.append("<button type='submit'>")
                .append("SUBMIT ANSWER")
                .append("</button>");

        html.append("</form>");

        html.append("<hr>");

        html.append("<p>Difficulty: NORMAL</p>");

        html.append("</div>");

        html.append("</body></html>");

        response.getWriter().write(html.toString());
    }


    private void appendOption(
            StringBuilder html,
            String option
    ) {

        html.append("<label class='option'>");

        html.append("<input type='radio' name='answer' value='")
                .append(escape(option))
                .append("' required>");

        html.append(" ")
                .append(escape(option));

        html.append("</label>");
    }


    // ==========================================
    // 5. SUBMIT AND CHECK ANSWER
    // ==========================================

    private void submitAnswer(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute(GAME_KEY) == null) {

            response.sendRedirect(
                    request.getContextPath() + "/game-whiz"
            );

            return;
        }

        GameSession game =
                (GameSession) session.getAttribute(GAME_KEY);

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(QUESTIONS_KEY);

        Integer index =
                (Integer) session.getAttribute(INDEX_KEY);

        if (questions == null || index == null ||
                index >= questions.size()) {

            response.sendRedirect(
                    request.getContextPath() + "/game-whiz"
            );

            return;
        }

        Question q = questions.get(index);

        String selectedAnswer = request.getParameter("answer");

        if (selectedAnswer == null ||
                !(selectedAnswer.equals(q.option1) ||
                        selectedAnswer.equals(q.option2) ||
                        selectedAnswer.equals(q.option3))) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid answer selection."
            );

            return;
        }

        boolean correct;

        if (q.operation.equals("PRIME")) {

            correct = selectedAnswer.equals(q.correctAnswer);

        } else {

            correct = MathService.compareAnswers(
                    Double.parseDouble(q.correctAnswer),
                    Double.parseDouble(selectedAnswer)
            );
        }

        int marks = correct ? q.marks : 0;

        // UPDATE PLAYER STATISTICS

        if (correct) {

            game.setCorrectAnswers(
                    game.getCorrectAnswers() + 1
            );

        } else {

            game.setWrongAnswers(
                    game.getWrongAnswers() + 1
            );
        }

        game.setScore(game.getScore() + marks);

        // SAVE QUESTION HISTORY

        GameHistory history = new GameHistory();

        history.setSessionId(game.getSessionId());

        history.setQuestionNumber(index + 1);

        history.setOperation(q.operation);

        history.setQuestion(q.question);

        history.setOption1(q.option1);

        history.setOption2(q.option2);

        history.setOption3(q.option3);

        history.setCorrectAnswer(q.correctAnswer);

        history.setSelectedAnswer(selectedAnswer);

        history.setResult(correct ? "RIGHT" : "WRONG");

        history.setMarksAwarded(marks);

        try {

            gameDAO.saveHistory(history);

            gameDAO.updateSession(game);

        } catch (SQLException e) {

            throw new IOException(
                    "Unable to save game result.", e
            );
        }

        showFeedback(
                response,
                game,
                q,
                correct,
                marks
        );

        index++;

        session.setAttribute(INDEX_KEY, index);

        if (index < questions.size()) {

            // NEXT QUESTION WILL BE SHOWN
            // THROUGH THE CONTINUE BUTTON

        } else {

            session.setAttribute("gameCompleted", true);
        }
    }


    // ==========================================
    // 6. FEEDBACK PAGE
    // ==========================================

    private void showFeedback(
            HttpServletResponse response,
            GameSession game,
            Question q,
            boolean correct,
            int marks
    ) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <title>Game Whiz - Answer</title>
                <style>
                body {
                    background: #eeeeee;
                    text-align: center;
                    font-family: "Courier New", monospace;
                }
                .header {
                    background: navy;
                    color: white;
                    padding: 20px;
                }
                .box {
                    background: white;
                    width: 450px;
                    max-width: 90%;
                    margin: 70px auto;
                    padding: 30px;
                    border: 3px double black;
                }
                button {
                    padding: 12px 30px;
                    background: navy;
                    color: white;
                    border: 2px solid black;
                    cursor: pointer;
                }
                </style>
                </head>
                <body>
                <div class="header">
                    <h1>GOLAMRABBY GAME WHIZ</h1>
                </div>
                <div class="box">
                """);

        html.append("<h2>")
                .append(correct ? "RIGHT ANSWER!" : "WRONG ANSWER!")
                .append("</h2>");

        html.append("<p>Correct Answer: ")
                .append(escape(q.correctAnswer))
                .append("</p>");

        html.append("<p>Marks Awarded: ")
                .append(marks)
                .append(" / ")
                .append(q.marks)
                .append("</p>");

        html.append("<h3>Current Score: ")
                .append(game.getScore())
                .append(" / 10</h3>");

        html.append("""
                <form method="post" action="game-whiz">
                    <input type="hidden" name="action" value="continue">
                    <button type="submit">CONTINUE</button>
                </form>
                </div>
                </body>
                </html>
                """);

        response.getWriter().write(html.toString());
    }


    // ==========================================
    // 7. CONTINUE GAME
    // ==========================================

    // Handled in doPost through this method.
    // See the doPost continuation below.


    // ==========================================
    // 8. RESULT PAGE
    // ==========================================

    private void showResult(
            HttpServletResponse response,
            HttpSession session,
            GameSession game
    ) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>

                <title>Final Result</title>

                <style>

                body {
                    background: #eeeeee;
                    text-align: center;
                    font-family: "Courier New", monospace;
                }

                .header {
                    background: navy;
                    color: white;
                    padding: 20px;
                }

                .box {
                    background: white;
                    width: 450px;
                    margin: 60px auto;
                    padding: 30px;
                    border: 3px double black;
                }

                a {
                    display: inline-block;
                    margin: 10px;
                    padding: 12px;
                    background: navy;
                    color: white;
                    text-decoration: none;
                }

                </style>

                </head>

                <body>

                <div class="header">
                    <h1>GOLAMRABBY GAME WHIZ</h1>
                </div>

                <div class="box">
                """);

        html.append("<h2>GAME COMPLETED!</h2>");

        html.append("<h3>Player: ")
                .append(escape(game.getPlayerName()))
                .append("</h3>");

        html.append("<h1>FINAL SCORE: ")
                .append(game.getScore())
                .append(" / 10</h1>");

        html.append("<p>Total Questions: 4</p>");

        html.append("<p>Correct Answers: ")
                .append(game.getCorrectAnswers())
                .append("</p>");

        html.append("<p>Wrong Answers: ")
                .append(game.getWrongAnswers())
                .append("</p>");

        html.append("<p>Difficulty: NORMAL</p>");

        html.append("<a href='game-whiz?action=leaderboard'>");

        html.append("VIEW LEADERBOARD");

        html.append("</a>");

        html.append("<a href='game-whiz'>");

        html.append("PLAY AGAIN");

        html.append("</a>");

        html.append("</div></body></html>");

        response.getWriter().write(html.toString());
    }


    // ==========================================
    // 9. TOP 10 LEADERBOARD
    // ==========================================

    private void showLeaderboard(
            HttpServletResponse response
    ) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>

                <title>Game Whiz Leaderboard</title>

                <style>

                body {
                    background: #eeeeee;
                    font-family: "Courier New", monospace;
                    text-align: center;
                }

                .header {
                    background: navy;
                    color: white;
                    padding: 20px;
                }

                table {
                    border-collapse: collapse;
                    width: 85%;
                    margin: 40px auto;
                    background: white;
                }

                th, td {
                    border: 1px solid black;
                    padding: 12px;
                }

                th {
                    background: navy;
                    color: white;
                }

                tr:nth-child(even) {
                    background: #eeeeee;
                }

                a {
                    color: navy;
                }

                </style>

                </head>

                <body>

                <div class="header">
                    <h1>GOLAMRABBY GAME WHIZ</h1>
                    <h2>TOP 10 LEADERBOARD</h2>
                </div>

                <table>

                <tr>
                    <th>Rank</th>
                    <th>Player</th>
                    <th>Score</th>
                    <th>Difficulty</th>
                    <th>Played At</th>
                </tr>
                """);

        try {

            List<GameSession> players =
                    gameDAO.getTopScores();

            int rank = 1;

            for (GameSession player : players) {

                html.append("<tr>");

                html.append("<td>")
                        .append(rank++)
                        .append("</td>");

                html.append("<td>")
                        .append(escape(player.getPlayerName()))
                        .append("</td>");

                html.append("<td>")
                        .append(player.getScore())
                        .append(" / 10</td>");

                html.append("<td>")
                        .append(escape(player.getDifficulty()))
                        .append("</td>");

                html.append("<td>")
                        .append(player.getPlayedAt())
                        .append("</td>");

                html.append("</tr>");
            }

        } catch (SQLException e) {

            throw new IOException(
                    "Unable to retrieve leaderboard.", e
            );
        }

        html.append("""
                </table>

                <p>
                <a href="game-whiz">BACK TO HOME</a>
                </p>

                </body>
                </html>
                """);

        response.getWriter().write(html.toString());
    }


    // ==========================================
    // HTML ESCAPING
    // ==========================================

    private String escape(String value) {

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