package com.example.studentintelligentsystem.supabase;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Supabase Authentication Manager
 * Handles login by querying Supabase database for user credentials
 */
public class SupabaseAuthManager {
    private static final String TAG = "SupabaseAuthManager";

    public SupabaseAuthManager() {
        // Empty constructor
    }

    /**
     * Login user by querying Supabase
     * @param email User's email
     * @param password User's plaintext password (will be hashed)
     * @param role User role: Admin, Teacher, or Parent
     * @return JSONObject with user data if login successful, null otherwise
     */
    public JSONObject login(String email, String password, String role) {
        if (!SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Supabase not configured");
            return null;
        }

        try {
            // Hash password to match stored hash
            String hashedPassword = hashPassword(password);

            // Determine table name based on role
            String tableName = getTableNameForRole(role);
            if (tableName == null) {
                Log.e(TAG, "Invalid role: " + role);
                return null;
            }

            Log.d(TAG, "Attempting login for " + role + " with email: " + email);

            // Query Supabase for user
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/" + tableName
                + "?email=eq." + email
                + "&password_hash=eq." + hashedPassword
                + "&select=*";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                // Parse response
                JSONArray users = new JSONArray(response.toString());

                if (users.length() > 0) {
                    JSONObject user = users.getJSONObject(0);
                    Log.i(TAG, "✓ Login successful for " + role + ": " + email);
                    return user;
                } else {
                    Log.w(TAG, "✗ Login failed: Invalid credentials for " + role);
                    return null;
                }

            } else {
                Log.e(TAG, "✗ Login failed: HTTP " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get table name based on user role
     */
    private String getTableNameForRole(String role) {
        switch (role) {
            case "Admin":
                return "admins";
            case "Teacher":
                return "teachers";
            case "Parent":
                return "parents";
            default:
                return null;
        }
    }

    /**
     * Hash password using SHA-256
     * Must match the hashing in DatabaseHelper
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }
}

