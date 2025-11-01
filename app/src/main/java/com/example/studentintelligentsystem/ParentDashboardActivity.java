package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParentDashboardActivity extends AppCompatActivity {

    private TextView tvParentWelcome;
    private ListView lvMyChildren, lvAnnouncements;
    private CardView cardViewResults, cardAIAnalysis;
    private PieChart pieChart;
    private BarChart barChart;
    private DatabaseHelper dbHelper;
    private int parentId;
    private final HashMap<String, Integer> childrenMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        tvParentWelcome = findViewById(R.id.tvParentWelcome);
        lvMyChildren = findViewById(R.id.lvMyChildren);
        lvAnnouncements = findViewById(R.id.lvAnnouncements);
        cardViewResults = findViewById(R.id.cardViewResults);
        cardAIAnalysis = findViewById(R.id.cardAIAnalysis);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadChildrenData();
        loadAnnouncements();
        loadResultsForChart();
        loadAttendanceForChart();

        cardViewResults.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewResultsActivity.class);
            startActivity(intent);
        });

        cardAIAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerformanceAnalysisActivity.class);
            startActivity(intent);
        });

        lvMyChildren.setOnItemClickListener((parent, view, position, id) -> {
            String selectedChild = (String) parent.getItemAtPosition(position);
            // Extract the name from the "Name - Grade: X" string
            String childName = selectedChild.split(" - ")[0];
            Integer childId = childrenMap.get(childName);

            if (childId != null) {
                Intent intent = new Intent(this, AttendanceHistoryActivity.class);
                intent.putExtra("studentId", childId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Could not find details for the selected child.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildrenData() {
        Set<Integer> childrenGrades = new HashSet<>();
        ArrayList<String> childrenList = new ArrayList<>();
        childrenMap.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, new String[]{DatabaseHelper.STUDENT_ID, DatabaseHelper.STUDENT_NAME, DatabaseHelper.STUDENT_GRADE}, DatabaseHelper.STUDENT_FK_PARENT_ID + " = ?", new String[]{String.valueOf(parentId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE));
                childrenList.add(name + " - Grade: " + grade);
                childrenMap.put(name, studentId);
                childrenGrades.add(grade);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (childrenList.isEmpty()) {
            childrenList.add("No children linked to this account yet.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, childrenList);
        lvMyChildren.setAdapter(adapter);

        SharedPreferences.Editor editor = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();
        Set<String> gradesStrSet = new HashSet<>();
        for (Integer grade : childrenGrades) {
            gradesStrSet.add(String.valueOf(grade));
        }
        editor.putStringSet("userChildrenGrades", gradesStrSet);
        editor.apply();
    }

    private void loadAnnouncements() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> childrenGrades = prefs.getStringSet("userChildrenGrades", new HashSet<>());

        ArrayList<String> announcementList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IS NULL");
        if (!childrenGrades.isEmpty()) {
            selection.append(" OR " + DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IN (");
            for (int i = 0; i < childrenGrades.size(); i++) {
                selection.append("?");
                if (i < childrenGrades.size() - 1) {
                    selection.append(",");
                }
            }
            selection.append(")");
        }

        String[] selectionArgs = childrenGrades.toArray(new String[0]);

        Cursor cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT, new String[]{DatabaseHelper.ANNOUNCEMENT_TITLE, DatabaseHelper.ANNOUNCEMENT_MESSAGE, DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL}, selection.toString(), selectionArgs, null, null, DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE));
                String source = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL));
                announcementList.add("[" + source + "] " + title + "\n" + message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (announcementList.isEmpty()) {
            announcementList.add("No announcements available right now.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        lvAnnouncements.setAdapter(adapter);
    }

    private void loadResultsForChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> childrenGrades = prefs.getStringSet("userChildrenGrades", new HashSet<>());
        Map<String, Float> subjectAverages = new HashMap<>();

        if (childrenGrades.isEmpty()) {
            pieChart.setNoDataText("No results available to display chart.");
            return;
        }

        String grades = String.join(",", childrenGrades);
        String subjectsQuery = "SELECT DISTINCT " + DatabaseHelper.SUBJECT_NAME + " FROM " + DatabaseHelper.TABLE_SUBJECTS + " WHERE " + DatabaseHelper.SUBJECT_GRADE + " IN (" + grades + ")";
        Cursor subjectsCursor = db.rawQuery(subjectsQuery, null);

        if (subjectsCursor != null && subjectsCursor.moveToFirst()) {
            do {
                String subject = subjectsCursor.getString(subjectsCursor.getColumnIndexOrThrow(DatabaseHelper.SUBJECT_NAME));
                subjectAverages.put(subject, 0f);
            } while (subjectsCursor.moveToNext());
            subjectsCursor.close();
        }

        String resultsQuery = "SELECT r." + DatabaseHelper.RESULT_SUBJECT + ", AVG(r." + DatabaseHelper.RESULT_MARKS + ") as avg_marks FROM " + DatabaseHelper.TABLE_RESULTS + " r INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON r." + DatabaseHelper.RESULT_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID + " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ? GROUP BY r." + DatabaseHelper.RESULT_SUBJECT;

        Cursor resultsCursor = db.rawQuery(resultsQuery, new String[]{String.valueOf(parentId)});

        if (resultsCursor != null && resultsCursor.moveToFirst()) {
            do {
                String subject = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                float avgMarks = resultsCursor.getFloat(resultsCursor.getColumnIndexOrThrow("avg_marks"));
                subjectAverages.put(subject, avgMarks);
            } while (resultsCursor.moveToNext());
            resultsCursor.close();
        }

        for (Map.Entry<String, Float> entry : subjectAverages.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No results available to display chart.");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Results");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate(); // refresh
    }

    private void loadAttendanceForChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int presentCount = 0;
        int absentCount = 0;

        String query = "SELECT " + DatabaseHelper.ATTENDANCE_STATUS + ", COUNT(" + DatabaseHelper.ATTENDANCE_STATUS + ") FROM " + DatabaseHelper.TABLE_ATTENDANCE + " a INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON a." + DatabaseHelper.ATTENDANCE_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID + " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ? GROUP BY " + DatabaseHelper.ATTENDANCE_STATUS;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String status = cursor.getString(0);
                int count = cursor.getInt(1);
                if ("Present".equalsIgnoreCase(status)) {
                    presentCount = count;
                } else if ("Absent".equalsIgnoreCase(status)) {
                    absentCount = count;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        entries.add(new BarEntry(0, presentCount));
        entries.add(new BarEntry(1, absentCount));

        BarDataSet dataSet = new BarDataSet(entries, "Attendance");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        ArrayList<String> labels = new ArrayList<>();
        labels.add("Present");
        labels.add("Absent");

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(65f); // 65 days in a term
        barChart.getAxisRight().setEnabled(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Overriding to do nothing and stay on the dashboard
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
