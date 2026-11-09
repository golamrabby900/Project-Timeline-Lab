package com.assignment.projecttimelinelab;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    // =====================================
    // 1. CREATE NEW GAME SESSION
    // =====================================

    public int createSession(GameSession game) throws SQLException {

        String sql = "INSERT INTO game_sessions " +
                "(player_name, score, difficulty, total_questions, " +
                "correct_answers, wrong_answers) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, game.getPlayerName());
            ps.setInt(2, game.getScore());
            ps.setString(3, game.getDifficulty());
            ps.setInt(4, game.getTotalQuestions());
            ps.setInt(5, game.getCorrectAnswers());
            ps.setInt(6, game.getWrongAnswers());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {

                    int sessionId = rs.getInt(1);

                    game.setSessionId(sessionId);

                    return sessionId;
                }
            }
        }

        throw new SQLException("Unable to create game session.");
    }


    // =====================================
    // 2. SAVE QUESTION HISTORY
    // =====================================

    public boolean saveHistory(GameHistory history)
            throws SQLException {

        String sql = "INSERT INTO game_history " +
                "(session_id, question_number, operation, question, " +
                "option_1, option_2, option_3, correct_answer, " +
                "selected_answer, result, marks_awarded) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, history.getSessionId());
            ps.setInt(2, history.getQuestionNumber());
            ps.setString(3, history.getOperation());
            ps.setString(4, history.getQuestion());

            ps.setString(5, history.getOption1());
            ps.setString(6, history.getOption2());
            ps.setString(7, history.getOption3());

            ps.setString(8, history.getCorrectAnswer());
            ps.setString(9, history.getSelectedAnswer());

            ps.setString(10, history.getResult());
            ps.setInt(11, history.getMarksAwarded());

            return ps.executeUpdate() == 1;
        }
    }


    // =====================================
    // 3. UPDATE PLAYER SCORE
    // =====================================

    public boolean updateSession(GameSession game)
            throws SQLException {

        String sql = "UPDATE game_sessions SET " +
                "score = ?, " +
                "total_questions = ?, " +
                "correct_answers = ?, " +
                "wrong_answers = ? " +
                "WHERE session_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, game.getScore());
            ps.setInt(2, game.getTotalQuestions());
            ps.setInt(3, game.getCorrectAnswers());
            ps.setInt(4, game.getWrongAnswers());
            ps.setInt(5, game.getSessionId());

            return ps.executeUpdate() == 1;
        }
    }


    // =====================================
    // 4. RETRIEVE TOP 10 COMPLETED GAMES
    // =====================================

    public List<GameSession> getTopScores()
            throws SQLException {

        List<GameSession> players = new ArrayList<>();

        String sql = "SELECT gs.* FROM game_sessions gs " +
                "WHERE (SELECT COUNT(*) FROM game_history gh " +
                "WHERE gh.session_id = gs.session_id) = 4 " +
                "ORDER BY gs.score DESC, gs.played_at ASC LIMIT 10";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                GameSession game = new GameSession();

                game.setSessionId(
                        rs.getInt("session_id"));

                game.setPlayerName(
                        rs.getString("player_name"));

                game.setScore(
                        rs.getInt("score"));

                game.setDifficulty(
                        rs.getString("difficulty"));

                game.setTotalQuestions(
                        rs.getInt("total_questions"));

                game.setCorrectAnswers(
                        rs.getInt("correct_answers"));

                game.setWrongAnswers(
                        rs.getInt("wrong_answers"));

                game.setPlayedAt(
                        rs.getTimestamp("played_at"));

                players.add(game);
            }
        }

        return players;
    }


    // =====================================
    // 5. RETRIEVE INDIVIDUAL GAME HISTORY
    // =====================================

    public List<GameHistory> getHistory(int sessionId)
            throws SQLException {

        List<GameHistory> historyList = new ArrayList<>();

        String sql = "SELECT * FROM game_history " +
                "WHERE session_id = ? " +
                "ORDER BY question_number ASC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    GameHistory history = new GameHistory();

                    history.setId(
                            rs.getInt("id"));

                    history.setSessionId(
                            rs.getInt("session_id"));

                    history.setQuestionNumber(
                            rs.getInt("question_number"));

                    history.setOperation(
                            rs.getString("operation"));

                    history.setQuestion(
                            rs.getString("question"));

                    history.setOption1(
                            rs.getString("option_1"));

                    history.setOption2(
                            rs.getString("option_2"));

                    history.setOption3(
                            rs.getString("option_3"));

                    history.setCorrectAnswer(
                            rs.getString("correct_answer"));

                    history.setSelectedAnswer(
                            rs.getString("selected_answer"));

                    history.setResult(
                            rs.getString("result"));

                    history.setMarksAwarded(
                            rs.getInt("marks_awarded"));

                    history.setPlayedAt(
                            rs.getTimestamp("played_at"));

                    historyList.add(history);
                }
            }
        }

        return historyList;
    }

}