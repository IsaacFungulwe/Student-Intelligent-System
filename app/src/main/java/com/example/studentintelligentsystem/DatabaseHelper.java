package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.studentintelligentsystem.supabase.SupabaseSyncManager;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private Context context;
    private SupabaseSyncManager syncManager;

    private static final String DATABASE_NAME = "StudentIntelligentSystem.db";
    private static final int DATABASE_VERSION = 6;

    // Tables
    public static final String TABLE_ADMIN = "Admin";
    public static final String TABLE_TEACHER = "Teacher";
    public static final String TABLE_PARENT = "Parent";
    public static final String TABLE_STUDENT = "Student";
    public static final String TABLE_ATTENDANCE = "Attendance";
    public static final String TABLE_RESULTS = "Results";
    public static final String TABLE_ANNOUNCEMENT = "Announcement";
    public static final String TABLE_SUBJECTS = "Subjects";

    // Columns
    public static final String ADMIN_ID = "adminId";
    public static final String ADMIN_SCHOOL_NAME = "schoolName";
    public static final String ADMIN_DISTRICT = "district";
    public static final String ADMIN_EMAIL = "schoolEmail";
    public static final String ADMIN_PASSWORD = "password";

    public static final String TEACHER_ID = "teacherId";
    public static final String TEACHER_NAME = "name";
    public static final String TEACHER_EMAIL = "email";
    public static final String TEACHER_PASSWORD = "password";
    public static final String TEACHER_GRADE_ASSIGNED = "gradeAssigned";
    public static final String TEACHER_FK_ADMIN_ID = "linkedSchoolAdminId";

    public static final String PARENT_ID = "parentId";
    public static final String PARENT_NAME = "parentName";
    public static final String PARENT_EMAIL = "parentEmail";
    public static final String PARENT_PHONE = "parentPhone";
    public static final String PARENT_PASSWORD = "password";
    public static final String PARENT_FK_ADMIN_ID = "linkedSchoolAdminId";

    public static final String STUDENT_ID = "studentId";
    public static final String STUDENT_NAME = "studentName";
    public static final String STUDENT_AGE = "age";
    public static final String STUDENT_GENDER = "gender";
    public static final String STUDENT_GRADE = "grade";
    public static final String STUDENT_ADDRESS = "address";
    public static final String STUDENT_FK_PARENT_ID = "linkedParentId";
    public static final String STUDENT_FK_TEACHER_ID = "linkedTeacherId";

    public static final String ATTENDANCE_ID = "attendanceId";
    public static final String ATTENDANCE_FK_STUDENT_ID = "studentId";
    public static final String ATTENDANCE_DATE = "date";
    public static final String ATTENDANCE_STATUS = "status";
    public static final String ATTENDANCE_FK_TEACHER_ID = "markedByTeacherId";

    public static final String RESULT_ID = "resultId";
    public static final String RESULT_FK_STUDENT_ID = "studentId";
    public static final String RESULT_SUBJECT = "subject";
    public static final String RESULT_TERM = "term";
    public static final String RESULT_MARKS = "marks";
    public static final String RESULT_COMMENT = "comment";
    public static final String RESULT_FK_TEACHER_ID = "recordedByTeacherId";

    public static final String ANNOUNCEMENT_ID = "announcementId";
    public static final String ANNOUNCEMENT_TITLE = "title";
    public static final String ANNOUNCEMENT_MESSAGE = "message";
    public static final String ANNOUNCEMENT_ROLE = "createdByRole";
    public static final String ANNOUNCEMENT_FK_CREATOR_ID = "createdById";
    public static final String ANNOUNCEMENT_GRADE_TARGET = "gradeTarget";
    public static final String ANNOUNCEMENT_SOURCE_LABEL = "sourceLabel";
    public static final String ANNOUNCEMENT_TIMESTAMP = "timestamp";

    public static final String SUBJECT_ID = "subjectId";
    public static final String SUBJECT_NAME = "subjectName";
    public static final String SUBJECT_GRADE = "grade";
    public static final String SUBJECT_FK_TEACHER_ID = "createdByTeacherId";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        // Initialize sync manager if Supabase is configured
        if (SupabaseConfig.isConfigured()) {
            try {
                this.syncManager = SupabaseSyncManager.getInstance(context);
                Log.d(TAG, "✓ Supabase sync manager initialized");
            } catch (Exception e) {
                Log.w(TAG, "Supabase sync manager not available: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Supabase not configured - sync disabled");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ADMIN + " (" + ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ADMIN_SCHOOL_NAME + " TEXT NOT NULL, " + ADMIN_DISTRICT + " TEXT NOT NULL, " + ADMIN_EMAIL + " TEXT NOT NULL UNIQUE, " + ADMIN_PASSWORD + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_TEACHER + " (" + TEACHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TEACHER_NAME + " TEXT NOT NULL, " + TEACHER_EMAIL + " TEXT NOT NULL UNIQUE, " + TEACHER_PASSWORD + " TEXT NOT NULL, " + TEACHER_GRADE_ASSIGNED + " INTEGER NOT NULL, " + TEACHER_FK_ADMIN_ID + " INTEGER NOT NULL, FOREIGN KEY(" + TEACHER_FK_ADMIN_ID + ") REFERENCES " + TABLE_ADMIN + "(" + ADMIN_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_PARENT + " (" + PARENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PARENT_NAME + " TEXT NOT NULL, " + PARENT_EMAIL + " TEXT NOT NULL UNIQUE, " + PARENT_PHONE + " TEXT, " + PARENT_PASSWORD + " TEXT NOT NULL, " + PARENT_FK_ADMIN_ID + " INTEGER NOT NULL, FOREIGN KEY(" + PARENT_FK_ADMIN_ID + ") REFERENCES " + TABLE_ADMIN + "(" + ADMIN_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_STUDENT + " (" + STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + STUDENT_NAME + " TEXT NOT NULL, " + STUDENT_AGE + " INTEGER, " + STUDENT_GENDER + " TEXT, " + STUDENT_GRADE + " INTEGER NOT NULL, " + STUDENT_ADDRESS + " TEXT, " + STUDENT_FK_PARENT_ID + " INTEGER NOT NULL, " + STUDENT_FK_TEACHER_ID + " INTEGER NOT NULL, FOREIGN KEY(" + STUDENT_FK_PARENT_ID + ") REFERENCES " + TABLE_PARENT + "(" + PARENT_ID + "), FOREIGN KEY(" + STUDENT_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_ATTENDANCE + " (" + ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ATTENDANCE_FK_STUDENT_ID + " INTEGER NOT NULL, " + ATTENDANCE_DATE + " TEXT NOT NULL, " + ATTENDANCE_STATUS + " TEXT NOT NULL, " + ATTENDANCE_FK_TEACHER_ID + " INTEGER NOT NULL, FOREIGN KEY(" + ATTENDANCE_FK_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + STUDENT_ID + "), FOREIGN KEY(" + ATTENDANCE_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_RESULTS + " (" + RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RESULT_FK_STUDENT_ID + " INTEGER NOT NULL, " + RESULT_SUBJECT + " TEXT NOT NULL, " + RESULT_TERM + " TEXT NOT NULL, " + RESULT_MARKS + " INTEGER NOT NULL, " + RESULT_COMMENT + " TEXT, " + RESULT_FK_TEACHER_ID + " INTEGER NOT NULL, FOREIGN KEY(" + RESULT_FK_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + STUDENT_ID + "), FOREIGN KEY(" + RESULT_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_ANNOUNCEMENT + " (" + ANNOUNCEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ANNOUNCEMENT_TITLE + " TEXT NOT NULL, " + ANNOUNCEMENT_MESSAGE + " TEXT NOT NULL, " + ANNOUNCEMENT_ROLE + " TEXT NOT NULL, " + ANNOUNCEMENT_FK_CREATOR_ID + " INTEGER NOT NULL, " + ANNOUNCEMENT_GRADE_TARGET + " INTEGER, " + ANNOUNCEMENT_SOURCE_LABEL + " TEXT, " + ANNOUNCEMENT_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE " + TABLE_SUBJECTS + " (" + SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SUBJECT_NAME + " TEXT NOT NULL, " + SUBJECT_GRADE + " INTEGER NOT NULL, " + SUBJECT_FK_TEACHER_ID + " INTEGER NOT NULL, FOREIGN KEY(" + SUBJECT_FK_TEACHER_ID + ") REFERENCES " + TABLE_TEACHER + "(" + TEACHER_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNOUNCEMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        onCreate(db);
    }

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
            return String.valueOf(password.hashCode());
        }
    }

    public Cursor checkLogin(String email, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName, emailColumn, passwordColumn;
        switch (role) {
            case "Admin": tableName = TABLE_ADMIN; emailColumn = ADMIN_EMAIL; passwordColumn = ADMIN_PASSWORD; break;
            case "Teacher": tableName = TABLE_TEACHER; emailColumn = TEACHER_EMAIL; passwordColumn = TEACHER_PASSWORD; break;
            case "Parent": tableName = TABLE_PARENT; emailColumn = PARENT_EMAIL; passwordColumn = PARENT_PASSWORD; break;
            default: return null;
        }
        String hashedPassword = hashPassword(password);
        return db.query(tableName, null, emailColumn + " = ? AND " + passwordColumn + " = ?", new String[]{email, hashedPassword}, null, null, null);
    }

    public long addSubject(String subjectName, int grade, int teacherId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SUBJECT_NAME, subjectName);
        values.put(SUBJECT_GRADE, grade);
        values.put(SUBJECT_FK_TEACHER_ID, teacherId);
        long subjectId = db.insert(TABLE_SUBJECTS, null, values);
        db.close();

        // Sync to Supabase
        if (subjectId > 0 && syncManager != null) {
            Log.d(TAG, "Syncing subject record " + subjectId + " to Supabase...");
            syncManager.syncSubject((int) subjectId);
        }

        return subjectId;
    }

    public List<String> getSubjectsByGrade(int grade) {
        List<String> subjects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{SUBJECT_NAME}, SUBJECT_GRADE + " = ?", new String[]{String.valueOf(grade)}, null, null, SUBJECT_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                subjects.add(cursor.getString(cursor.getColumnIndexOrThrow(SUBJECT_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return subjects;
    }

    public void deleteSubject(String subjectName, int grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBJECTS, SUBJECT_NAME + " = ? AND " + SUBJECT_GRADE + " = ?", new String[]{subjectName, String.valueOf(grade)});
        db.close();
    }

    public List<String> getUniqueAttendanceDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_ATTENDANCE, new String[]{ATTENDANCE_DATE}, null, null, null, null, ATTENDANCE_DATE + " DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(cursor.getColumnIndexOrThrow(ATTENDANCE_DATE)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return dates;
    }

    public Cursor getAttendanceByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s." + STUDENT_NAME + ", a." + ATTENDANCE_STATUS + " FROM " + TABLE_STUDENT + " s LEFT JOIN " + TABLE_ATTENDANCE + " a ON s." + STUDENT_ID + " = a." + ATTENDANCE_FK_STUDENT_ID + " AND a." + ATTENDANCE_DATE + " = ?";
        return db.rawQuery(query, new String[]{date});
    }

    public void addAttendance(int studentId, String date, boolean isPresent, int teacherId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ATTENDANCE_FK_STUDENT_ID, studentId);
        values.put(ATTENDANCE_DATE, date);
        values.put(ATTENDANCE_STATUS, isPresent ? "Present" : "Absent");
        values.put(ATTENDANCE_FK_TEACHER_ID, teacherId);  // Add teacher ID
        long attendanceId = db.insert(TABLE_ATTENDANCE, null, values);
        db.close();

        // Sync to Supabase
        if (attendanceId > 0 && syncManager != null) {
            Log.d(TAG, "Syncing attendance record " + attendanceId + " to Supabase...");
            syncManager.syncAttendance((int) attendanceId);
        }
    }

    public Cursor getStudentsByParentId(int parentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT, null, STUDENT_FK_PARENT_ID + "=?", new String[]{String.valueOf(parentId)}, null, null, null);
    }

    public Cursor getStudentById(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT, null, STUDENT_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, null);
    }

    // Get all results for a specific student
    public Cursor getResultsForStudent(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RESULTS, null, RESULT_FK_STUDENT_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, RESULT_TERM + " DESC");
    }

    // Calculate attendance percentage for a student
    public double getAttendancePercentage(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ATTENDANCE, new String[]{ATTENDANCE_STATUS}, ATTENDANCE_FK_STUDENT_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, null);

        int totalDays = 0;
        int presentDays = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalDays++;
                String status = cursor.getString(cursor.getColumnIndexOrThrow(ATTENDANCE_STATUS));
                if ("Present".equalsIgnoreCase(status)) {
                    presentDays++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;
    }

    public Cursor getAttendanceForStudent(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ATTENDANCE, null, ATTENDANCE_FK_STUDENT_ID + "=?", new String[]{String.valueOf(studentId)}, null, null, ATTENDANCE_DATE + " DESC");
    }

    // Update a result record
    public boolean updateResult(int resultId, String subject, String term, int marks, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RESULT_SUBJECT, subject);
        values.put(RESULT_TERM, term);
        values.put(RESULT_MARKS, marks);
        values.put(RESULT_COMMENT, comment);

        int rowsAffected = db.update(TABLE_RESULTS, values, RESULT_ID + " = ?",
                new String[]{String.valueOf(resultId)});
        db.close();
        return rowsAffected > 0;
    }

    // Delete a result record
    public boolean deleteResult(int resultId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_RESULTS, RESULT_ID + " = ?",
                new String[]{String.valueOf(resultId)});
        db.close();
        return rowsDeleted > 0;
    }

    /**
     * Insert or update parent from Supabase data into local database
     * @param parentData JSON object containing parent data from Supabase
     * @return parent ID if successful, -1 otherwise
     */
    public long insertOrUpdateParentFromSupabase(org.json.JSONObject parentData) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            int parentId = parentData.getInt("id");
            String name = parentData.getString("name");
            String email = parentData.getString("email");
            String phone = parentData.optString("phone", "");
            String password = parentData.getString("password_hash");
            int adminId = parentData.getInt("admin_id");
            
            values.put(PARENT_ID, parentId);
            values.put(PARENT_NAME, name);
            values.put(PARENT_EMAIL, email);
            values.put(PARENT_PHONE, phone);
            values.put(PARENT_PASSWORD, password);
            values.put(PARENT_FK_ADMIN_ID, adminId);
            
            // Check if parent already exists locally
            Cursor cursor = db.query(TABLE_PARENT, new String[]{PARENT_ID}, 
                PARENT_ID + "=?", new String[]{String.valueOf(parentId)}, 
                null, null, null);
            
            long result;
            if (cursor != null && cursor.moveToFirst()) {
                // Update existing parent
                int rowsAffected = db.update(TABLE_PARENT, values, 
                    PARENT_ID + "=?", new String[]{String.valueOf(parentId)});
                result = rowsAffected > 0 ? parentId : -1;
                cursor.close();
                Log.d(TAG, "✓ Updated parent from Supabase: " + email);
            } else {
                // Insert new parent
                result = db.insertWithOnConflict(TABLE_PARENT, null, values, 
                    SQLiteDatabase.CONFLICT_REPLACE);
                if (cursor != null) cursor.close();
                Log.d(TAG, "✓ Inserted parent from Supabase: " + email);
            }
            
            db.close();
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting/updating parent from Supabase: " + e.getMessage());
            return -1;
        }
    }
}
