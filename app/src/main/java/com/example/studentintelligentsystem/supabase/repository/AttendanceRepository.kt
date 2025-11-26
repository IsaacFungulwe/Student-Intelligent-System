package com.example.studentintelligentsystem.supabase.repository

import com.example.studentintelligentsystem.supabase.SupabaseClient
import com.example.studentintelligentsystem.supabase.models.Attendance
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Attendance Repository
 * Handles all attendance-related database operations
 */
class AttendanceRepository {

    private val client = SupabaseClient.client

    /**
     * Get all attendance records for a student
     */
    suspend fun getStudentAttendance(studentId: String): Result<List<Attendance>> {
        return withContext(Dispatchers.IO) {
            try {
                val attendance = client.from("attendance")
                    .select {
                        filter {
                            eq("student_id", studentId)
                        }
                    }
                    .decodeList<Attendance>()
                Result.success(attendance)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get attendance for a specific subject and student
     */
    suspend fun getAttendanceBySubject(studentId: String, subjectId: String): Result<List<Attendance>> {
        return withContext(Dispatchers.IO) {
            try {
                val attendance = client.from("attendance")
                    .select {
                        filter {
                            eq("student_id", studentId)
                            eq("subject_id", subjectId)
                        }
                    }
                    .decodeList<Attendance>()
                Result.success(attendance)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get attendance for a subject on a specific date
     */
    suspend fun getAttendanceByDate(subjectId: String, date: String): Result<List<Attendance>> {
        return withContext(Dispatchers.IO) {
            try {
                val attendance = client.from("attendance")
                    .select {
                        filter {
                            eq("subject_id", subjectId)
                            eq("attendance_date", date)
                        }
                    }
                    .decodeList<Attendance>()
                Result.success(attendance)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mark attendance
     */
    suspend fun markAttendance(attendance: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("attendance")
                    .insert(attendance)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mark attendance for multiple students
     */
    suspend fun markBulkAttendance(attendanceList: List<Map<String, Any>>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("attendance")
                    .insert(attendanceList)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update attendance record
     */
    suspend fun updateAttendance(attendanceId: String, updates: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("attendance")
                    .update(updates) {
                        filter {
                            eq("id", attendanceId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete attendance record
     */
    suspend fun deleteAttendance(attendanceId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.from("attendance")
                    .delete {
                        filter {
                            eq("id", attendanceId)
                        }
                    }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get attendance statistics for a student
     */
    suspend fun getAttendanceStats(studentId: String, subjectId: String): Result<Map<String, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val attendance = client.from("attendance")
                    .select {
                        filter {
                            eq("student_id", studentId)
                            eq("subject_id", subjectId)
                        }
                    }
                    .decodeList<Attendance>()

                val stats = mapOf(
                    "present" to attendance.count { it.status == "present" },
                    "absent" to attendance.count { it.status == "absent" },
                    "late" to attendance.count { it.status == "late" },
                    "excused" to attendance.count { it.status == "excused" },
                    "total" to attendance.size
                )

                Result.success(stats)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

