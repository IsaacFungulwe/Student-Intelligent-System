package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentIntelligentSystem.db";
    private static final int DATABASE_VERSION = 4; // Incremented for schema change

    // --- TABLE DEFINITIONS ---

    // Admin Table
    public static final String TABLE_ADMIN = "Admin";
    public static final String ADMIN_ID = "adminId";
    public static final String ADMIN_SCHOOL_NAME = "schoolName";
    public static final String ADMIN_DISTRICT = "district";
    public static final String ADMIN_EMAIL = "schoolEmail";
    public static final String ADMIN_PASSWORD = "password";

    // Teacher Table
    public static final String TABLE_TEACHER = "Teacher";
    public static final String TEACHER_ID = "teacherId";
    public static final String TEACHER_NAME = "name";
    public static final String TEACHER_EMAIL = "email";
    public static final String TEACHER_PASSWORD = "password";
    public static final String TEACHER_GRADE_ASSIGNED = "gradeAssigned";
    public static final String TEACHER_FK_ADMIN_ID = "linkedSchoolAdminId";

    // Parent Table
    public static final String TABLE_PARENT = "Parent";
    public static final String PARENT_ID = "parentId";
    public static final String PARENT_NAME = "parentName";
    public static final String PARENT_EMAIL = "parentEmail";
    public static final String PARENT_PHONE = "parentPhone";
    public static final String PARENT_PASSWORD = "password";
    public static final String PARENT_FK_ADMIN_ID = "linkedSchoolAdminId";

    // Student Table
    public static final String TABLE_STUDENT = "Student";
    public static final String STUDENT_ID = "studentId";
    public static final String STUDENT_NAME = "studentName";
    public static final String STUDENT_AGE = "age";
    public static final String STUDENT_GENDER = "gender";
    public static final String STUDENT_GRADE = "grade";
    public static final String STUDENT_ADDRESS = "address";
    public static final String STUDENT_FK_PARENT_ID = "linkedParentId";
    public static final String STUDENT_FK_TEACHER_ID = "linkedTeacherId";

    // Attendance Table
    public static final String TABLE_ATTENDANCE = "Attendance";
    public static final String ATTENDANCE_ID = "attendanceId";
    public static final String ATTENDANCE_FK_STUDENT_ID = "studentId";
    public static final String ATTENDANCE_DATE = "date";
    public static final String ATTENDANCE_STATUS = "status"; // Present, Absent
    public static final String ATTENDANCE_FK_TEACHER_ID = "markedByTeacherId";

    // Results Table
    public static final String TABLE_RESULTS = "Results";
    public static final String RESULT_ID = "resultId";
    public static final String RESULT_FK_STUDENT_ID = "studentId";
    public static final String RESULT_SUBJECT = "subject";
    public static final String RESULT_TERM = "term";
    public static final String RESULT_MARKS = "marks";
    public static final String RESULT_FK_TEACHER_ID = "recordedByTeacherId";

    // Announcement Table
    public static final String TABLE_ANNOUNCEMENT = "Announcement";
    public static final String ANNOUNCEMENT_ID = "announcementId";
    public static final String ANNOUNCEMENT_TITLE = "title";
    public static final String ANNOUNCEMENT_MESSAGE = "message";
    public static final String ANNOUNCEMENT_ROLE = "createdByRole"; // Admin, Teacher
    public static final String ANNOUNCEMENT_FK_CREATOR_ID = "createdById";
    public static final String ANNOUNCEMENT_GRADE_TARGET = "gradeTarget";
    public static final String ANNOUNCEMENT_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL for creating tables
        final String CREATE_ADMIN_TABLE = "CREATE TABLE " + TABLE_ADMIN + "(" +
                ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ADMIN_SCHOOL_NAME + " TEXT NOT NULL," +
                ADMIN_DISTRICT + " TEXT NOT NULL," +
                ADMIN_EMAIL + " TEXT NOT NULL UNIQUE," +
                ADMIN_PASSWORD + " TEXT NOT NULL);";

        final String CREATE_TEACHER_TABLE = "CREATE TABLE " + TABLE_TEACHER + "(" +
                TEACHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TEACHER_NAME + " TEXT NOT NULL," +
                TEACHER_EMAIL + " TEXT NOT NULL UNIQUE," +
                TEACHER_PASSWORD + " TEXT NOT NULL," +
                TEACHER_GRADE_ASSIGNED + " INTEGER NOT NULL," +
                TEACHER_FK_ADMIN_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + TEACHER_FK_ADMIN_ID + ") REFERENCES " + TABLE_ADMIN + "(" + ADMIN_ID + "));";

        final String CREATE_PARENT_TABLE = "CREATE TABLE " + TABLE_PARENT + "(" +
                PARENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PARENT_NAME + " TEXT NOT NULL," +
                PARENT_EMAIL + " TEXT NOT NULL UNIQUE," +
                PARENT_PHONE + " TEXT," +
                PARENT_PASSWORD + " TEXT NOT NULL," +
                PARENT_FK_ADMIN_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + PARENT_FK_ADMIN_ID + ") REFERENCES " + TABLE_ADMIN + "(" + ADMIN_ID + "));";

        final String CREATE_STUDENT_TABLE = "CREATE TABLE " + TABLE_STUDENT + "(" +
                STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                STUDENT_NAME + " TEXT NOT NULL," +
                STUDENT_AGE + " INTEGER," +
                STUDENT_GENDER + " TEXT," +
                STUDENT_GRADE + " INTEGER NOT NULL," +
                STUDENT_ADDRESS + " TEXT," +
                STUDENT_FK_PARENT_ID + " INTEGER NOT NULL," +
                STUDENT_FK_TEACHER_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + STUDENT_FK_PARENT_ID + ") REFERENCES " + TABLE_PARENT + "(" + PARENT_ID + ")," +
                "FOREIGN KEY(" + STUDENT_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "));";

        final String CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE + "(" +
                ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ATTENDANCE_FK_STUDENT_ID + " INTEGER NOT NULL," +
                ATTENDANCE_DATE + " TEXT NOT NULL," +
                ATTENDANCE_STATUS + " TEXT NOT NULL," +
                ATTENDANCE_FK_TEACHER_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + ATTENDANCE_FK_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + STUDENT_ID + ")," +
                "FOREIGN KEY(" + ATTENDANCE_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "));";

        final String CREATE_RESULTS_TABLE = "CREATE TABLE " + TABLE_RESULTS + "(" +
                RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RESULT_FK_STUDENT_ID + " INTEGER NOT NULL," +
                RESULT_SUBJECT + " TEXT NOT NULL," +
                RESULT_TERM + " TEXT NOT NULL," +
                RESULT_MARKS + " INTEGER NOT NULL," +
                RESULT_FK_TEACHER_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + RESULT_FK_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + STUDENT_ID + ")," +
                "FOREIGN KEY(" + RESULT_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "));";

        final String CREATE_ANNOUNCEMENT_TABLE = "CREATE TABLE " + TABLE_ANNOUNCEMENT + "(" +
                ANNOUNCEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ANNOUNCEMENT_TITLE + " TEXT NOT NULL," +
                ANNOUNCEMENT_MESSAGE + " TEXT NOT NULL," +
                ANNOUNCEMENT_ROLE + " TEXT NOT NULL," +
                ANNOUNCEMENT_FK_CREATOR_ID + " INTEGER NOT NULL," +
                ANNOUNCEMENT_GRADE_TARGET + " INTEGER," +
                ANNOUNCEMENT_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

        // Execute all CREATE TABLE statements
        db.execSQL(CREATE_ADMIN_TABLE);
        db.execSQL(CREATE_TEACHER_TABLE);
        db.execSQL(CREATE_PARENT_TABLE);
        db.execSQL(CREATE_STUDENT_TABLE);
        db.execSQL(CREATE_ATTENDANCE_TABLE);
        db.execSQL(CREATE_RESULTS_TABLE);
        db.execSQL(CREATE_ANNOUNCEMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNOUNCEMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        // Recreate all tables
        onCreate(db);
    }

    /**
     * A simple, insecure password hashing example. 
     * DO NOT use in a production app. Use BCrypt or a similar library.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback to a simpler (and even less secure) method if SHA-256 is not available
            return String.valueOf(password.hashCode());
        }
    }

    // Method to check user credentials (example for one role)
    public Cursor checkLogin(String email, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName;
        String emailColumn;
        String passwordColumn;

        switch (role) {
            case "Admin":
                tableName = TABLE_ADMIN;
                emailColumn = ADMIN_EMAIL;
                passwordColumn = ADMIN_PASSWORD;
                break;
            case "Teacher":
                tableName = TABLE_TEACHER;
                emailColumn = TEACHER_EMAIL;
                passwordColumn = TEACHER_PASSWORD;
                break;
            case "Parent":
                tableName = TABLE_PARENT;
                emailColumn = PARENT_EMAIL;
                passwordColumn = PARENT_PASSWORD;
                break;
            default:
                return null;
        }

        String hashedPassword = hashPassword(password);
        String selection = emailColumn + " = ? AND " + passwordColumn + " = ?";
        String[] selectionArgs = {email, hashedPassword};

        return db.query(tableName, null, selection, selectionArgs, null, null, null);
    }

    // ... We will add more methods here for registration, data insertion, and retrieval ...

}
