package com.smartguard.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartguard.app.data.EncryptedKeywords
import com.smartguard.app.model.QuizResult
import com.smartguard.app.mainapp.*
import com.smartguard.app.mainapp.admin.AdminFeedbackScreen
import com.smartguard.app.mainapp.admin.AdminHomeScreen
import com.smartguard.app.mainapp.admin.AdminKeywordManagerScreen
import com.smartguard.app.mainapp.admin.AdminQuizManagerScreen
import com.smartguard.app.mainapp.quiz.QuizScreen
import com.smartguard.app.mainapp.theme.SmartGuardTheme
import com.smartguard.app.mainapp.user.*
import com.smartguard.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request runtime permissions
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }

        // ✅ Sync keywords safely
        lifecycleScope.launch {
            try {
                EncryptedKeywords.syncFromFirestore(applicationContext)
            } catch (e: Exception) {
                Log.e("SmartGuard", "Keyword sync failed", e)
            }
        }

        setContent {
            SmartGuardTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    val context = LocalContext.current

    if (permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }) {
        ActivityCompat.requestPermissions(context as Activity, permissions, 100)
    }


    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            startDestination = "login"
        } else {
            try {
                authViewModel.checkAdminStatus { isAdmin ->
                    startDestination = if (isAdmin) "admin" else "home"
                }
            } catch (e: Exception) {
                Log.e("SmartGuard", "Admin check failed", e)
                startDestination = "home"
            }
        }
    }

    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("home") { HomeScreen(navController, authViewModel) }
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("profile") { ProfileScreen(navController, authViewModel) }
            composable("tips") { CourseDashboardScreen(navController) }
            composable("quiz") { QuizScreen(navController) }
            composable("scam chat") { ScamChatGameScreen(navController) }
            composable("website_checker") { WebsiteCheckerScreen(navController) }
            composable("scan_history") { ScanHistoryScreen(navController) }
            composable("user_feedback") { UserFeedbackScreen(navController) }
            composable("history") { HistoryScreen(navController) }
            
            composable(
                route = "courseDetail/{courseTitle}",
                arguments = listOf(navArgument("courseTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseTitle = backStackEntry.arguments?.getString("courseTitle") ?: ""
                CourseDetailScreen(navController, courseTitle)
            }

            composable("admin") {
                AdminHomeScreen(navController, onLogout = { logout(navController) })
            }
            composable("admin_keywords") {
                AdminKeywordManagerScreen(navController)
            }
            composable("admin_quiz_manager") {
                AdminQuizManagerScreen(navController)
            }
            composable("admin_feedback") {
                AdminFeedbackScreen(navController)
            }

            composable(
                route = "quizOverview?resultsJson={resultsJson}",
                arguments = listOf(navArgument("resultsJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val json = backStackEntry.arguments?.getString("resultsJson") ?: ""
                val type = object : TypeToken<List<QuizResult>>() {}.type
                val results = try {
                    Gson().fromJson<List<QuizResult>>(java.net.URLDecoder.decode(json, "UTF-8"), type)
                } catch (e: Exception) {
                    Log.e("SmartGuard", "Failed to parse quiz results", e)
                    emptyList()
                }
                QuizOverviewScreen(results) {
                    navController.popBackStack()
                }
            }
        }
    } else {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
    }
}

fun logout(nav: NavController) {
    Firebase.auth.signOut()
    nav.navigate("login") {
        popUpTo("admin") { inclusive = true }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SmartGuardTheme {
        AppNavigation()
    }
}
