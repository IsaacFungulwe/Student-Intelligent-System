package com.example.studentintelligentsystem.supabase;

import com.example.studentintelligentsystem.BuildConfig;

/**
 * Supabase Configuration
 * Contains the Supabase URL and API key from BuildConfig
 */
public class SupabaseConfig {
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // Validate configuration
    public static boolean isConfigured() {
        return SUPABASE_URL != null && !SUPABASE_URL.isEmpty() &&
               SUPABASE_ANON_KEY != null && !SUPABASE_ANON_KEY.isEmpty();
    }
}

