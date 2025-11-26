package com.example.studentintelligentsystem.supabase.repository

import com.example.studentintelligentsystem.supabase.SupabaseClient
import com.example.studentintelligentsystem.supabase.models.Profile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Profile Repository
 * Handles all profile-related database operations
 */
class ProfileRepository {

    private val client = SupabaseClient.client

    /**
     * Get profile by user ID
     */
    suspend fun getProfile(userId: String): Result<Profile> {
        return withContext(Dispatchers.IO) {
            try {
                val profile = client.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<Profile>()
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get profile by student ID
     */
    suspend fun getProfileByStudentId(studentId: String): Result<Profile> {
        return withContext(Dispatchers.IO) {
            try {
                val profile = client.from("profiles")
                    .select {
                        filter {
                            eq("student_id", studentId)
                        }
                    }
                    .decodeSingle<Profile>()
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get all profiles by role
     */
    suspend fun getProfilesByRole(role: String): Result<List<Profile>> {
        return withContext(Dispatchers.IO) {
            try {
                val profiles = client.from("profiles")
                    .select {
                        filter {
                            eq("role", role)
                        }
                    }
                    .decodeList<Profile>()
                Result.success(profiles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update profile
     */
    suspend fun updateProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("profiles")
                    .update(updates) {
                        filter {
                            eq("id", userId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Create profile (usually triggered automatically, but can be used manually)
     */
    suspend fun createProfile(profile: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("profiles")
                    .insert(profile)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete profile
     */
    suspend fun deleteProfile(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("profiles")
                    .delete {
                        filter {
                            eq("id", userId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Search profiles by name
     */
    suspend fun searchProfiles(searchTerm: String): Result<List<Profile>> {
        return withContext(Dispatchers.IO) {
            try {
                val profiles = client.from("profiles")
                    .select {
                        filter {
                            ilike("full_name", "%$searchTerm%")
                        }
                    }
                    .decodeList<Profile>()
                Result.success(profiles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

