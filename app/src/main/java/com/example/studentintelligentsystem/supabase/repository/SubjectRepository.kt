package com.example.studentintelligentsystem.supabase.repository

import com.example.studentintelligentsystem.supabase.SupabaseClient
import com.example.studentintelligentsystem.supabase.models.Subject
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Subject Repository
 * Handles all subject-related database operations
 */
class SubjectRepository {

    private val client = SupabaseClient.client

    /**
     * Get all subjects
     */
    suspend fun getAllSubjects(): Result<List<Subject>> {
        return withContext(Dispatchers.IO) {
            try {
                val subjects = client.from("subjects")
                    .select()
                    .decodeList<Subject>()
                Result.success(subjects)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get subject by ID
     */
    suspend fun getSubject(subjectId: String): Result<Subject> {
        return withContext(Dispatchers.IO) {
            try {
                val subject = client.from("subjects")
                    .select {
                        filter {
                            eq("id", subjectId)
                        }
                    }
                    .decodeSingle<Subject>()
                Result.success(subject)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get subjects by teacher
     */
    suspend fun getSubjectsByTeacher(teacherId: String): Result<List<Subject>> {
        return withContext(Dispatchers.IO) {
            try {
                val subjects = client.from("subjects")
                    .select {
                        filter {
                            eq("teacher_id", teacherId)
                        }
                    }
                    .decodeList<Subject>()
                Result.success(subjects)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Create new subject
     */
    suspend fun createSubject(subject: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("subjects")
                    .insert(subject)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update subject
     */
    suspend fun updateSubject(subjectId: String, updates: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("subjects")
                    .update(updates) {
                        filter {
                            eq("id", subjectId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete subject
     */
    suspend fun deleteSubject(subjectId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("subjects")
                    .delete {
                        filter {
                            eq("id", subjectId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

