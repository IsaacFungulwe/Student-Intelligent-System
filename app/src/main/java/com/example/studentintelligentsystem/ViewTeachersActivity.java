package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.studentintelligentsystem.supabase.SupabaseClient;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewTeachersActivity extends AppCompatActivity {
    private static final String TAG = "ViewTeachersActivity";

    private ListView lvTeachers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseHelper dbHelper;
    private int adminId;
    private ArrayAdapter<String> adapter;
    private List<String> teacherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_teachers);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Registered Teachers");

        dbHelper = new DatabaseHelper(this);
        lvTeachers = findViewById(R.id.lvTeachers);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        adminId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (adminId == -1) {
            Toast.makeText(this, "Error: Could not verify your login.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        teacherList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teacherList);
        lvTeachers.setAdapter(adapter);

        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTeachersFromSupabase();
        });

        loadTeachers();
    }

    private void loadTeachers() {
        loadTeachersFromLocal();

        // Also try to load from Supabase in background
        if (SupabaseConfig.isConfigured()) {
            loadTeachersFromSupabase();
        }
    }

    private void loadTeachersFromLocal() {
        teacherList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Load all teachers (not just for this admin, to show synchronized teachers too)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TEACHER,
                new String[]{
                    DatabaseHelper.TEACHER_NAME,
                    DatabaseHelper.TEACHER_EMAIL,
                    DatabaseHelper.TEACHER_GRADE_ASSIGNED,
                    DatabaseHelper.TEACHER_FK_ADMIN_ID
                },
                null, // Load all teachers
                null,
                null, null,
                DatabaseHelper.TEACHER_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_EMAIL));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED));
                int teacherAdminId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_FK_ADMIN_ID));

                String adminIndicator = (teacherAdminId == adminId) ? " ★" : "";
                teacherList.add(name + adminIndicator + "\n" + email + "\nGrade: " + grade);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (teacherList.isEmpty()) {
            teacherList.add("No teachers registered yet.\nPull down to refresh from cloud.");
        }

        adapter.notifyDataSetChanged();
    }

    private void loadTeachersFromSupabase() {
        if (!SupabaseConfig.isConfigured()) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        new Thread(() -> {
            try {
                SupabaseClient client = SupabaseClient.getInstance();
                String response = client.queryData("teachers", "select=*&order=name.asc");

                if (response != null) {
                    JSONArray teachers = new JSONArray(response);
                    Log.d(TAG, "✓ Loaded " + teachers.length() + " teachers from Supabase");

                    // Sync to local database
                    syncTeachersToLocal(teachers);

                    runOnUiThread(() -> {
                        loadTeachersFromLocal();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        Toast.makeText(this, "Teachers synced from cloud", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        Toast.makeText(this, "Could not load from cloud", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading teachers from Supabase: " + e.getMessage());
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(this, "Sync error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void syncTeachersToLocal(JSONArray teachers) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            for (int i = 0; i < teachers.length(); i++) {
                JSONObject teacher = teachers.getJSONObject(i);

                android.content.ContentValues values = new android.content.ContentValues();
                values.put(DatabaseHelper.TEACHER_ID, teacher.getInt("id"));
                values.put(DatabaseHelper.TEACHER_NAME, teacher.getString("name"));
                values.put(DatabaseHelper.TEACHER_EMAIL, teacher.getString("email"));
                values.put(DatabaseHelper.TEACHER_PASSWORD, teacher.getString("password_hash"));
                values.put(DatabaseHelper.TEACHER_GRADE_ASSIGNED, teacher.getInt("grade_assigned"));
                values.put(DatabaseHelper.TEACHER_FK_ADMIN_ID, teacher.getInt("admin_id"));

                db.insertWithOnConflict(DatabaseHelper.TABLE_TEACHER, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.close();
            Log.d(TAG, "✓ Synced " + teachers.length() + " teachers to local database");
        } catch (Exception e) {
            Log.e(TAG, "Error syncing teachers to local: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            swipeRefreshLayout.setRefreshing(true);
            loadTeachersFromSupabase();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
