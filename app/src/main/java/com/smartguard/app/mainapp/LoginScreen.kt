package com.smartguard.app.mainapp

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.mainapp.resources.GradientButton
import com.smartguard.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignup by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_smartguard),
                    contentDescription = "SmartGuard Logo",
                    modifier = Modifier
                        .size(190.dp)
                        .padding(top = 32.dp)
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(120.dp))

                    Text(
                        text = if (isSignup) "Create Account" else "Login",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isSignup) {
                        Text("Name", color = Color.White, modifier = Modifier.align(Alignment.Start))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = Color.White,
                                cursorColor = Color.Black,
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    Text("Email", color = Color.White, modifier = Modifier.align(Alignment.Start))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White,
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Text("Password", color = Color.White, modifier = Modifier.align(Alignment.Start))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White,
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 100.dp)
                    )

                    GradientButton(
                        text = if (isSignup) "Sign Up" else "Login",
                        onClick = {
                            Log.d("Login", "Calling vm.login with email=$email")
                            if (isSignup) {
                                vm.signup(email.trim(), password, name) { success ->
                                    if (success) nav.navigate("home")
                                    else errorMessage = "Signup failed"
                                }
                            } else {
                                val isValidEmail =
                                    Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                                if (!isValidEmail) {
                                    errorMessage = "Invalid email format"
                                    return@GradientButton
                                }
                                vm.login(email.trim(), password) { success, uid ->
                                    Log.d("Login", "Login result: success=$success, uid=$uid")
                                    if (success && uid != null) {
                                        Firebase.firestore.collection("users").document(uid).get()
                                            .addOnSuccessListener { doc ->
                                                val role = doc.getString("role")
                                                Log.d("Login", "Role fetched: $role")
                                                val target =
                                                    if (role == "admin") "admin" else "home"
                                                Log.d("Login", "Navigating to: $target")
                                                nav.navigate(target) {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Log.e(
                                                    "Login",
                                                    "Firestore fetch failed: ${it.message}"
                                                )
                                                errorMessage = "Failed to fetch role"
                                            }
                                    } else {
                                        Log.e("Login", "Login failed or UID null")
                                        errorMessage = "Login failed"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { isSignup = !isSignup }) {
                        Text(
                            if (isSignup) "Already have an account? Login" else "New user? Sign up",
                            color = Color.White
                        )
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
