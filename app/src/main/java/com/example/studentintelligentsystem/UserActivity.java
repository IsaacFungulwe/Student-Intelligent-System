package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private TextView tvWelcome, tvStudentName, tvStudentGrade, tvAttendancePercentage;
    private ProgressBar pbAttendance;
    private RecyclerView rvResults, rvAnnouncements;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = new DatabaseHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentGrade = findViewById(R.id.tvStudentGrade);
        tvAttendancePercentage = findViewById(R.id.tvAttendancePercentage);
        pbAttendance = findViewById(R.id.pbAttendance);
        rvResults = findViewById(R.id.rvResults);
        rvAnnouncements = findViewById(R.id.rvAnnouncements);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));

        String parentEmail = getIntent().getStringExtra("PARENT_EMAIL");

        if (parentEmail != null) {
            loadStudentData(parentEmail);
            loadAnnouncements();
        }
    }

    private void loadStudentData(String parentEmail) {
        Cursor studentCursor = db.getStudentByParentEmail(parentEmail);
        if (studentCursor != null && studentCursor.moveToFirst()) {
            int studentId = studentCursor.getInt(studentCursor.getColumnIndexOrThrow("id"));
            String studentName = studentCursor.getString(studentCursor.getColumnIndexOrThrow("stu_name"));
            int studentGrade = studentCursor.getInt(studentCursor.getColumnIndexOrThrow("stu_grade"));

            tvStudentName.setText(studentName);
            tvStudentGrade.setText("Grade: " + studentGrade);
            tvWelcome.setText("Welcome " + studentCursor.getString(studentCursor.getColumnIndexOrThrow("stu_parent_name")));

            loadAttendanceData(studentId);
            loadResultsData(studentId);
            studentCursor.close();
        }
    }

    private void loadAttendanceData(int studentId) {
        Cursor attendanceCursor = db.getAttendanceForStudent(studentId);
        int totalDays = 0;
        int presentDays = 0;
        if (attendanceCursor != null && attendanceCursor.moveToFirst()) {
            totalDays = attendanceCursor.getCount();
            do {
                int present = attendanceCursor.getInt(attendanceCursor.getColumnIndexOrThrow("present"));
                if (present == 1) {
                    presentDays++;
                }
            } while (attendanceCursor.moveToNext());
            attendanceCursor.close();
        }

        if (totalDays > 0) {
            int attendancePercentage = (int) (((double) presentDays / totalDays) * 100);
            pbAttendance.setProgress(attendancePercentage);
            tvAttendancePercentage.setText(attendancePercentage + "% Present");
        } else {
            pbAttendance.setProgress(0);
            tvAttendancePercentage.setText("No attendance data");
        }
    }

    private void loadResultsData(int studentId) {
        List<Result> resultsList = new ArrayList<>();
        Cursor resultsCursor = db.getResultsForStudent(studentId);
        if (resultsCursor != null && resultsCursor.moveToFirst()) {
            do {
                String subject = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow("subject"));
                int score = resultsCursor.getInt(resultsCursor.getColumnIndexOrThrow("score"));
                resultsList.add(new Result(subject, score));
            } while (resultsCursor.moveToNext());
            resultsCursor.close();
        }
        ResultsAdapter resultsAdapter = new ResultsAdapter(resultsList);
        rvResults.setAdapter(resultsAdapter);
    }

    private void loadAnnouncements() {
        List<Announcement> announcementList = new ArrayList<>();
        Cursor announcementsCursor = db.getAllAnnouncements();
        if (announcementsCursor != null && announcementsCursor.moveToFirst()) {
            do {
                String title = announcementsCursor.getString(announcementsCursor.getColumnIndexOrThrow("title"));
                String body = announcementsCursor.getString(announcementsCursor.getColumnIndexOrThrow("body"));
                announcementList.add(new Announcement(title, body));
            } while (announcementsCursor.moveToNext());
            announcementsCursor.close();
        }
        AnnouncementsAdapter announcementsAdapter = new AnnouncementsAdapter(announcementList);
        rvAnnouncements.setAdapter(announcementsAdapter);
    }
}
