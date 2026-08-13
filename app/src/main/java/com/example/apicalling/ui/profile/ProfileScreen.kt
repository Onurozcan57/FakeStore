package com.example.apicalling.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.ui.theme.APIcallingTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel? = null,
    onLogout: () -> Unit = {},
    onFavoritesClick: () -> Unit = {}
) {
    val user = viewModel?.user?.value
    val sheetState = rememberModalBottomSheetState()
    var showCardSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Temiz beyaz arka plan
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Üst Kısım: Mavi Arka Planlı Header (Terminoloji: Themed Profile Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık: Hesabım (Ortada)
                Text(
                    text = "Hesabım",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Profil Kartı (Resim + İsim)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Yuvarlak Profil Fotoğrafı
                    Surface(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        AsyncImage(
                            model = user?.image ?: "https://i.pravatar.cc/150",
                            contentDescription = "Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Merhaba,",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (user != null) "${user.firstName} ${user.lastName}" else "Kullanıcı",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Orta Kısım: Menü Seçenekleri (Terminoloji: Interactive Menu List)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            ProfileMenuItem(
                title = "Siparişlerim",
                icon = Icons.Default.LocalMall,
                onClick = { /* TODO */ }
            )
            ProfileMenuItem(
                title = "Kuponlarım",
                icon = Icons.Default.ConfirmationNumber,
                onClick = { /* TODO */ }
            )
            ProfileMenuItem(
                title = "Kartlarım",
                icon = Icons.Default.CreditCard,
                onClick = { showCardSheet = true }
            )
            ProfileMenuItem(
                title = "Favorilerim",
                icon = Icons.Default.Favorite,
                onClick = onFavoritesClick
            )
            ProfileMenuItem(
                title = "Müşteri Hizmetleri",
                icon = Icons.Default.SupportAgent,
                onClick = { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Alt Kısım: Çıkış Yap Butonu
        Button(
            onClick = {
                viewModel?.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5F5F5), // Hafif gri buton
                contentColor = Color.Red // Çıkış vurgusu için kırmızı metin
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Çıkış Yap", fontWeight = FontWeight.Bold)
        }

        // Bottom Bar için güvenli boşluk
        Spacer(modifier = Modifier.height(110.dp))
    }

    // Terminoloji: Bank Card Bottom Sheet
    if (showCardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCardSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            user?.bank?.let { bank ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    // Header
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "Kayıtlı Kartlarım",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showCardSheet = false
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Black)
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Görsel Kart Tasarımı (Terminoloji: Premium Credit Card UI)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bank.cardType.uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Text(
                                text = bank.cardNumber.chunked(4).joinToString(" "),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "KART SAHİBİ",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "${user.firstName} ${user.lastName}".uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "SON KUL. TAR.",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = bank.cardExpire,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Kayıtlı kart bulunamadı.")
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // Terminoloji: Interaction Tracking
    // Tıklama durumunu izleyerek basılma efekti oluşturuyoruz
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Basıldığında arka plan hafif gri, bırakıldığında şeffaf/beyaz olur
    val backgroundColor = if (isPressed) Color(0xFFF5F5F5) else Color.Transparent

    Column(modifier = Modifier.background(backgroundColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Ripple'ı hala kapalı tutuyoruz, kendi efektimizi veriyoruz
                    onClick = onClick
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    APIcallingTheme {
        ProfileScreen()
    }
}
