package com.example.studentintelligentsystem;

public class AttendanceRecord {
    private String date;
    private boolean isPresent;
    private String studentName;

    public AttendanceRecord(String date, boolean isPresent) {
        this.date = date;
        this.isPresent = isPresent;
    }

    public AttendanceRecord(String studentName, String date, boolean isPresent) {
        this.studentName = studentName;
        this.date = date;
        this.isPresent = isPresent;
    }

    public String getDate() {
        return date;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public String getStudentName() {
        return studentName;
    }
}
