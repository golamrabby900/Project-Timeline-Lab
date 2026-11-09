package com.assignment.projecttimelinelab;

import java.sql.Timestamp;

public class GameSession {

    private int sessionId;
    private String playerName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int score;
    private String difficulty;
    private Timestamp playedAt;


    // DEFAULT CONSTRUCTOR

    public GameSession() {
        this.difficulty = "NORMAL";
        this.totalQuestions = 4;
    }


    // GETTERS AND SETTERS

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }


    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }


    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }


    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }


    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }


    public Timestamp getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Timestamp playedAt) {
        this.playedAt = playedAt;
    }

}