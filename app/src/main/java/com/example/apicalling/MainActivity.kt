package com.example.apicalling

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apicalling.domain.repository.FavoriteRepository
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.ui.login.LoginScreen
import com.example.apicalling.ui.login.LoginViewModel
import com.example.apicalling.ui.main.MainScreen
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.theme.APIcallingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Oturum yönetimini enjekte ediyoruz
    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var favoriteRepository: FavoriteRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIcallingTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // Başlangıç rotasını kullanıcı durumuna göre seçiyoruz (Terminoloji: Session Routing)
                val startRoute = if (sessionRepository.isLoggedIn()) Screen.Main.route else Screen.Login.route
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startRoute
                    ) {
                        composable(Screen.Login.route) {
                            val viewModel: LoginViewModel = hiltViewModel()
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { name ->
                                    Toast.makeText(context, "Hoş geldin $name!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Main.route) {
                            MainScreen(
                                onLogout = {
                                    sessionRepository.clearSession() // Hafızayı temizle
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Main.route) { inclusive = true }
                                    }
                                },
                                favoriteRepository = favoriteRepository
                            )
                        }
                    }
                }
            }
        }
    }
}
