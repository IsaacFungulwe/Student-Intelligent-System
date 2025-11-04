package com.example.studentintelligentsystem;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditResultDialog extends Dialog {

    private Result result;
    private OnResultUpdatedListener listener;

    private TextView tvStudentName;
    private EditText editSubject;
    private EditText editTerm;
    private EditText editMarks;
    private EditText editComment;
    private Button btnSave;
    private Button btnCancel;

    public interface OnResultUpdatedListener {
        void onResultUpdated(Result updatedResult);
    }

    public EditResultDialog(Context context, Result result, OnResultUpdatedListener listener) {
        super(context);
        this.result = result;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_result);

        tvStudentName = findViewById(R.id.tvStudentName);
        editSubject = findViewById(R.id.editSubject);
        editTerm = findViewById(R.id.editTerm);
        editMarks = findViewById(R.id.editMarks);
        editComment = findViewById(R.id.editComment);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Populate fields with current data
        tvStudentName.setText(result.getStudentName());
        editSubject.setText(result.getSubject());
        editTerm.setText(result.getTerm());
        editMarks.setText(String.valueOf(result.getMarks()));
        editComment.setText(result.getComment());

        btnSave.setOnClickListener(v -> {
            if (validateAndSave()) {
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private boolean validateAndSave() {
        String subject = editSubject.getText().toString().trim();
        String term = editTerm.getText().toString().trim();
        String marksStr = editMarks.getText().toString().trim();
        String comment = editComment.getText().toString().trim();

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(term) || TextUtils.isEmpty(marksStr)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        int marks;
        try {
            marks = Integer.parseInt(marksStr);
            if (marks < 0 || marks > 100) {
                Toast.makeText(getContext(), "Marks must be between 0 and 100", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid marks value", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Create updated result
        Result updatedResult = new Result(
                result.getResultId(),
                result.getStudentId(),
                result.getStudentName(),
                subject,
                term,
                marks,
                comment
        );

        if (listener != null) {
            listener.onResultUpdated(updatedResult);
        }

        return true;
    }
}

