package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_STUDENT = "com.example.studentintelligentsystem.STUDENT";

    private RecyclerView rvAttendanceHistory;
    private AttendanceHistoryAdapter adapter;
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private Student student;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        dbHelper = new DatabaseHelper(this);

        // Safely get the Parcelable extra, handling API level differences
        if (Build.VERSION.SDK_INT >= 33) { // TIRAMISU
            student = getIntent().getParcelableExtra(EXTRA_STUDENT, Student.class);
        } else {
            // Use the deprecated method for older APIs
            student = getIntent().getParcelableExtra(EXTRA_STUDENT);
        }

        TextView tvStudentName = findViewById(R.id.tvStudentName);
        rvAttendanceHistory = findViewById(R.id.rvAttendanceHistory);

        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(this));

        if (student != null) {
            tvStudentName.setText(student.getName());
            loadAttendanceHistory();
        }
    }

    private void loadAttendanceHistory() {
        Cursor cursor = dbHelper.getAttendanceForStudent((int) student.getId());

        if (cursor != null) {
            int dateIndex = cursor.getColumnIndex("date");
            int presentIndex = cursor.getColumnIndex("present");

            if (dateIndex != -1 && presentIndex != -1) {
                while (cursor.moveToNext()) {
                    String date = cursor.getString(dateIndex);
                    boolean isPresent = cursor.getInt(presentIndex) == 1;
                    attendanceRecords.add(new AttendanceRecord(date, isPresent));
                }
            }
            cursor.close();
        }

        adapter = new AttendanceHistoryAdapter(attendanceRecords);
        rvAttendanceHistory.setAdapter(adapter);
    }
}
