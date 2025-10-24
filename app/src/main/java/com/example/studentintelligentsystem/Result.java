package com.example.studentintelligentsystem;

public class Result {
    private String subject;
    private int score;

    public Result(String subject, int score) {
        this.subject = subject;
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public int getScore() {
        return score;
    }
}
