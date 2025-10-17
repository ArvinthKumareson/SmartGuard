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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smartguard.app.model.QuizResult
import com.smartguard.app.ui.*
import com.smartguard.app.ui.quiz.QuizScreen
import com.smartguard.app.ui.theme.SmartGuardTheme
import com.smartguard.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                100
            )
        }

        // ❌ Removed anonymous login to allow proper admin detection

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
            composable("tips") { TipsScreen(navController) }
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
                AdminQuizManagerScreen(navController)
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


@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SmartGuardTheme {
        AppNavigation()
    }
}
