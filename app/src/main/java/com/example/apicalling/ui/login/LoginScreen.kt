package com.example.apicalling.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apicalling.ui.theme.APIcallingTheme

/**
 * Giriş ekranı tasarımı.
 * ViewModel'den gelen state'e göre (loading, error, success) tepki verir.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel? = null, // Preview için opsiyonel yaptık
    onLoginSuccess: (String) -> Unit = {} // Başarılı girişte yapılacak işlem
) {
    // ViewModel'deki state'i dinliyoruz
    val state = viewModel?.state?.value ?: LoginState()
    
    // Kullanıcı giriş alanları için yerel state'ler
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Başarılı giriş durumunda bir kez tetiklenmesi için LaunchedEffect
    LaunchedEffect(state.successUser) {
        state.successUser?.let {
            onLoginSuccess(it.firstName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hoş Geldiniz",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Kullanıcı Adı Alanı
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Şifre Alanı
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Giriş Butonu
        Button(
            onClick = { viewModel?.login(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Giriş Yap")
            }
        }

        // Hata Mesajı Gösterimi
        state.error?.let {
            Spacer(modifier = Modifier.height(30 .dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    APIcallingTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    // Sadece UI'ı test etmek için boş bir state simülasyonu
    APIcallingTheme {
        // Not: Gerçek hayatta burada MockViewModel kullanılabilir
        Surface {
             // Loading durumunu temsil eden manuel bir görünüm
        }
    }
}
