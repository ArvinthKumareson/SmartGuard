@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.google.firebase.auth.UserProfileChangeRequest
import com.smartguard.app.viewmodel.AuthViewModel
import com.smartguard.app.R

@Composable
fun ProfileScreen(nav: NavController, vm: AuthViewModel) {
    val user = vm.currentUser.collectAsState().value
    var newName by remember { mutableStateOf(user?.displayName ?: "") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_profile),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { SmartGuardBottomBar(nav, currentRoute = "profile") },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Text("Email: ${user?.email ?: "N/A"}", color = Color.White)
                Spacer(Modifier.height(24.dp))

                Text("Display Name", color = Color.White)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                GradientButton("Update Name", onClick = {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            message = if (task.isSuccessful) "Name updated!" else "Failed to update name"
                        }
                })

                Spacer(Modifier.height(32.dp))

                Text("Current Password", color = Color.White)
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Text("New Password", color = Color.White)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                GradientButton("Change Password", onClick = {
                    val email = user?.email
                    if (email != null) {
                        vm.login(email, oldPassword) { success, _ ->
                            if (success) {
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { task ->
                                        message = if (task.isSuccessful) "Password updated!" else "Failed to update password"
                                    }
                            } else {
                                message = "Current password is incorrect"
                            }
                        }
                    }
                })

                message?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = Color(0xFFDA22FF))
                }

                Spacer(Modifier.height(32.dp))
                GradientButton("Sign Out", onClick = {
                    vm.logout()
                    nav.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
