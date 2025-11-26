package com.example.studentintelligentsystem.supabase.repository

import com.example.studentintelligentsystem.supabase.SupabaseClient
import com.example.studentintelligentsystem.supabase.models.Result
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Results Repository
 * Handles all student results/grades database operations
 */
class ResultRepository {

    private val client = SupabaseClient.client

    /**
     * Get all results for a student
     */
    suspend fun getStudentResults(studentId: String): kotlin.Result<List<Result>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = client.from("results")
                    .select {
                        filter {
                            eq("student_id", studentId)
                        }
                    }
                    .decodeList<Result>()
                kotlin.Result.success(results)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Get results for a specific subject and student
     */
    suspend fun getResultsBySubject(studentId: String, subjectId: String): kotlin.Result<List<Result>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = client.from("results")
                    .select {
                        filter {
                            eq("student_id", studentId)
                            eq("subject_id", subjectId)
                        }
                    }
                    .decodeList<Result>()
                kotlin.Result.success(results)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Get all results for a subject (teacher view)
     */
    suspend fun getSubjectResults(subjectId: String): kotlin.Result<List<Result>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = client.from("results")
                    .select {
                        filter {
                            eq("subject_id", subjectId)
                        }
                    }
                    .decodeList<Result>()
                kotlin.Result.success(results)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Add new result
     */
    suspend fun addResult(result: Map<String, Any>): kotlin.Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("results")
                    .insert(result)
                kotlin.Result.success(Unit)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Update result
     */
    suspend fun updateResult(resultId: String, updates: Map<String, Any>): kotlin.Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("results")
                    .update(updates) {
                        filter {
                            eq("id", resultId)
                        }
                    }
                kotlin.Result.success(Unit)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Delete result
     */
    suspend fun deleteResult(resultId: String): kotlin.Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("results")
                    .delete {
                        filter {
                            eq("id", resultId)
                        }
                    }
                kotlin.Result.success(Unit)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }

    /**
     * Get results by exam type
     */
    suspend fun getResultsByExamType(studentId: String, examType: String): kotlin.Result<List<Result>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = client.from("results")
                    .select {
                        filter {
                            eq("student_id", studentId)
                            eq("exam_type", examType)
                        }
                    }
                    .decodeList<Result>()
                kotlin.Result.success(results)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
    }
}

