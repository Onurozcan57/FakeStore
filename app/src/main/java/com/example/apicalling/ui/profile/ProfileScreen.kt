package com.example.apicalling.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apicalling.ui.theme.APIcallingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel? = null,
    onLogout: () -> Unit = {}
) {
    val user = viewModel?.user?.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilim") },
                actions = {
                    IconButton(onClick = {
                        viewModel?.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış Yap")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Resmi Yerine İkon (Şimdilik)
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            user?.let {
                Text(
                    text = "${it.firstName} ${it.lastName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Kişisel Bilgiler Kartı
                InfoCard(title = "Kişisel Bilgiler") {
                    InfoRow(label = "Kullanıcı Adı", value = it.username)
                    InfoRow(label = "E-posta", value = it.email)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banka Bilgileri Kartı (Ödeme kısmı için önemli)
                InfoCard(title = "Banka & Kart Bilgileri") {
                    InfoRow(label = "Kart Numarası", value = it.bank.cardNumber)
                    InfoRow(label = "Kart Tipi", value = it.bank.cardType)
                    InfoRow(label = "Son Kullanma", value = it.bank.cardExpire)
                    InfoRow(label = "Para Birimi", value = it.bank.currency)
                }
            } ?: run {
                Text("Kullanıcı bilgisi bulunamadı.")
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    APIcallingTheme {
        ProfileScreen()
    }
}
