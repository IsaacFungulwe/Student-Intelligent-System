package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student_system.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_PARENTS = "parents";
    private static final String TABLE_USERS = "users";

    // Students columns
    private static final String COL_STU_ID = "id";
    private static final String COL_STU_NAME = "stu_name";
    private static final String COL_STU_GRADE = "stu_grade";

    // Parents columns
    private static final String COL_PARENT_ID = "id";
    private static final String COL_PARENT_EMAIL = "email";
    private static final String COL_PARENT_PASSWORD = "password";

    // Users columns (for login system, optional)
    private static final String COL_USERNAME = "username";
    private static final String COL_USER_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create students table
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_STU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STU_NAME + " TEXT, " +
                COL_STU_GRADE + " INTEGER)";
        db.execSQL(createStudentsTable);

        // Create parents table
        String createParentsTable = "CREATE TABLE " + TABLE_PARENTS + " (" +
                COL_PARENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PARENT_EMAIL + " TEXT UNIQUE, " +
                COL_PARENT_PASSWORD + " TEXT)";
        db.execSQL(createParentsTable);

        // Optional users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_USER_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    // STUDENT METHODS

    public boolean addStudent(String name, int grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STU_NAME, name);
        cv.put(COL_STU_GRADE, grade);
        long result = db.insert(TABLE_STUDENTS, null, cv);
        return result != -1;
    }

    public Cursor getAllStudentsSorted() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENTS,
                null,
                null,
                null,
                null,
                null,
                COL_STU_GRADE + " DESC");
    }

    // -------------------------------
    // PARENT METHODS
    // -------------------------------

    public boolean addParent(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PARENT_EMAIL, email);
        cv.put(COL_PARENT_PASSWORD, password);
        long result = db.insert(TABLE_PARENTS, null, cv);
        return result != -1;
    }

    public boolean validateParent(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PARENTS,
                new String[]{COL_PARENT_EMAIL},
                COL_PARENT_EMAIL + "=? AND " + COL_PARENT_PASSWORD + "=?",
                new String[]{email, password},
                null,
                null,
                null
        );
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // -------------------------------
    // USER METHODS (optional for login)
    // -------------------------------

    public boolean registerUser(String username, String password) {
        if (checkUserExists(username)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    private boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USERNAME},
                COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USERNAME},
                COL_USERNAME + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }
}
