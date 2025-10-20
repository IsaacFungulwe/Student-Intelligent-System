package com.example.studentintelligentsystem;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.studentintelligentsystem.ManageStudentsActivity;

public class AdminDashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the existing layout file activity_admin_dash_board.xml
        setContentView(R.layout.activity_admin_dash_board);

        // Wire Add/Remove card to ManageStudentsActivity
        CardView cardAddRemove = findViewById(R.id.cardAddRemove);
        if (cardAddRemove != null) {
            cardAddRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, ManageStudentsActivity.class));
                }
            });
        }

        // Wire Check Results
        CardView cardCheck = findViewById(R.id.cardCheckResults);
        if (cardCheck != null) {
            cardCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, CheckResultsActivity.class));
                }
            });
        }

        // Wire Update Results
        CardView cardUpdate = findViewById(R.id.cardUpdateResults);
        if (cardUpdate != null) {
            cardUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, UpdateResultsActivity.class));
                }
            });
        }

        // Wire Mark Register
        CardView cardMark = findViewById(R.id.cardMarkRegister);
        if (cardMark != null) {
            cardMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, MarkRegisterActivity.class));
                }
            });
        }

        // You can wire up other CardView click listeners here if needed
    }

    // Add other methods for your admin buttons (Check Results, Update Results etc.)
}