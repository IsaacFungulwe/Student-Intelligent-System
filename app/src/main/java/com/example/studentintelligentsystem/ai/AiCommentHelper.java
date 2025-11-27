package com.example.studentintelligentsystem.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.studentintelligentsystem.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI helper for generating intelligent comments using Google Gemini API
 */
public class AiCommentHelper {
    private static final String TAG = "AiCommentHelper";
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface CommentCallback {
        void onCommentGenerated(String comment);
        void onError(String error);
    }

    /**
     * Generate an AI-powered comment based on student performance using Google Gemini
     */
    public void generateComment(String studentName, String subject, int marks, CommentCallback callback) {
        executor.execute(() -> {
            try {
                String apiKey = BuildConfig.GEMINI_API_KEY;

                if (apiKey == null || apiKey.isEmpty()) {
                    mainHandler.post(() -> callback.onError("Gemini API key not configured"));
                    return;
                }

                // Create a contextual prompt
                String prompt = String.format(
                    "Generate a brief, encouraging teacher's comment (max 50 words) for %s who scored %d%% in %s. " +
                    "Be specific, constructive, and professional. Focus on what the student did well if marks are above 50%%, " +
                    "or areas for improvement if below 50%%. Return only the comment text without any formatting.",
                    studentName, marks, subject
                );

                // Build Gemini API request payload
                JSONObject payload = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                payload.put("contents", contents);

                // Add generation config for better control
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 150);
                payload.put("generationConfig", generationConfig);

                // Build URL with API key
                String urlWithKey = GEMINI_ENDPOINT + "?key=" + apiKey;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlWithKey).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Gemini API response code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    Log.d(TAG, "Gemini API response: " + response.toString());

                    // Parse Gemini response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray candidates = jsonResponse.optJSONArray("candidates");

                    if (candidates != null && candidates.length() > 0) {
                        JSONObject candidate = candidates.getJSONObject(0);
                        JSONObject contentObj = candidate.optJSONObject("content");
                        if (contentObj != null) {
                            JSONArray partsArray = contentObj.optJSONArray("parts");
                            if (partsArray != null && partsArray.length() > 0) {
                                JSONObject partObj = partsArray.getJSONObject(0);
                                String text = partObj.optString("text", "").trim();
                                if (!text.isEmpty()) {
                                    final String finalComment = text;
                                    mainHandler.post(() -> callback.onCommentGenerated(finalComment));
                                    return;
                                }
                            }
                        }
                    }

                    mainHandler.post(() -> callback.onError("No comment generated from Gemini"));
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    String errorMsg = "Gemini API error: " + responseCode + " - " + errorResponse.toString();
                    Log.e(TAG, errorMsg);
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception generating AI comment", e);
                mainHandler.post(() -> callback.onError("Exception: " + e.getMessage()));
            }
        });
    }

    /**
     * Generate a fallback comment based on marks (used when AI is unavailable)
     */
    public static String generateFallbackComment(int marks, String subject) {
        if (marks >= 80) {
            return "Excellent performance in " + subject + "! Keep up the outstanding work.";
        } else if (marks >= 70) {
            return "Good work in " + subject + ". Continue putting in effort.";
        } else if (marks >= 60) {
            return "Satisfactory performance. There's room for improvement in " + subject + ".";
        } else if (marks >= 50) {
            return "Average performance. Please dedicate more time to " + subject + ".";
        } else {
            return "Needs improvement in " + subject + ". Additional support recommended.";
        }
    }
}

