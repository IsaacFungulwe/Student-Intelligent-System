package com.example.studentintelligentsystem.supabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.studentintelligentsystem.DatabaseHelper;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sync Manager for synchronizing local SQLite data with Supabase
 */
public class SupabaseSyncManager {
    private static final String TAG = "SupabaseSyncManager";
    private static SupabaseSyncManager instance;
    // removed persistent DatabaseHelper to avoid circular init
    private final Context appContext;
    private SupabaseClient supabaseClient;
    private ExecutorService executorService;
    private boolean syncEnabled = true;

    private SupabaseSyncManager(Context context) {
        // keep only the application context reference
        this.appContext = context.getApplicationContext();
        this.supabaseClient = SupabaseClient.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized SupabaseSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseSyncManager(context);
        }
        return instance;
    }

    /**
     * Enable/disable sync
     */
    public void setSyncEnabled(boolean enabled) {
        this.syncEnabled = enabled;
        Log.i(TAG, "Sync " + (enabled ? "enabled" : "disabled"));
    }

    // helper to safely open a readable DB and close it
    private SQLiteDatabase openReadableDb(DatabaseHelper helper) {
        return helper.getReadableDatabase();
    }

    /**
     * Sync a student record to Supabase
     */
    public void syncStudent(int studentId) {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            Log.d(TAG, "Sync disabled or not configured");
            return;
        }

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            Cursor cursor = null;
            SQLiteDatabase db = null;
            try {
                localDbHelper = new DatabaseHelper(appContext);
                db = localDbHelper.getReadableDatabase();
                cursor = db.query(
                    DatabaseHelper.TABLE_STUDENT,
                    null,
                    DatabaseHelper.STUDENT_ID + "=?",
                    new String[]{String.valueOf(studentId)},
                    null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    JSONObject studentData = new JSONObject();
                    studentData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID)));
                    studentData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME)));
                    studentData.put("age", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_AGE)));
                    studentData.put("gender", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GENDER)));
                    studentData.put("grade", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE)));
                    studentData.put("address", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ADDRESS)));
                    studentData.put("parent_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_PARENT_ID)));
                    studentData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_TEACHER_ID)));

                    boolean success = supabaseClient.insertData("students", studentData);
                    if (success) {
                        Log.i(TAG, "✓ Student " + studentId + " synced to Supabase");
                    } else {
                        Log.w(TAG, "✗ Failed to sync student " + studentId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing student: " + e.getMessage(), e);
            } finally {
                if (cursor != null) cursor.close();
                if (db != null && db.isOpen()) db.close();
                // DatabaseHelper doesn't need explicit close, but clear ref
                localDbHelper = null;
            }
        });
    }

    /**
     * Sync an attendance record to Supabase
     */
    public void syncAttendance(int attendanceId) {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            return;
        }

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            Cursor cursor = null;
            SQLiteDatabase db = null;
            try {
                localDbHelper = new DatabaseHelper(appContext);
                db = localDbHelper.getReadableDatabase();
                cursor = db.query(
                    DatabaseHelper.TABLE_ATTENDANCE,
                    null,
                    DatabaseHelper.ATTENDANCE_ID + "=?",
                    new String[]{String.valueOf(attendanceId)},
                    null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_FK_STUDENT_ID));

                    // IMPORTANT: First ensure the student exists in Supabase
                    Log.d(TAG, "Ensuring student " + studentId + " exists in Supabase before syncing attendance...");
                    syncStudentSync(studentId, localDbHelper);

                    // Small delay to ensure student is created
                    Thread.sleep(1000);

                    JSONObject attendanceData = new JSONObject();
                    attendanceData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_ID)));
                    attendanceData.put("student_id", studentId);
                    attendanceData.put("date", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_DATE)));
                    attendanceData.put("status", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_STATUS)));
                    attendanceData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_FK_TEACHER_ID)));

                    boolean success = supabaseClient.insertData("attendance", attendanceData);
                    if (success) {
                        Log.i(TAG, "✓ Attendance " + attendanceId + " synced to Supabase");
                    } else {
                        Log.w(TAG, "✗ Failed to sync attendance " + attendanceId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing attendance: " + e.getMessage(), e);
            } finally {
                if (cursor != null) cursor.close();
                if (db != null && db.isOpen()) db.close();
                localDbHelper = null;
            }
        });
    }

    /**
     * Sync a result record to Supabase
     */
    public void syncResult(int resultId) {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            return;
        }

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            Cursor cursor = null;
            SQLiteDatabase db = null;
            try {
                localDbHelper = new DatabaseHelper(appContext);
                db = localDbHelper.getReadableDatabase();
                cursor = db.query(
                    DatabaseHelper.TABLE_RESULTS,
                    null,
                    DatabaseHelper.RESULT_ID + "=?",
                    new String[]{String.valueOf(resultId)},
                    null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_FK_STUDENT_ID));

                    // IMPORTANT: First ensure the student exists in Supabase
                    // This prevents foreign key constraint violations
                    Log.d(TAG, "Ensuring student " + studentId + " exists in Supabase before syncing result...");
                    syncStudentSync(studentId, localDbHelper);  // Sync student first (synchronously)

                    // Small delay to ensure student is created in Supabase
                    Thread.sleep(1000);

                    JSONObject resultData = new JSONObject();
                    resultData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_ID)));
                    resultData.put("student_id", studentId);
                    resultData.put("subject_name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT)));
                    resultData.put("term", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM)));
                    resultData.put("marks", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS)));
                    resultData.put("comment", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT)));
                    resultData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_FK_TEACHER_ID)));

                    boolean success = supabaseClient.insertData("results", resultData);
                    if (success) {
                        Log.i(TAG, "✓ Result " + resultId + " synced to Supabase");
                    } else {
                        Log.w(TAG, "✗ Failed to sync result " + resultId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing result: " + e.getMessage(), e);
            } finally {
                if (cursor != null) cursor.close();
                if (db != null && db.isOpen()) db.close();
                localDbHelper = null;
            }
        });
    }

    /**
     * Synchronously sync a student (used internally to ensure dependencies exist)
     */
    private void syncStudentSync(int studentId, DatabaseHelper dbHelper) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                DatabaseHelper.TABLE_STUDENT,
                null,
                DatabaseHelper.STUDENT_ID + "=?",
                new String[]{String.valueOf(studentId)},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int parentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_PARENT_ID));
                int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_TEACHER_ID));

                // First sync parent and teacher (dependencies)
                syncParentSync(parentId, dbHelper);
                syncTeacherSync(teacherId, dbHelper);

                JSONObject studentData = new JSONObject();
                studentData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID)));
                studentData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME)));
                studentData.put("age", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_AGE)));
                studentData.put("gender", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GENDER)));
                studentData.put("grade", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE)));
                studentData.put("address", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ADDRESS)));
                studentData.put("parent_id", parentId);
                studentData.put("teacher_id", teacherId);

                supabaseClient.insertData("students", studentData);
                Log.d(TAG, "✓ Student " + studentId + " synced (dependency)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing student dependency: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Synchronously sync a parent (used internally to ensure dependencies exist)
     */
    private void syncParentSync(int parentId, DatabaseHelper dbHelper) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                DatabaseHelper.TABLE_PARENT,
                null,
                DatabaseHelper.PARENT_ID + "=?",
                new String[]{String.valueOf(parentId)},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int adminId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_FK_ADMIN_ID));

                // First sync admin (dependency)
                syncAdminSync(adminId, dbHelper);

                JSONObject parentData = new JSONObject();
                parentData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_ID)));
                parentData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_NAME)));
                parentData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_EMAIL)));
                parentData.put("phone", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PHONE)));
                parentData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PASSWORD)));
                parentData.put("admin_id", adminId);

                supabaseClient.insertData("parents", parentData);
                Log.d(TAG, "✓ Parent " + parentId + " synced (dependency)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing parent dependency: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Sync a subject record to Supabase
     */
    public void syncSubject(int subjectId) {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            Log.d(TAG, "Sync disabled or not configured for subject");
            return;
        }

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            Cursor cursor = null;
            SQLiteDatabase db = null;
            try {
                localDbHelper = new DatabaseHelper(appContext);
                db = localDbHelper.getReadableDatabase();
                cursor = db.query(
                    DatabaseHelper.TABLE_SUBJECTS,
                    null,
                    DatabaseHelper.SUBJECT_ID + "=?",
                    new String[]{String.valueOf(subjectId)},
                    null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_FK_TEACHER_ID));

                    // IMPORTANT: First ensure the teacher exists in Supabase
                    Log.d(TAG, "Ensuring teacher " + teacherId + " exists in Supabase before syncing subject...");
                    syncTeacherSync(teacherId, localDbHelper);

                    // Small delay to ensure teacher is created
                    Thread.sleep(1000);

                    JSONObject subjectData = new JSONObject();
                    subjectData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_ID)));
                    subjectData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_NAME)));
                    subjectData.put("grade", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_GRADE)));
                    subjectData.put("teacher_id", teacherId);

                    boolean success = supabaseClient.insertData("subjects", subjectData);
                    if (success) {
                        Log.i(TAG, "✓ Subject " + subjectId + " synced to Supabase");
                    } else {
                        Log.w(TAG, "✗ Failed to sync subject " + subjectId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing subject: " + e.getMessage(), e);
            } finally {
                if (cursor != null) cursor.close();
                if (db != null && db.isOpen()) db.close();
                localDbHelper = null;
            }
        });
    }

    /**
     * Synchronously sync a teacher (used internally to ensure dependencies exist)
     */
    private void syncTeacherSync(int teacherId, DatabaseHelper dbHelper) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                DatabaseHelper.TABLE_TEACHER,
                null,
                DatabaseHelper.TEACHER_ID + "=?",
                new String[]{String.valueOf(teacherId)},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int adminId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_FK_ADMIN_ID));

                // First sync admin (dependency)
                syncAdminSync(adminId, dbHelper);

                JSONObject teacherData = new JSONObject();
                teacherData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_ID)));
                teacherData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_NAME)));
                teacherData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_EMAIL)));
                teacherData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_PASSWORD)));
                teacherData.put("grade_assigned", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED)));
                teacherData.put("admin_id", adminId);

                supabaseClient.insertData("teachers", teacherData);
                Log.d(TAG, "✓ Teacher " + teacherId + " synced (dependency)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing teacher dependency: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Synchronously sync an admin (used internally to ensure dependencies exist)
     */
    private void syncAdminSync(int adminId, DatabaseHelper dbHelper) {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                DatabaseHelper.TABLE_ADMIN,
                null,
                DatabaseHelper.ADMIN_ID + "=?",
                new String[]{String.valueOf(adminId)},
                null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                JSONObject adminData = new JSONObject();
                adminData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_ID)));
                adminData.put("school_name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_SCHOOL_NAME)));
                adminData.put("district", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_DISTRICT)));
                adminData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_EMAIL)));
                adminData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_PASSWORD)));

                supabaseClient.insertData("admins", adminData);
                Log.d(TAG, "✓ Admin " + adminId + " synced (dependency)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing admin dependency: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Sync an announcement to Supabase
     */
    public void syncAnnouncement(int announcementId) {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            return;
        }

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            Cursor cursor = null;
            SQLiteDatabase db = null;
            try {
                localDbHelper = new DatabaseHelper(appContext);
                db = localDbHelper.getReadableDatabase();
                cursor = db.query(
                    DatabaseHelper.TABLE_ANNOUNCEMENT,
                    null,
                    DatabaseHelper.ANNOUNCEMENT_ID + "=?",
                    new String[]{String.valueOf(announcementId)},
                    null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    JSONObject announcementData = new JSONObject();
                    announcementData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_ID)));
                    announcementData.put("title", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE)));
                    announcementData.put("message", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE)));
                    announcementData.put("created_by_role", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_ROLE)));
                    announcementData.put("created_by_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_FK_CREATOR_ID)));
                    announcementData.put("grade_target", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET)));

                    boolean success = supabaseClient.insertData("announcements", announcementData);
                    if (success) {
                        Log.i(TAG, "✓ Announcement " + announcementId + " synced to Supabase");
                    } else {
                        Log.w(TAG, "✗ Failed to sync announcement " + announcementId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing announcement: " + e.getMessage(), e);
            } finally {
                if (cursor != null) cursor.close();
                if (db != null && db.isOpen()) db.close();
                localDbHelper = null;
            }
        });
    }

    /**
     * Sync all existing local data to Supabase (one-time migration or periodic sync)
     * Syncs in CORRECT dependency order: Teachers → Parents → Students → Subjects → Attendance → Results → Announcements
     */
    public void syncAllLocalData() {
        if (!syncEnabled || !SupabaseConfig.isConfigured()) {
            Log.w(TAG, "Cannot sync: Sync disabled or not configured");
            return;
        }

        Log.i(TAG, "🔄 Starting full data migration to Supabase...");

        executorService.execute(() -> {
            DatabaseHelper localDbHelper = null;
            int totalSynced = 0;
            int totalFailed = 0;

            try {
                localDbHelper = new DatabaseHelper(appContext);
                SQLiteDatabase db = localDbHelper.getReadableDatabase();

                // STEP 0: Sync all admins first (base dependency)
                Log.i(TAG, "📊 Step 1/8: Syncing admins...");
                int adminsCount = syncAllAdmins(db);
                totalSynced += adminsCount;
                Log.i(TAG, "✓ Admins synced: " + adminsCount);
                Thread.sleep(1500);

                // STEP 1: Sync all teachers (required by students & subjects)
                Log.i(TAG, "📊 Step 2/8: Syncing teachers...");
                int teachersCount = syncAllTeachers(db);
                totalSynced += teachersCount;
                Log.i(TAG, "✓ Teachers synced: " + teachersCount);
                Thread.sleep(1500);

                // STEP 2: Sync all parents (required by students)
                Log.i(TAG, "📊 Step 3/8: Syncing parents...");
                int parentsCount = syncAllParents(db);
                totalSynced += parentsCount;
                Log.i(TAG, "✓ Parents synced: " + parentsCount);
                Thread.sleep(1500);

                // STEP 3: Sync all students (required for attendance & results)
                Log.i(TAG, "📊 Step 4/8: Syncing students...");
                int studentsCount = syncAllStudents(db);
                totalSynced += studentsCount;
                Log.i(TAG, "✓ Students synced: " + studentsCount);
                Thread.sleep(1500);

                // STEP 4: Sync all subjects (needs teachers)
                Log.i(TAG, "📊 Step 5/8: Syncing subjects...");
                int subjectsCount = syncAllSubjects(db);
                totalSynced += subjectsCount;
                Log.i(TAG, "✓ Subjects synced: " + subjectsCount);
                Thread.sleep(1000);

                // STEP 5: Sync all attendance (needs students)
                Log.i(TAG, "📊 Step 6/8: Syncing attendance records...");
                int attendanceCount = syncAllAttendance(db);
                totalSynced += attendanceCount;
                Log.i(TAG, "✓ Attendance records synced: " + attendanceCount);
                Thread.sleep(1000);

                // STEP 6: Sync all results (needs students)
                Log.i(TAG, "📊 Step 7/8: Syncing results...");
                int resultsCount = syncAllResults(db);
                totalSynced += resultsCount;
                Log.i(TAG, "✓ Results synced: " + resultsCount);
                Thread.sleep(1000);

                // STEP 7: Sync all announcements (independent)
                Log.i(TAG, "📊 Step 8/8: Syncing announcements...");
                int announcementsCount = syncAllAnnouncements(db);
                totalSynced += announcementsCount;
                Log.i(TAG, "✓ Announcements synced: " + announcementsCount);

                db.close();

                // Summary
                Log.i(TAG, "════════════════════════════════════════");
                Log.i(TAG, "✅ FULL SYNC COMPLETED!");
                Log.i(TAG, "📦 Total records synced: " + totalSynced);
                Log.i(TAG, "❌ Failed: " + totalFailed);
                Log.i(TAG, "════════════════════════════════════════");

            } catch (Exception e) {
                Log.e(TAG, "❌ Error during full sync: " + e.getMessage(), e);
            } finally {
                if (localDbHelper != null) {
                    localDbHelper = null;
                }
            }
        });
    }

    /**
     * Sync all admins from local DB to Supabase
     */
    private int syncAllAdmins(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ADMIN, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject adminData = new JSONObject();
                        adminData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_ID)));
                        adminData.put("school_name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_SCHOOL_NAME)));
                        adminData.put("district", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_DISTRICT)));
                        adminData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_EMAIL)));
                        adminData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_PASSWORD)));

                        if (supabaseClient.insertData("admins", adminData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync admin: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all teachers from local DB to Supabase
     */
    private int syncAllTeachers(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_TEACHER, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject teacherData = new JSONObject();
                        teacherData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_ID)));
                        teacherData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_NAME)));
                        teacherData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_EMAIL)));
                        teacherData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_PASSWORD)));
                        teacherData.put("grade_assigned", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED)));
                        teacherData.put("admin_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_FK_ADMIN_ID)));

                        if (supabaseClient.insertData("teachers", teacherData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync teacher: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all parents from local DB to Supabase
     */
    private int syncAllParents(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_PARENT, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject parentData = new JSONObject();
                        parentData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_ID)));
                        parentData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_NAME)));
                        parentData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_EMAIL)));
                        parentData.put("phone", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PHONE)));
                        parentData.put("password_hash", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PASSWORD)));
                        parentData.put("admin_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_FK_ADMIN_ID)));

                        if (supabaseClient.insertData("parents", parentData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync parent: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all students from local DB to Supabase
     */
    private int syncAllStudents(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_STUDENT, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject studentData = new JSONObject();
                        studentData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID)));
                        studentData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME)));
                        studentData.put("age", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_AGE)));
                        studentData.put("gender", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GENDER)));
                        studentData.put("grade", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE)));
                        studentData.put("address", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ADDRESS)));
                        studentData.put("parent_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_PARENT_ID)));
                        studentData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_FK_TEACHER_ID)));

                        if (supabaseClient.insertData("students", studentData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync student: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all subjects from local DB to Supabase
     */
    private int syncAllSubjects(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SUBJECTS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject subjectData = new JSONObject();
                        subjectData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_ID)));
                        subjectData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_NAME)));
                        subjectData.put("grade", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_GRADE)));
                        subjectData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_FK_TEACHER_ID)));

                        if (supabaseClient.insertData("subjects", subjectData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync subject: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all attendance records from local DB to Supabase
     */
    private int syncAllAttendance(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ATTENDANCE, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject attendanceData = new JSONObject();
                        attendanceData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_ID)));
                        attendanceData.put("student_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_FK_STUDENT_ID)));
                        attendanceData.put("date", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_DATE)));
                        attendanceData.put("status", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_STATUS)));
                        attendanceData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_FK_TEACHER_ID)));

                        if (supabaseClient.insertData("attendance", attendanceData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync attendance: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all results from local DB to Supabase
     */
    private int syncAllResults(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_RESULTS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject resultData = new JSONObject();
                        resultData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_ID)));
                        resultData.put("student_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_FK_STUDENT_ID)));
                        resultData.put("subject_name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT)));
                        resultData.put("term", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM)));
                        resultData.put("marks", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS)));
                        resultData.put("comment", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT)));
                        resultData.put("teacher_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_FK_TEACHER_ID)));

                        if (supabaseClient.insertData("results", resultData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync result: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all announcements from local DB to Supabase
     */
    private int syncAllAnnouncements(SQLiteDatabase db) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        JSONObject announcementData = new JSONObject();
                        announcementData.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_ID)));
                        announcementData.put("title", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE)));
                        announcementData.put("message", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE)));
                        announcementData.put("created_by_role", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_ROLE)));
                        announcementData.put("created_by_id", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_FK_CREATOR_ID)));
                        announcementData.put("grade_target", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET)));

                        if (supabaseClient.insertData("announcements", announcementData)) {
                            count++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to sync announcement: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Sync all pending data (can be called periodically)
     */
    public void syncAll() {
        syncAllLocalData();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

