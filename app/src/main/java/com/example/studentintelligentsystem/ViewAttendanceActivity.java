package com.example.studentintelligentsystem;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "com.example.studentintelligentsystem.DATE";

    private RecyclerView rvAttendanceDates;
    private DateAdapter adapter;
    private List<String> dateList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        dbHelper = new DatabaseHelper(this);
        rvAttendanceDates = findViewById(R.id.rvAttendanceDates);
        rvAttendanceDates.setLayoutManager(new LinearLayoutManager(this));

        loadAttendanceDates();
    }

    private void loadAttendanceDates() {
        dateList = dbHelper.getUniqueAttendanceDates();

        adapter = new DateAdapter(dateList);
        rvAttendanceDates.setAdapter(adapter);
    }
}
