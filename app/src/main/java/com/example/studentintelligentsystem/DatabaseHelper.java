package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student_system.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    // Tables
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String TABLE_RESULTS = "results";
    private static final String TABLE_PARENTS = "parents";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ANNOUNCEMENTS = "announcements"; // New table

    // Students columns
    private static final String COL_STU_ID = "id";
    private static final String COL_STU_NAME = "stu_name";
    private static final String COL_STU_GRADE = "stu_grade";
    private static final String COL_STU_AGE = "stu_age";
    private static final String COL_STU_GENDER = "stu_gender";
    private static final String COL_STU_PARENT_NAME = "stu_parent_name";
    private static final String COL_STU_PARENT_EMAIL = "stu_parent_email";
    private static final String COL_STU_PARENT_PHONE = "stu_parent_phone";
    private static final String COL_STU_ADDRESS = "stu_address";

    // Attendance columns
    private static final String COL_ATT_ID = "id";
    private static final String COL_ATT_STU_ID = "student_id";
    private static final String COL_ATT_DATE = "date"; // TEXT yyyy-MM-dd
    private static final String COL_ATT_PRESENT = "present"; // 0/1

    // Results columns
    private static final String COL_RES_ID = "id";
    private static final String COL_RES_STU_ID = "student_id";
    private static final String COL_RES_SUBJECT = "subject";
    private static final String COL_RES_SCORE = "score";

    // Parents columns
    private static final String COL_PARENT_ID = "id";
    private static final String COL_PARENT_EMAIL = "email";
    private static final String COL_PARENT_PASSWORD = "password";

    // Users columns (optional)
    private static final String COL_USERNAME = "username";
    private static final String COL_USER_PASSWORD = "password";

    // Announcements columns
    private static final String COL_ANN_ID = "id";
    private static final String COL_ANN_TITLE = "title";
    private static final String COL_ANN_BODY = "body";
    private static final String COL_ANN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Students table
        String createStudents = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_STU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STU_NAME + " TEXT, " +
                COL_STU_GRADE + " INTEGER, " +
                COL_STU_AGE + " INTEGER, " +
                COL_STU_GENDER + " TEXT, " +
                COL_STU_PARENT_NAME + " TEXT, " +
                COL_STU_PARENT_EMAIL + " TEXT, " +
                COL_STU_PARENT_PHONE + " TEXT, " +
                COL_STU_ADDRESS + " TEXT)";
        db.execSQL(createStudents);

        // Attendance table
        String createAttendance = "CREATE TABLE " + TABLE_ATTENDANCE + " (" +
                COL_ATT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ATT_STU_ID + " INTEGER, " +
                COL_ATT_DATE + " TEXT, " +
                COL_ATT_PRESENT + " INTEGER, " +
                "FOREIGN KEY(" + COL_ATT_STU_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COL_STU_ID + "))";
        db.execSQL(createAttendance);

        // Results table
        String createResults = "CREATE TABLE " + TABLE_RESULTS + " (" +
                COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RES_STU_ID + " INTEGER, " +
                COL_RES_SUBJECT + " TEXT, " +
                COL_RES_SCORE + " INTEGER, " +
                "FOREIGN KEY(" + COL_RES_STU_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COL_STU_ID + "))";
        db.execSQL(createResults);

        // Parents table
        String createParents = "CREATE TABLE " + TABLE_PARENTS + " (" +
                COL_PARENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PARENT_EMAIL + " TEXT UNIQUE, " +
                COL_PARENT_PASSWORD + " TEXT)";
        db.execSQL(createParents);

        // Users table (optional)
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_USER_PASSWORD + " TEXT)";
        db.execSQL(createUsers);

        // Announcements table
        String createAnnouncements = "CREATE TABLE " + TABLE_ANNOUNCEMENTS + " (" +
                COL_ANN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ANN_TITLE + " TEXT, " +
                COL_ANN_BODY + " TEXT, " +
                COL_ANN_DATE + " TEXT)";
        db.execSQL(createAnnouncements);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNOUNCEMENTS);
            onCreate(db);
        }
    }

    // ... existing methods

    // ------------------------------
    // ANNOUNCEMENT METHODS
    // ------------------------------

    public boolean addAnnouncement(String title, String body, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ANN_TITLE, title);
        cv.put(COL_ANN_BODY, body);
        cv.put(COL_ANN_DATE, date);
        long result = db.insert(TABLE_ANNOUNCEMENTS, null, cv);
        return result != -1;
    }

    public Cursor getAllAnnouncements() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ANNOUNCEMENTS, null, null, null, null, null, COL_ANN_DATE + " DESC");
    }
    
    // ... rest of the existing methods ...

    // STUDENT METHODS

    // Add student and return ID
    public long addStudentAndGetId(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STU_NAME, student.getName());
        cv.put(COL_STU_GRADE, student.getGrade());
        cv.put(COL_STU_AGE, student.getAge());
        cv.put(COL_STU_GENDER, student.getGender());
        cv.put(COL_STU_PARENT_NAME, student.getParentName());
        cv.put(COL_STU_PARENT_EMAIL, student.getParentEmail());
        cv.put(COL_STU_PARENT_PHONE, student.getParentPhone());
        cv.put(COL_STU_ADDRESS, student.getAddress());
        long newId = db.insert(TABLE_STUDENTS, null, cv);
        db.close();
        return newId;
    }

    public Cursor getStudentByParentEmail(String parentEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENTS, null, COL_STU_PARENT_EMAIL + "=?", new String[]{parentEmail}, null, null, null);
    }

    public boolean updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STU_NAME, student.getName());
        cv.put(COL_STU_GRADE, student.getGrade());
        cv.put(COL_STU_AGE, student.getAge());
        cv.put(COL_STU_GENDER, student.getGender());
        cv.put(COL_STU_PARENT_NAME, student.getParentName());
        cv.put(COL_STU_PARENT_EMAIL, student.getParentEmail());
        cv.put(COL_STU_PARENT_PHONE, student.getParentPhone());
        cv.put(COL_STU_ADDRESS, student.getAddress());
        int rows = db.update(TABLE_STUDENTS, cv, COL_STU_ID + "=?", new String[]{String.valueOf(student.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_STUDENTS, COL_STU_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENTS, null, null, null, null, null, COL_STU_NAME + " ASC");
    }

    // ------------------------------
    // ATTENDANCE METHODS
    // ------------------------------

    public boolean addAttendance(int studentId, String dateIso, boolean present) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ATT_STU_ID, studentId);
        cv.put(COL_ATT_DATE, dateIso);
        cv.put(COL_ATT_PRESENT, present ? 1 : 0);
        long res = db.insert(TABLE_ATTENDANCE, null, cv);
        db.close();
        return res != -1;
    }

    public Cursor getAttendanceForStudent(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ATTENDANCE, null, COL_ATT_STU_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, COL_ATT_DATE + " DESC");
    }

    public Cursor getAttendanceByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s." + COL_STU_NAME + ", a." + COL_ATT_PRESENT + " FROM " + TABLE_ATTENDANCE + " a JOIN " + TABLE_STUDENTS + " s ON a." + COL_ATT_STU_ID + " = s." + COL_STU_ID + " WHERE a." + COL_ATT_DATE + " = ?";
        return db.rawQuery(query, new String[]{date});
    }

    public Cursor getUniqueAttendanceDates() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_ATTENDANCE, new String[]{COL_ATT_DATE}, null, null, null, null, COL_ATT_DATE + " DESC", null);
    }

    // ------------------------------
    // RESULTS METHODS
    // ------------------------------

    public boolean addOrUpdateResult(int studentId, String subject, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RES_SCORE, score);
        int rows = db.update(TABLE_RESULTS, cv, COL_RES_STU_ID + "=? AND " + COL_RES_SUBJECT + "=?",
                new String[]{String.valueOf(studentId), subject});
        if (rows > 0) {
            db.close();
            return true;
        }
        ContentValues insert = new ContentValues();
        insert.put(COL_RES_STU_ID, studentId);
        insert.put(COL_RES_SUBJECT, subject);
        insert.put(COL_RES_SCORE, score);
        long res = db.insert(TABLE_RESULTS, null, insert);
        db.close();
        return res != -1;
    }

    public Cursor getResultsForStudent(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r." + COL_RES_ID + " as _id, r." + COL_RES_STU_ID + ", s." + COL_STU_NAME + ", r." + COL_RES_SUBJECT + ", r." + COL_RES_SCORE +
                " FROM " + TABLE_RESULTS + " r LEFT JOIN " + TABLE_STUDENTS + " s ON r." + COL_RES_STU_ID + " = s." + COL_STU_ID +
                " WHERE r." + COL_RES_STU_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(studentId)});
    }

    public Cursor getAllResults() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r." + COL_RES_ID + " as _id, r." + COL_RES_STU_ID + ", s." + COL_STU_NAME + ", r." + COL_RES_SUBJECT + ", r." + COL_RES_SCORE +
                " FROM " + TABLE_RESULTS + " r LEFT JOIN " + TABLE_STUDENTS + " s ON r." + COL_RES_STU_ID + " = s." + COL_STU_ID +
                " ORDER BY r." + COL_RES_ID + " DESC";
        return db.rawQuery(query, null);
    }

    // ------------------------------
    // PARENT METHODS
    // ------------------------------

    public boolean addParent(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PARENT_EMAIL, email);
        cv.put(COL_PARENT_PASSWORD, password);
        long result = db.insert(TABLE_PARENTS, null, cv);
        db.close();
        return result != -1;
    }

    public boolean validateParent(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PARENTS, new String[]{COL_PARENT_EMAIL},
                COL_PARENT_EMAIL + "=? AND " + COL_PARENT_PASSWORD + "=?", new String[]{email, password},
                null, null, null);
        boolean valid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return valid;
    }

    // ------------------------------
    // USER METHODS (optional)
    // ------------------------------

    public boolean registerUser(String username, String password) {
        if (checkUserExists(username)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;
    }

    private boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME},
                COL_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME},
                COL_USERNAME + "=? AND " + COL_USER_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        boolean valid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return valid;
    }
}
