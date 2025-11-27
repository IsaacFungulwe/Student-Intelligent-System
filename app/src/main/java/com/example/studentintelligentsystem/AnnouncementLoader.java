package com.example.studentintelligentsystem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.studentintelligentsystem.supabase.SupabaseClient;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to load announcements from both local database and Supabase
 * Provides unified announcement loading across all views
 */
public class AnnouncementLoader {
    private static final String TAG = "AnnouncementLoader";

    private Context context;
    private DatabaseHelper dbHelper;

    public AnnouncementLoader(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Load announcements for admin view (all announcements)
     */
    public List<String> loadAdminAnnouncements() {
        return loadAnnouncementsFromLocal(null, null);
    }

    /**
     * Load announcements for teacher view (grade-specific + general)
     */
    public List<String> loadTeacherAnnouncements(int teacherGrade) {
        String selection = DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " = ? OR " +
                          DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IS NULL";
        String[] selectionArgs = {String.valueOf(teacherGrade)};
        return loadAnnouncementsFromLocal(selection, selectionArgs);
    }

    /**
     * Load announcements for parent view (children's grades + general)
     */
    public List<String> loadParentAnnouncements(List<Integer> childrenGrades) {
        if (childrenGrades == null || childrenGrades.isEmpty()) {
            // Load only general announcements
            String selection = DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IS NULL";
            return loadAnnouncementsFromLocal(selection, null);
        }

        // Build selection for multiple grades
        StringBuilder selection = new StringBuilder();
        selection.append("(");
        List<String> selectionArgs = new ArrayList<>();

        for (int i = 0; i < childrenGrades.size(); i++) {
            if (i > 0) selection.append(" OR ");
            selection.append(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET).append(" = ?");
            selectionArgs.add(String.valueOf(childrenGrades.get(i)));
        }

        selection.append(" OR ").append(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET).append(" IS NULL)");

        return loadAnnouncementsFromLocal(selection.toString(),
                selectionArgs.toArray(new String[0]));
    }

    /**
     * Load announcements from local database with optional filtering
     */
    private List<String> loadAnnouncementsFromLocal(String selection, String[] selectionArgs) {
        List<String> announcementList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ANNOUNCEMENT,
                new String[]{
                    DatabaseHelper.ANNOUNCEMENT_TITLE,
                    DatabaseHelper.ANNOUNCEMENT_MESSAGE,
                    DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL,
                    DatabaseHelper.ANNOUNCEMENT_TIMESTAMP,
                    DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET
                },
                selection,
                selectionArgs,
                null, null,
                DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseHelper.ANNOUNCEMENT_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseHelper.ANNOUNCEMENT_MESSAGE));
                String source = cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(
                        DatabaseHelper.ANNOUNCEMENT_TIMESTAMP));

                // Format timestamp
                String formattedTimestamp = formatTimestamp(timestamp);

                // Get grade target if exists
                int gradeColumnIndex = cursor.getColumnIndexOrThrow(
                        DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET);
                String gradeInfo = cursor.isNull(gradeColumnIndex) ?
                        "All Grades" : "Grade " + cursor.getInt(gradeColumnIndex);

                announcementList.add(
                    "[" + source + "] " + title + "\n" +
                    message + "\n" +
                    gradeInfo + " • " + formattedTimestamp
                );
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (announcementList.isEmpty()) {
            announcementList.add("No announcements available.\nPull down to refresh from cloud.");
        }

        return announcementList;
    }

    /**
     * Sync announcements from Supabase to local database
     */
    public void syncAnnouncementsFromSupabase(OnSyncCompleteListener listener) {
        if (!SupabaseConfig.isConfigured()) {
            if (listener != null) {
                listener.onSyncComplete(false, "Supabase not configured");
            }
            return;
        }

        new Thread(() -> {
            try {
                SupabaseClient client = SupabaseClient.getInstance();
                String response = client.queryData("announcements",
                        "select=*&order=timestamp.desc");

                if (response != null) {
                    JSONArray announcements = new JSONArray(response);
                    Log.d(TAG, "✓ Loaded " + announcements.length() +
                            " announcements from Supabase");

                    // Sync to local database
                    syncAnnouncementsToLocal(announcements);

                    if (listener != null) {
                        listener.onSyncComplete(true,
                                "Synced " + announcements.length() + " announcements");
                    }
                } else {
                    if (listener != null) {
                        listener.onSyncComplete(false, "No data from Supabase");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing announcements: " + e.getMessage());
                if (listener != null) {
                    listener.onSyncComplete(false, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Sync announcements array to local database
     */
    private void syncAnnouncementsToLocal(JSONArray announcements) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            for (int i = 0; i < announcements.length(); i++) {
                JSONObject announcement = announcements.getJSONObject(i);

                android.content.ContentValues values = new android.content.ContentValues();
                values.put(DatabaseHelper.ANNOUNCEMENT_ID, announcement.getInt("id"));
                values.put(DatabaseHelper.ANNOUNCEMENT_TITLE,
                        announcement.getString("title"));
                values.put(DatabaseHelper.ANNOUNCEMENT_MESSAGE,
                        announcement.getString("message"));
                values.put(DatabaseHelper.ANNOUNCEMENT_ROLE,
                        announcement.optString("created_by_role", "System"));
                values.put(DatabaseHelper.ANNOUNCEMENT_FK_CREATOR_ID,
                        announcement.optInt("created_by_id", 0));

                if (!announcement.isNull("grade_target")) {
                    values.put(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET,
                            announcement.getInt("grade_target"));
                }

                values.put(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL,
                        announcement.optString("source_label", "System"));
                values.put(DatabaseHelper.ANNOUNCEMENT_TIMESTAMP,
                        announcement.getString("timestamp"));

                db.insertWithOnConflict(DatabaseHelper.TABLE_ANNOUNCEMENT, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.close();
            Log.d(TAG, "✓ Synced " + announcements.length() + " announcements to local");
        } catch (Exception e) {
            Log.e(TAG, "Error syncing announcements to local: " + e.getMessage());
        }
    }

    /**
     * Format timestamp for display
     */
    private String formatTimestamp(String timestamp) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a",
                Locale.getDefault());

        try {
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp; // Return raw timestamp if parsing fails
        }
    }

    /**
     * Interface for sync completion callback
     */
    public interface OnSyncCompleteListener {
        void onSyncComplete(boolean success, String message);
    }
}

