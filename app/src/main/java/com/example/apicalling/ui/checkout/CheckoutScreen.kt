package com.example.apicalling.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.AsyncImage
import com.example.apicalling.data.model.AddressDto
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.ui.cart.CartBottomBar
import com.example.apicalling.ui.cart.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    cartItems: List<CartItem>,
    discount: Double,
    appliedCoupon: Coupon?,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var isDetailExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(state.paymentErrorMessage) {
        state.paymentErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FakeStore", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            if (state.paymentStatus == PaymentStatus.THREE_DS_REQUIRED || state.paymentStatus == PaymentStatus.VERIFYING_OTP) {
                // 3DS Bekleme Ekranı (Daha profesyonel)
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("3D Secure Doğrulaması", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lütfen telefonunuza gönderilen 6 haneli doğrulama kodunu giriniz. (Test Kodu: 123456)",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = state.otp,
                        onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
                        label = { Text("Doğrulama Kodu") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.verifyOTP() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = state.otp.length == 6 && state.paymentStatus != PaymentStatus.VERIFYING_OTP
                    ) {
                        if (state.paymentStatus == PaymentStatus.VERIFYING_OTP) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Doğrula ve Öde", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ödeme ID: ${state.paymentId}", fontSize = 11.sp, color = Color.LightGray)
                }
            } else if (state.paymentStatus == PaymentStatus.SUCCESS) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalMall, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Siparişiniz Başarıyla Alındı!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBackClick) { Text("Ana Sayfaya Dön") }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Teslimat Adresi Section
                    CheckoutSection(
                        title = "Teslimat Adresi",
                        trailingIcon = if (!state.isAddingNewAddress) Icons.Default.Add else null,
                        onTrailingIconClick = { viewModel.addNewAddressMode() }
                    ) {
                        if (state.isAddingNewAddress) {
                            AddressForm(
                                address = state.addressForm,
                                onAddressChange = { viewModel.onAddressFormChange(it) },
                                onSave = { viewModel.saveAddress() },
                                onCancel = { viewModel.cancelAddNewAddress() },
                                showCancel = state.addresses.isNotEmpty()
                            )
                        } else {
                            AddressList(
                                addresses = state.addresses,
                                selectedId = state.selectedAddressId,
                                onSelect = { viewModel.selectAddress(it) }
                            )
                        }
                    }

                    // 3. Ödeme Seçenekleri Box
                    CheckoutSection(title = "Ödeme Seçenekleri") {
                        PaymentForm(
                            cardNumber = state.cardNumber,
                            cvv = state.cvv,
                            month = state.expiryMonth,
                            year = state.expiryYear,
                            onCardInfoChange = { n, c, m, y -> viewModel.updateCardInfo(n, c, m, y) }
                        )
                    }

                    // 4. Teslimat Zamanı Box
                    CheckoutSection(title = "Teslimat Zamanı") {
                        DeliverySummary(cartItems = cartItems)
                    }

                    Spacer(modifier = Modifier.height(150.dp))
                }
            }
        }

        // Terminoloji: Top-Level Snackbar Host
        // Z-Index hiyerarşisinde en üstte olması için Box'ın son elemanı olarak ekledik.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }

        // 5. Floating Checkout Bar
        if (cartItems.isNotEmpty() && state.paymentStatus != PaymentStatus.SUCCESS && 
            state.paymentStatus != PaymentStatus.THREE_DS_REQUIRED && state.paymentStatus != PaymentStatus.VERIFYING_OTP) {
            val isEnabled = viewModel.isFormValid(cartItems.map { it.product }) && state.paymentStatus != PaymentStatus.LOADING
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CartBottomBar(
                    totalPrice = cartItems.sumOf { it.product.price * it.quantity },
                    discount = discount,
                    isExpanded = isDetailExpanded,
                    onExpandClick = { isDetailExpanded = !isDetailExpanded },
                    buttonText = if (state.paymentStatus == PaymentStatus.LOADING) "İşleniyor..." else "Siparişi Onayla",
                    isEnabled = isEnabled, // Aktiflik durumu buraya geçti
                    onCheckoutClick = { if (isEnabled) viewModel.confirmOrder(cartItems, discount, appliedCoupon) }
                )
            }
        }
    }
}

@Composable
fun CheckoutSection(
    title: String, 
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                if (trailingIcon != null) {
                    IconButton(onClick = onTrailingIconClick, modifier = Modifier.size(24.dp)) {
                        Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AddressList(
    addresses: List<AddressDto>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        addresses.forEach { addr ->
            val isSelected = addr.id == selectedId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(addr.id) }
                    .border(
                        1.dp, 
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RadioBox Tasarımı
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = addr.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${addr.neighborhood} ${addr.street} No:${addr.building} D:${addr.apartmentNo} ${addr.district}/${addr.city}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddressForm(
    address: AddressDto, 
    onAddressChange: (AddressDto) -> Unit, 
    onSave: () -> Unit,
    onCancel: () -> Unit,
    showCancel: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = address.title,
            onValueChange = { onAddressChange(address.copy(title = it)) },
            label = { Text("Adres Başlığı (Örn: Evim, İş)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = address.city,
                onValueChange = { onAddressChange(address.copy(city = it)) },
                label = { Text("İl") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = address.district,
                onValueChange = { onAddressChange(address.copy(district = it)) },
                label = { Text("İlçe") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        OutlinedTextField(
            value = address.neighborhood,
            onValueChange = { onAddressChange(address.copy(neighborhood = it)) },
            label = { Text("Mahalle") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = address.street,
            onValueChange = { onAddressChange(address.copy(street = it)) },
            label = { Text("Sokak / Cadde") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = address.building, 
                onValueChange = { onAddressChange(address.copy(building = it)) }, 
                label = { Text("Bina") }, 
                modifier = Modifier.weight(1f), 
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = address.floor, 
                onValueChange = { onAddressChange(address.copy(floor = it)) }, 
                label = { Text("Kat") }, 
                modifier = Modifier.weight(1f), 
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = address.apartmentNo, 
                onValueChange = { onAddressChange(address.copy(apartmentNo = it)) }, 
                label = { Text("Daire") }, 
                modifier = Modifier.weight(1f), 
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (showCancel) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Vazgeç")
                }
            }
            Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PaymentForm(
    cardNumber: String, 
    cvv: String, 
    month: String, 
    year: String, 
    onCardInfoChange: (String, String, String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Terminoloji: Masked Input / Visual Transformation
        // Kart numarası: 16 hane sınırı, sayısal klavye ve 4 hanede bir boşluk.
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { 
                val digits = it.filter { char -> char.isDigit() }
                if (digits.length <= 16) {
                    onCardInfoChange(digits, cvv, month, year)
                }
            },
            label = { Text("Kart Numarası") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CardNumberVisualTransformation()
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Ay: 2 hane sınırı ve sayısal klavye
            OutlinedTextField(
                value = month,
                onValueChange = { 
                    val digits = it.filter { char -> char.isDigit() }
                    if (digits.length <= 2) {
                        onCardInfoChange(cardNumber, cvv, digits, year)
                    }
                },
                label = { Text("Ay") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Yıl: 2 hane sınırı ve sayısal klavye
            OutlinedTextField(
                value = year,
                onValueChange = { 
                    val digits = it.filter { char -> char.isDigit() }
                    if (digits.length <= 2) {
                        onCardInfoChange(cardNumber, cvv, month, digits)
                    }
                },
                label = { Text("Yıl") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // CVV: 3 hane sınırı ve sayısal klavye
            OutlinedTextField(
                value = cvv,
                onValueChange = { 
                    val digits = it.filter { char -> char.isDigit() }
                    if (digits.length <= 3) {
                        onCardInfoChange(cardNumber, digits, month, year)
                    }
                },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

/**
 * Terminoloji: Custom Visual Transformation
 * Kart numarasını 4000 1234 5678 9012 formatında göstermek için kullanılır.
 */
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 4 == 3 && i != 15) out += " "
        }

        val creditCardOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }

        return androidx.compose.ui.text.input.TransformedText(AnnotatedString(out), creditCardOffsetMapping)
    }
}

@Composable
fun DeliverySummary(cartItems: List<CartItem>) {
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val isFreeShipping = subtotal >= 50.0
    cartItems.take(1).forEach { item ->
        val product = item.product
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                AsyncImage(model = product.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Tahmini Teslimat: 2-3 İş Günü", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "FakeStore Güvencesiyle", fontSize = 11.sp, color = Color.Gray)
            }
            if (isFreeShipping) {
                Text(text = "Kargo Bedava", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Bottom))
            }
        }
    }
}
