package com.example.apicalling.ui.coupon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.model.DiscountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponScreen(
    viewModel: CouponViewModel,
    onBackClick: () -> Unit,
    onUseCoupon: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kuponlarım", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(text = state.error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (state.coupons.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Henüz kuponunuz bulunmuyor.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.coupons) { coupon ->
                        CouponCard(coupon = coupon, onUse = { onUseCoupon(coupon.code) })
                    }
                }
            }
        }
    }
}

@Composable
fun CouponCard(coupon: Coupon, onUse: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = coupon.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = coupon.description, color = Color.Gray, fontSize = 12.sp)
                }
                if (coupon.isUsed) {
                    Badge(containerColor = Color.LightGray) {
                        Text("KULLANILDI", color = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Kupon Kodu", color = Color.Gray, fontSize = 10.sp)
                    Text(text = coupon.code, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                
                if (!coupon.isUsed) {
                    Button(
                        onClick = onUse,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Kullan")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Min. Tutar: ${coupon.minimumAmount}$",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Sön Gün: ${coupon.expiresAt}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun size(dp: androidx.compose.ui.unit.Dp) = Modifier.size(dp)
