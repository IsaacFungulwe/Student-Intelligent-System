package com.example.studentintelligentsystem;

public class Student {

    private int id;
    private String name;
    private int grade;
    private int age;
    private String gender;
    private String parentName;
    private String parentEmail;
    private String parentPhone;
    private String address;

    public Student() {}

    public Student(String name, int grade, int age, String gender, String parentName, String parentEmail, String parentPhone, String address) {
        this.name = name;
        this.grade = grade;
        this.age = age;
        this.gender = gender;
        this.parentName = parentName;
        this.parentEmail = parentEmail;
        this.parentPhone = parentPhone;
        this.address = address;
    }

    public Student(int id, String name, int grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGrade() {
        return grade;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
