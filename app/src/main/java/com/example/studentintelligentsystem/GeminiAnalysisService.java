package com.example.studentintelligentsystem;

import android.content.Context;
import android.database.Cursor;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class GeminiAnalysisService {

    private final GenerativeModelFutures model;
    private final DatabaseHelper dbHelper;

    // API key is loaded from BuildConfig (local.properties)
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    public GeminiAnalysisService(Context context) {
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
        this.model = GenerativeModelFutures.from(gm);
        this.dbHelper = new DatabaseHelper(context);
    }

    public interface AnalysisCallback {
        void onSuccess(String analysis);
        void onError(String errorMessage);
    }

    /**
       Analyze student performance using Gemini AI
       @param studentId The ID of the student to analyze
       @param callback Callback to receive analysis results
       @param executor Executor for running async operations
     */
    public void analyzeStudentPerformance(int studentId, AnalysisCallback callback, Executor executor) {
        // Fetch student data
        StudentPerformanceData data = fetchStudentData(studentId);

        if (data == null) {
            callback.onError("Unable to fetch student data");
            return;
        }

        // Build prompt for Gemini
        String prompt = buildAnalysisPrompt(data);

        // Generate content using Gemini
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String analysisText = result.getText();
                callback.onSuccess(analysisText);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Error generating analysis: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Fetch all relevant student performance data
     */
    private StudentPerformanceData fetchStudentData(int studentId) {
        StudentPerformanceData data = new StudentPerformanceData();

        // Get student info
        Cursor studentCursor = dbHelper.getStudentById(studentId);
        if (studentCursor != null && studentCursor.moveToFirst()) {
            data.studentName = studentCursor.getString(studentCursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
            data.grade = studentCursor.getInt(studentCursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE));
            studentCursor.close();
        } else {
            return null;
        }

        // Get grades
        data.grades = new HashMap<>();
        Cursor resultsCursor = dbHelper.getResultsForStudent(studentId);
        if (resultsCursor != null && resultsCursor.moveToFirst()) {
            do {
                String subject = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                int marks = resultsCursor.getInt(resultsCursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));
                String comment = resultsCursor.getString(resultsCursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT));

                data.grades.put(subject, marks);
                if (comment != null && !comment.isEmpty()) {
                    data.teacherComments.add(comment);
                }
            } while (resultsCursor.moveToNext());
            resultsCursor.close();
        }

        // Get attendance percentage
        data.attendancePercentage = dbHelper.getAttendancePercentage(studentId);

        return data;
    }

    /**
     * Build comprehensive analysis prompt for Gemini AI
     */
    private String buildAnalysisPrompt(StudentPerformanceData data) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an educational analyst. Analyze the following student's academic performance comprehensively:\n\n");

        prompt.append("Student Information:\n");
        prompt.append("- Name: ").append(data.studentName).append("\n");
        prompt.append("- Grade: ").append(data.grade).append("\n\n");

        prompt.append("Academic Performance (Grades by Subject):\n");
        if (data.grades.isEmpty()) {
            prompt.append("- No grades recorded yet\n");
        } else {
            double totalMarks = 0;
            for (Map.Entry<String, Integer> entry : data.grades.entrySet()) {
                prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("%\n");
                totalMarks += entry.getValue();
            }
            double average = totalMarks / data.grades.size();
            prompt.append("\n Average Score: ").append(String.format("%.2f", average)).append("%\n");
        }

        prompt.append("\n Attendance: \n");
        prompt.append("- Attendance Rate: ").append(String.format("%.2f", data.attendancePercentage)).append("%\n");

        prompt.append("\n Teacher's Comments: \n");
        if (data.teacherComments.isEmpty()) {
            prompt.append("- No teacher comments recorded\n");
        } else {
            for (String comment : data.teacherComments) {
                prompt.append("- \"").append(comment).append("\"\n");
            }
        }

        prompt.append("\nProvide a very brief analysis with ONLY these three sections (no markdown formatting, plain text only):\n\n");

        prompt.append("BEHAVIOUR & LEARNING ISSUES\n");
        prompt.append("List any behavioral or learning issues identified from the data and comments.\n\n");

        prompt.append("SUGGESTED IMPROVEMENT ACTIONS\n");
        prompt.append("Provide specific, actionable recommendations for improvement.\n\n");

        prompt.append("ENCOURAGEMENT AND FEEDBACK MESSAGES\n");
        prompt.append("Write encouraging and positive feedback messages based on the student's performance.\n\n");

        prompt.append("Format response in plain text with section headers only. Do not use markdown, asterisks, hyphens, or special formatting.");


        return prompt.toString();
    }

    /**
     * Inner class to hold student performance data
     */
    private static class StudentPerformanceData {
        String studentName;
        int grade;
        Map<String, Integer> grades = new HashMap<>();
        double attendancePercentage;
        java.util.List<String> teacherComments = new java.util.ArrayList<>();
    }
}

