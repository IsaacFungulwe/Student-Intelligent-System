package com.example.studentintelligentsystem.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Supabase Client Singleton
 * Provides a single instance of the Supabase client throughout the app
 */
object SupabaseClient {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    // Helper to check if client is initialized properly
    fun isInitialized(): Boolean {
        return try {
            client.supabaseUrl.isNotEmpty()
            true
        } catch (e: Exception) {
            false
        }
    }
}

