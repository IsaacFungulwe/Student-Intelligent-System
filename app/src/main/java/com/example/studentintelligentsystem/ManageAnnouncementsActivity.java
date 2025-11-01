package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageAnnouncementsActivity extends AppCompatActivity {

    private ListView lvAnnouncements;
    private DatabaseHelper dbHelper;
    private String userRole;
    private int userId;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_announcements);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Announcements");

        dbHelper = new DatabaseHelper(this);
        lvAnnouncements = findViewById(R.id.lvAnnouncements);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: Could not verify your login.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        announcementList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        if ("Admin".equals(userRole)) {
            cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT, null, null, null, null, null, DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC");
        } else { // Teacher
            cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT,
                    null,
                    DatabaseHelper.ANNOUNCEMENT_FK_CREATOR_ID + " = ? AND " + DatabaseHelper.ANNOUNCEMENT_ROLE + " = 'Teacher'",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC");
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TIMESTAMP));
                announcementList.add(new Announcement(id, title, message, timestamp));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (adapter == null) {
            adapter = new AnnouncementAdapter(this, announcementList);
            lvAnnouncements.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(announcementList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showEditDialog(Announcement announcement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Announcement");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_announcement, (ViewGroup) findViewById(android.R.id.content), false);
        final EditText inputTitle = viewInflated.findViewById(R.id.editAnnouncementTitle);
        final EditText inputMessage = viewInflated.findViewById(R.id.editAnnouncementMessage);

        inputTitle.setText(announcement.getTitle());
        inputMessage.setText(announcement.getMessage());

        builder.setView(viewInflated);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newMessage = inputMessage.getText().toString().trim();
            if (!newTitle.isEmpty() && !newMessage.isEmpty()) {
                updateAnnouncement(announcement.getId(), newTitle, newMessage);
            } else {
                Toast.makeText(ManageAnnouncementsActivity.this, "Title and message cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmationDialog(Announcement announcement) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAnnouncement(announcement.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateAnnouncement(int announcementId, String title, String message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ANNOUNCEMENT_TITLE, title);
        values.put(DatabaseHelper.ANNOUNCEMENT_MESSAGE, message);

        int rows = db.update(DatabaseHelper.TABLE_ANNOUNCEMENT, values, DatabaseHelper.ANNOUNCEMENT_ID + " = ?", new String[]{String.valueOf(announcementId)});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "Announcement updated successfully.", Toast.LENGTH_SHORT).show();
            loadAnnouncements(); // Refresh list
        } else {
            Toast.makeText(this, "Failed to update announcement.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAnnouncement(int announcementId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_ANNOUNCEMENT, DatabaseHelper.ANNOUNCEMENT_ID + " = ?", new String[]{String.valueOf(announcementId)});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "Announcement deleted successfully.", Toast.LENGTH_SHORT).show();
            loadAnnouncements(); // Refresh list
        } else {
            Toast.makeText(this, "Failed to delete announcement.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inner class for the custom adapter
    private class AnnouncementAdapter extends ArrayAdapter<Announcement> {

        public AnnouncementAdapter(@NonNull Context context, @NonNull List<Announcement> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_announcement_manage, parent, false);
            }

            Announcement announcement = getItem(position);

            TextView tvTitle = convertView.findViewById(R.id.tvAnnouncementTitle);
            TextView tvMessage = convertView.findViewById(R.id.tvAnnouncementMessage);
            TextView tvTimestamp = convertView.findViewById(R.id.tvAnnouncementTimestamp);
            Button btnEdit = convertView.findViewById(R.id.btnEdit);
            Button btnDelete = convertView.findViewById(R.id.btnDelete);

            if (announcement != null) {
                tvTitle.setText(announcement.getTitle());
                tvMessage.setText(announcement.getMessage());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                try {
                    Date date = sdf.parse(announcement.getTimestamp());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                    tvTimestamp.setText(outputFormat.format(date));
                } catch (Exception e) {
                    tvTimestamp.setText(announcement.getTimestamp());
                }

                btnEdit.setOnClickListener(v -> showEditDialog(announcement));
                btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(announcement));
            }

            return convertView;
        }
    }

    private static class Announcement {
        private final int id;
        private final String title;
        private final String message;
        private final String timestamp;

        public Announcement(int id, String title, String message, String timestamp) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }
}
