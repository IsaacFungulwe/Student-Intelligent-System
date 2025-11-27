package com.example.studentintelligentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";

    private CardView cardRegisterTeacher, cardRegisterParent, cardPostAnnouncement,
                     cardManageAnnouncements, cardViewTeachers;
    private AnnouncementLoader announcementLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        announcementLoader = new AnnouncementLoader(this);

        cardRegisterTeacher = findViewById(R.id.cardRegisterTeacher);
        cardRegisterParent = findViewById(R.id.cardRegisterParent);
        cardPostAnnouncement = findViewById(R.id.cardPostAnnouncement);
        cardManageAnnouncements = findViewById(R.id.cardManageAnnouncements);
        cardViewTeachers = findViewById(R.id.cardViewTeachers);

        cardRegisterTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherRegisterActivity.class);
            startActivity(intent);
        });

        cardRegisterParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentRegisterActivity.class);
            startActivity(intent);
        });

        cardPostAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostAnnouncementActivity.class);
            startActivity(intent);
        });

        cardManageAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAnnouncementsActivity.class);
            startActivity(intent);
        });

        cardViewTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewTeachersActivity.class);
            startActivity(intent);
        });

        // Load initial data
        syncDataFromSupabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        syncDataFromSupabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            syncDataFromSupabase();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Overriding to do nothing and stay on the dashboard
    }

    private void syncDataFromSupabase() {
        Toast.makeText(this, "Syncing data from cloud...", Toast.LENGTH_SHORT).show();

        // Sync announcements
        announcementLoader.syncAnnouncementsFromSupabase((success, message) -> {
            runOnUiThread(() -> {
                if (success) {
                    Log.d(TAG, "✓ " + message);
                    Toast.makeText(this, "Data synced successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "✗ Sync failed: " + message);
                    Toast.makeText(this, "Sync completed with errors", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
