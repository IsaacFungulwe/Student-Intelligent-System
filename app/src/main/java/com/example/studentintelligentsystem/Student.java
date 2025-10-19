package com.example.studentintelligentsystem;

import org.jetbrains.annotations.NotNull;

public class Student {
    private int id;
    private String name;
    private int grade;

    public Student(int id, String name, int grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    public Student() {}

    public int getId() { return id; }
    public String getName() { return name; }
    public int getGrade() { return grade; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setGrade(int grade) { this.grade = grade; }

    @NotNull
    @Override
    public String toString() {
        return id + ": " + name + " (Grade " + grade + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student)) return false;
        Student student = (Student) obj;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
