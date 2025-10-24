package com.example.studentintelligentsystem;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable {

    private long id;
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

    public Student(long id, String name, int grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    protected Student(Parcel in) {
        id = in.readLong();
        name = in.readString();
        grade = in.readInt();
        age = in.readInt();
        gender = in.readString();
        parentName = in.readString();
        parentEmail = in.readString();
        parentPhone = in.readString();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(grade);
        dest.writeInt(age);
        dest.writeString(gender);
        dest.writeString(parentName);
        dest.writeString(parentEmail);
        dest.writeString(parentPhone);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public long getId() {
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

    public void setId(long id) {
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
