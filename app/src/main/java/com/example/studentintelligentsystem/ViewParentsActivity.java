package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class ViewParentsActivity extends AppCompatActivity {

    private ListView lvParents;
    private DatabaseHelper dbHelper;
    private int teacherGrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_parents);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Parents");

        dbHelper = new DatabaseHelper(this);
        lvParents = findViewById(R.id.lvParents);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade == -1) {
            Toast.makeText(this, "Error: Could not verify your grade.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadParents();
    }

    private void loadParents() {
        List<Parent> parentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT p." + DatabaseHelper.PARENT_NAME + ", p." + DatabaseHelper.PARENT_EMAIL + ", p." + DatabaseHelper.PARENT_PHONE +
                " FROM " + DatabaseHelper.TABLE_PARENT + " p " +
                "INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON p." + DatabaseHelper.PARENT_ID + " = s." + DatabaseHelper.STUDENT_FK_PARENT_ID +
                " WHERE s." + DatabaseHelper.STUDENT_GRADE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherGrade)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PHONE));
                parentList.add(new Parent(name, email, phone));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (parentList.isEmpty()) {
            Toast.makeText(this, "No parents found for your grade.", Toast.LENGTH_SHORT).show();
        } else {
            ParentAdapter adapter = new ParentAdapter(this, parentList);
            lvParents.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inner class for the Parent data model
    private static class Parent {
        private final String name;
        private final String email;
        private final String phone;

        public Parent(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    // Inner class for the custom adapter
    private class ParentAdapter extends ArrayAdapter<Parent> {

        public ParentAdapter(@NonNull Context context, @NonNull List<Parent> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_parent, parent, false);
            }

            Parent parentItem = getItem(position);

            TextView tvParentName = convertView.findViewById(R.id.tvParentName);
            TextView tvParentEmail = convertView.findViewById(R.id.tvParentEmail);
            TextView tvParentPhone = convertView.findViewById(R.id.tvParentPhone);

            if (parentItem != null) {
                tvParentName.setText(parentItem.getName());
                tvParentEmail.setText(parentItem.getEmail());
                tvParentPhone.setText(parentItem.getPhone());
            }

            return convertView;
        }
    }
}
