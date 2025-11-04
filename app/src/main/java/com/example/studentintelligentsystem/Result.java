package com.example.studentintelligentsystem;

/**
 * Model class representing a student result record
 */
public class Result {
    private int resultId;
    private int studentId;
    private String studentName;
    private String subject;
    private String term;
    private int marks;
    private String comment;

    public Result() {}

    public Result(int resultId, int studentId, String studentName, String subject, String term, int marks, String comment) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
        this.term = term;
        this.marks = marks;
        this.comment = comment;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

