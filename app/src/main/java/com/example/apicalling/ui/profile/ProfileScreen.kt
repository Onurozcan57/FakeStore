package com.example.apicalling.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apicalling.ui.theme.APIcallingTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel? = null,
    onLogout: () -> Unit = {}
) {
    val user = viewModel?.user?.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Üst bar padding'i kaldırıldığı için buraya ekledik
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Kısmı (TopBar yerine daha yukarıda durması için manuel ekledik)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profilim",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            IconButton(onClick = {
                viewModel?.logout()
                onLogout()
            }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış Yap")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profil Resmi (API'den gelen resmi Coil ile yüklüyoruz)
        if (user?.image != null) {
            AsyncImage(
                model = user.image,
                contentDescription = "Profil Resmi",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Resim yoksa veya yüklenirken varsayılan ikon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        user?.let {
            Text(
                text = "${it.firstName} ${it.lastName}",
                style = MaterialTheme.typography.titleLarge,
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
            it.bank?.let { bank ->
                InfoCard(title = "Banka & Kart Bilgileri") {
                    InfoRow(label = "Kart Numarası", value = bank.cardNumber)
                    InfoRow(label = "Kart Tipi", value = bank.cardType)
                    InfoRow(label = "Son Kullanma", value = bank.cardExpire)
                    InfoRow(label = "Para Birimi", value = bank.currency)
                }
            }
        } ?: run {
            Text("Kullanıcı bilgisi bulunamadı.")
        }
        
        // Bottom Bar padding'i için güvenli boşluk
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
