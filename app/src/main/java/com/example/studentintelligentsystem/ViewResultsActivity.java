package com.example.studentintelligentsystem;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ViewResultsActivity extends AppCompatActivity {

    private TableLayout resultsTable;
    private DatabaseHelper dbHelper;
    private int parentId;
    private byte[] lastPdfBytes;

    private static final int REQ_WRITE_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Results");

        dbHelper = new DatabaseHelper(this);
        resultsTable = findViewById(R.id.resultsTable);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAllResultsForParent();

        Button btnDownloadResults = findViewById(R.id.btnDownloadResults);
        btnDownloadResults.setOnClickListener(v -> onDownloadPdfClicked());
    }

    private void loadAllResultsForParent() {
        resultsTable.removeViews(1, resultsTable.getChildCount() - 1);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r." + DatabaseHelper.RESULT_SUBJECT + ", r." + DatabaseHelper.RESULT_TERM + ", r." + DatabaseHelper.RESULT_MARKS + ", r." + DatabaseHelper.RESULT_COMMENT +
                " FROM " + DatabaseHelper.TABLE_RESULTS + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON r." + DatabaseHelper.RESULT_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID +
                " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                String term = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM));
                int marks = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));
                String comments = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT));

                TableRow row = new TableRow(this);

                TextView tvSubject = createTextView(subject);
                TextView tvTerm = createTextView(term);
                TextView tvMarks = createTextView(String.valueOf(marks));
                TextView tvComments = createTextView(comments != null ? comments : "");

                row.addView(tvSubject);
                row.addView(tvTerm);
                row.addView(tvMarks);
                row.addView(tvComments);

                resultsTable.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TableRow row = new TableRow(this);
            TextView tv = createTextView("No results found.");
            row.addView(tv);
            resultsTable.addView(row);
        }
        db.close();
    }

    private void createPdfBytes(String studentName, String grade, String teacherName, String schoolName) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(resultsTable.getWidth(), resultsTable.getHeight() + 200, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(schoolName, pageInfo.getPageWidth() / 2, 50, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(16);
        canvas.drawText("Student: " + studentName, 40, 80, paint);
        canvas.drawText("Grade: " + grade, 40, 100, paint);
        canvas.drawText("Teacher: " + teacherName, 40, 120, paint);

        canvas.translate(0, 150);
        resultsTable.draw(canvas);

        document.finishPage(page);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            document.writeTo(byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();
        lastPdfBytes = byteArrayOutputStream.toByteArray();
    }

    public void onDownloadPdfClicked() {
        // Fetch the required information from the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String studentName = "";
        String grade = "";
        String teacherName = "";
        String schoolName = "";

        String query = "SELECT s." + DatabaseHelper.STUDENT_NAME + ", s." + DatabaseHelper.STUDENT_GRADE + ", t." + DatabaseHelper.TEACHER_NAME + ", a." + DatabaseHelper.ADMIN_SCHOOL_NAME +
                " FROM " + DatabaseHelper.TABLE_STUDENT + " s " +
                "INNER JOIN " + DatabaseHelper.TABLE_TEACHER + " t ON s." + DatabaseHelper.STUDENT_FK_TEACHER_ID + " = t." + DatabaseHelper.TEACHER_ID +
                " INNER JOIN " + DatabaseHelper.TABLE_ADMIN + " a ON t." + DatabaseHelper.TEACHER_FK_ADMIN_ID + " = a." + DatabaseHelper.ADMIN_ID +
                " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentId)});

        if (cursor != null && cursor.moveToFirst()) {
            studentName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
            grade = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE));
            teacherName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_NAME));
            schoolName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ADMIN_SCHOOL_NAME));
            cursor.close();
        }
        db.close();

        createPdfBytes(studentName, grade, teacherName, schoolName); // Generate the PDF bytes first
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_WRITE_STORAGE);
                return;
            }
        }
        savePdfToDownloads(this, lastPdfBytes, "results-" + System.currentTimeMillis() + ".pdf");
    }

    private void savePdfToDownloads(Context ctx, byte[] pdfBytes, String fileName) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            Toast.makeText(ctx, "No PDF to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyAppResults");

                Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
                if (uri == null) throw new RuntimeException("Failed to create file Uri");

                try (OutputStream out = ctx.getContentResolver().openOutputStream(uri)) {
                    out.write(pdfBytes);
                }

                Toast.makeText(ctx, "Saved to Downloads/MyAppResults/" + fileName, Toast.LENGTH_LONG).show();

            } else {
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File folder = new File(downloads, "MyAppResults");
                if (!folder.exists()) folder.mkdir();
                File outFile = new File(folder, fileName);

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(pdfBytes);
                }

                MediaScannerConnection.scanFile(ctx, new String[]{ outFile.getAbsolutePath() }, new String[]{"application/pdf"}, null);

                Toast.makeText(ctx, "Saved to Downloads/MyAppResults/" + fileName, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onDownloadPdfClicked();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setTextColor(Color.BLACK);
        return tv;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
