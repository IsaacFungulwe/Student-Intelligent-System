package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DailyAttendanceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "com.example.studentintelligentsystem.DATE";

    private RecyclerView rvAttendanceList;
    private ViewAttendanceAdapter adapter;
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_attendance_detail);

        dbHelper = new DatabaseHelper(this);
        date = getIntent().getStringExtra(EXTRA_DATE);

        TextView tvAttendanceDate = findViewById(R.id.tvAttendanceDate);
        rvAttendanceList = findViewById(R.id.rvAttendanceList);

        rvAttendanceList.setLayoutManager(new LinearLayoutManager(this));

        if (date != null) {
            tvAttendanceDate.setText("Attendance for " + date);
            loadAttendanceData();
        }
    }

    private void loadAttendanceData() {
        Cursor cursor = dbHelper.getAttendanceByDate(date);

        if (cursor != null) {
            int studentNameIndex = cursor.getColumnIndex("stu_name");
            int presentIndex = cursor.getColumnIndex("present");

            while (cursor.moveToNext()) {
                if (studentNameIndex != -1 && presentIndex != -1) {
                    String studentName = cursor.getString(studentNameIndex);
                    boolean isPresent = cursor.getInt(presentIndex) == 1;
                    attendanceRecords.add(new AttendanceRecord(studentName, date, isPresent));
                }
            }
            cursor.close();
        }

        adapter = new ViewAttendanceAdapter(attendanceRecords);
        rvAttendanceList.setAdapter(adapter);
    }
}
