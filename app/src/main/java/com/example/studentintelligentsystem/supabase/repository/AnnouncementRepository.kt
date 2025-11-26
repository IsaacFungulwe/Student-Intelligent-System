package com.example.studentintelligentsystem.supabase.repository

import com.example.studentintelligentsystem.supabase.SupabaseClient
import com.example.studentintelligentsystem.supabase.models.Announcement
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Announcement Repository
 * Handles all announcement-related database operations
 */
class AnnouncementRepository {

    private val client = SupabaseClient.client

    /**
     * Get all announcements
     */
    suspend fun getAllAnnouncements(): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val announcements = client.from("announcements")
                    .select()
                    .decodeList<Announcement>()
                Result.success(announcements)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get announcements by target role
     */
    suspend fun getAnnouncementsByRole(role: String): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val announcements = client.from("announcements")
                    .select {
                        filter {
                            or {
                                eq("target_role", role)
                                eq("target_role", "all")
                            }
                        }
                    }
                    .decodeList<Announcement>()
                Result.success(announcements)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get important announcements
     */
    suspend fun getImportantAnnouncements(): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val announcements = client.from("announcements")
                    .select {
                        filter {
                            eq("is_important", true)
                        }
                    }
                    .decodeList<Announcement>()
                Result.success(announcements)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Create new announcement
     */
    suspend fun createAnnouncement(announcement: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("announcements")
                    .insert(announcement)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update announcement
     */
    suspend fun updateAnnouncement(announcementId: String, updates: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("announcements")
                    .update(updates) {
                        filter {
                            eq("id", announcementId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete announcement
     */
    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("announcements")
                    .delete {
                        filter {
                            eq("id", announcementId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get announcements by author
     */
    suspend fun getAnnouncementsByAuthor(authorId: String): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val announcements = client.from("announcements")
                    .select {
                        filter {
                            eq("author_id", authorId)
                        }
                    }
                    .decodeList<Announcement>()
                Result.success(announcements)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

