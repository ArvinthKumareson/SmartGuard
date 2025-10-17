package com.smartguard.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartguard.app.model.QuizResult
import com.smartguard.app.mainapp.*
import com.smartguard.app.mainapp.admin.AdminHomeScreen
import com.smartguard.app.mainapp.admin.AdminQuizManagerScreen
import com.smartguard.app.mainapp.quiz.QuizScreen
import com.smartguard.app.mainapp.theme.SmartGuardTheme
import com.smartguard.app.mainapp.user.HistoryScreen
import com.smartguard.app.mainapp.user.HomeScreen
import com.smartguard.app.mainapp.user.ProfileScreen
import com.smartguard.app.mainapp.user.QuizOverviewScreen
import com.smartguard.app.mainapp.user.ScamChatGameScreen
import com.smartguard.app.mainapp.user.ScenarioScreen
import com.smartguard.app.mainapp.user.TipsScreen
import com.smartguard.app.viewmodel.AuthViewModel
import com.smartguard.app.mainapp.user.CourseDashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                100
            )
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
    val currentUser = authViewModel.currentUser.collectAsState().value

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser == null || currentUser.isAnonymous) {
            startDestination = "login"
        } else {
            authViewModel.checkAdminStatus { isAdmin ->
                startDestination = if (isAdmin) "admin" else "home"
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
            composable("scenarios") { ScenarioScreen(navController) }
            composable("history") { HistoryScreen(navController) }

            composable(
                route = "quizOverview?resultsJson={resultsJson}",
                arguments = listOf(navArgument("resultsJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val json = backStackEntry.arguments?.getString("resultsJson") ?: ""
                val type = object : TypeToken<List<QuizResult>>() {}.type
                val results = Gson().fromJson<List<QuizResult>>(java.net.URLDecoder.decode(json, "UTF-8"), type)
                QuizOverviewScreen(results) { navController.popBackStack() }
            }

            composable("admin") {
                AdminHomeScreen(navController, onLogout = { logout(navController) })
            }

            composable("admin_quiz_manager") {
                AdminQuizManagerScreen(navController)
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
                    emptyList()
                }

                QuizOverviewScreen(results) {
                    navController.popBackStack()
                }
            }

        }
    } else {
        // Optional: show loading spinner while role is being checked
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
