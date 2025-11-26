package com.example.studentintelligentsystem.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Profile Model
 * Represents a user profile (student, teacher, parent, admin)
 */
@Serializable
data class Profile(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "student",
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Subject Model
 * Represents a subject/course
 */
@Serializable
data class Subject(
    val id: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("subject_name") val subjectName: String,
    val description: String? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Enrollment Model
 * Represents student enrollment in a subject
 */
@Serializable
data class Enrollment(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("enrollment_date") val enrollmentDate: String? = null,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Result Model
 * Represents student exam/assignment results
 */
@Serializable
data class Result(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("exam_type") val examType: String,
    @SerialName("marks_obtained") val marksObtained: Double,
    @SerialName("total_marks") val totalMarks: Double,
    val percentage: Double? = null,
    val grade: String? = null,
    @SerialName("exam_date") val examDate: String,
    val remarks: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Attendance Model
 * Represents student attendance record
 */
@Serializable
data class Attendance(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("attendance_date") val attendanceDate: String,
    val status: String, // present, absent, late, excused
    val remarks: String? = null,
    @SerialName("marked_by") val markedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Announcement Model
 * Represents system announcements
 */
@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("target_role") val targetRole: String? = "all",
    @SerialName("is_important") val isImportant: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * AI Analysis Model
 * Represents AI-generated analysis results
 */
@Serializable
data class AIAnalysis(
    val id: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("analysis_type") val analysisType: String,
    @SerialName("analysis_data") val analysisData: String, // JSON string
    val recommendations: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Parent-Student Relationship Model
 */
@Serializable
data class ParentStudentRelationship(
    val id: String,
    @SerialName("parent_id") val parentId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("relationship_type") val relationshipType: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Student Performance Summary
 * View model for analytics
 */
@Serializable
data class StudentPerformanceSummary(
    @SerialName("student_id") val studentId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("student_id") val studentIdNumber: String? = null,
    @SerialName("total_subjects") val totalSubjects: Int,
    @SerialName("average_percentage") val averagePercentage: Double?,
    @SerialName("days_present") val daysPresent: Int,
    @SerialName("days_absent") val daysAbsent: Int,
    @SerialName("attendance_percentage") val attendancePercentage: Double?
)

