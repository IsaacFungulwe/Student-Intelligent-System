package com.example.studentintelligentsystem.supabase.auth

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import com.example.studentintelligentsystem.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Authentication Manager
 * Handles user authentication with Supabase
 */
class AuthManager {

    private val client = SupabaseClient.client

    /**
     * Sign up a new user
     */
    suspend fun signUp(email: String, password: String, fullName: String, role: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = mapOf(
                        "full_name" to fullName,
                        "role" to role
                    )
                }
                Result.success("Sign up successful! Please check your email for verification.")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign in an existing user
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = client.auth.currentUserOrNull()?.id
                Result.success(userId ?: "")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.signOut()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return try {
            client.auth.currentUserOrNull()?.email
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }

    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update user password
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                client.auth.updateUser {
                    password = newPassword
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

