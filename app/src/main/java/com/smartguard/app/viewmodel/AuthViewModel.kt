package com.smartguard.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.firestore
import com.smartguard.app.data.EncryptedKeywords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    private val appContext = app.applicationContext

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        Log.d("Auth", "login() called with email=$email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Log.d("Auth", "onComplete triggered")
                if (task.isSuccessful) {
                    val user = task.result.user
                    Log.d("Auth", "Login success. UID: ${user?.uid}")
                    _currentUser.value = user
                    _isLoggedIn.value = user != null
                    
                    // Sync keywords immediately on login and restart real-time sync
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("Auth", "Syncing keywords after login...")
                            EncryptedKeywords.syncFromFirestore(appContext)
                            Log.d("Auth", "Keywords synced successfully")
                        } catch (e: Exception) {
                            Log.e("Auth", "Failed to sync keywords after login: ${e.message}")
                        }
                    }
                    
                    // Restart real-time listener for this user session
                    EncryptedKeywords.startRealtimeSync(appContext)
                    Log.d("Auth", "Real-time keyword sync restarted")
                    
                    onResult(true, user?.uid)
                } else {
                    Log.e("Auth", "Login failed: ${task.exception?.message}")
                    onResult(false, null)
                }
            }
    }
    fun checkAdminStatus(onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false)
            return
        }

        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                onResult(role == "admin")
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun signup(email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    
                    // Update user profile with display name
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d("Auth", "User profile updated with name: $name")
                                _currentUser.value = auth.currentUser // Refresh to get updated displayName
                                _isLoggedIn.value = true
                                
                                // Sync keywords after signup
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        Log.d("Auth", "Syncing keywords after signup...")
                                        EncryptedKeywords.syncFromFirestore(appContext)
                                        Log.d("Auth", "Keywords synced successfully")
                                    } catch (e: Exception) {
                                        Log.e("Auth", "Failed to sync keywords after signup: ${e.message}")
                                    }
                                }
                                
                                // Start real-time sync
                                EncryptedKeywords.startRealtimeSync(appContext)
                                Log.d("Auth", "Real-time keyword sync started")
                                
                                onResult(true, null)
                            } else {
                                Log.e("Auth", "Failed to update profile: ${profileTask.exception?.message}")
                                onResult(true, null) // Still successful signup, just profile update failed
                            }
                        }
                } else {
                    val message = mapFirebaseSignupError(task.exception)
                    Log.e("Auth", "Signup failed: ${task.exception?.message}")
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    onResult(false, message)
                }
            }
    }

    private fun mapFirebaseSignupError(ex: Exception?): String {
        return when (ex) {
            is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
            is FirebaseAuthUserCollisionException -> "Email already in use"
            is FirebaseAuthException -> when (ex.errorCode) {
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                else -> ex.localizedMessage ?: "Signup failed"
            }
            else -> ex?.localizedMessage ?: "Signup failed"
        }
    }

    fun logout() {
        Log.d("Auth", "Logging out, stopping keyword sync...")
        EncryptedKeywords.stopRealtimeSync()
        auth.signOut()
        _currentUser.value = null
        _isLoggedIn.value = false
    }
}
