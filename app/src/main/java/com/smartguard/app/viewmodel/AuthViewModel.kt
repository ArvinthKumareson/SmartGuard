package com.smartguard.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

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

    fun signup(email: String, password: String, name: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val user = auth.currentUser
                _currentUser.value = user
                _isLoggedIn.value = user != null
                onResult(task.isSuccessful)
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _isLoggedIn.value = false
    }
}
