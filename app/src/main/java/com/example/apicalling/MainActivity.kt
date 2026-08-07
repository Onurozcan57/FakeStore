package com.example.apicalling

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apicalling.ui.login.LoginScreen
import com.example.apicalling.ui.login.LoginViewModel
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.profile.ProfileScreen
import com.example.apicalling.ui.profile.ProfileViewModel
import com.example.apicalling.ui.theme.APIcallingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIcallingTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route
                        ) {
                            composable(Screen.Login.route) {
                                val viewModel: LoginViewModel = hiltViewModel()
                                LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = { name ->
                                        Toast.makeText(context, "Hoş geldin $name!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Screen.ProductList.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Screen.ProductList.route) {
                                // Şimdilik profil butonuna basınca oraya gitsin diye ProfileScreen koyalım
                                // İleride burası ProductList olacak
                                val viewModel: ProfileViewModel = hiltViewModel()
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onLogout = {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.ProductList.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
