package com.assignment.projecttimelinelab;

import java.sql.Timestamp;

public class GameHistory {

    private int id;
    private int sessionId;
    private int questionNumber;

    private String operation;
    private String question;

    private String option1;
    private String option2;
    private String option3;

    private String correctAnswer;
    private String selectedAnswer;

    private String result;
    private int marksAwarded;

    private Timestamp playedAt;


    // DEFAULT CONSTRUCTOR

    public GameHistory() {
    }


    // GETTERS AND SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }


    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }


    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }


    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }


    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }


    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }


    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public int getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(int marksAwarded) {
        this.marksAwarded = marksAwarded;
    }


    public Timestamp getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Timestamp playedAt) {
        this.playedAt = playedAt;
    }

}