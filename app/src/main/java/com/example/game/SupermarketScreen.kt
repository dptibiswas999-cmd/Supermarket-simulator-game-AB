package com.example.game

import com.example.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val coinPopups by viewModel.coinPopups.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Shelves, 1 = Decorations
    var showRenameDialog by remember { mutableStateOf(false) }
    var shopNameInput by remember { mutableStateOf(uiState.shopName) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showClaimDialog by remember { mutableStateOf(false) }
    var transactionInput by remember { mutableStateOf("") }

    val upiId = "8653157930-3@ibl"

    // Vibrant Palette Theme Colors
    val slate50 = Color(0xFFF8FAFC)
    val slate100 = Color(0xFFF1F5F9)
    val slate200 = Color(0xFFE2E8F0)
    val slate300 = Color(0xFFCBD5E1)
    val slate400 = Color(0xFF94A3B8)
    val slate500 = Color(0xFF64748B)
    val slate600 = Color(0xFF475569)
    val slate900 = Color(0xFF0F172A)

    val indigo50 = Color(0xFFEEF2FF)
    val indigo100 = Color(0xFFE0E7FF)
    val indigo200 = Color(0xFFC7D2FE)
    val indigo500 = Color(0xFF6366F1)
    val indigo600 = Color(0xFF4F46E5)
    val indigo700 = Color(0xFF4338CA)
    val indigo800 = Color(0xFF3730A3)
    val indigo900 = Color(0xFF1E1B4B)

    val violet700 = Color(0xFF6D28D9)

    val amber100 = Color(0xFFFEF3C7)
    val amber200 = Color(0xFFFDE68A)
    val amber500 = Color(0xFFF59E0B)
    val amber900 = Color(0xFF78350F)

    val goldAccent = Color(0xFFF59E0B)
    val profitGreen = Color(0xFF10B981)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(slate50),
        containerColor = slate50,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(slate50)
        ) {
            // 0. Custom Header replacing default TopAppBar to match the Vibrant Palette theme exactly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop rename / info clickable zone
                Column(
                    modifier = Modifier.clickable {
                        shopNameInput = uiState.shopName
                        showRenameDialog = true
                    }
                ) {
                    Text(
                        text = "MANAGER",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = slate500,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.shopName,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = indigo900
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("supermarket_title")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename shop",
                            tint = slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Coins counter with custom styling matching bg-amber-100 / text-amber-900 / border-amber-200
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(amber100, RoundedCornerShape(20.dp))
                        .border(1.dp, amber200, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AnimatedContent(
                        targetState = uiState.coins,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "CoinsCount"
                    ) { coins ->
                        Text(
                            text = String.format("%,d", coins),
                            style = TextStyle(
                                color = amber900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.testTag("coins_counter")
                        )
                    }
                }
            }

            // Inner content layout with consistent side paddings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Decorative storefront hero header banner
                Image(
                    painter = painterResource(id = R.drawable.img_supermarket_hero),
                    contentDescription = "Supermarket Storefront Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, slate200, RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Interactive 2D Supermarket Display Card (Rounded-[2.5rem] bg-white border-4 border-slate-200)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .border(4.dp, slate200, RoundedCornerShape(40.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        val w = maxWidth
                        val h = maxHeight

                        // Grid dimensions: We model a 3x3 layout.
                        val cols = 3
                        val rows = 3

                        val gridW = w / cols
                        val gridH = h / rows

                        // Background floor canvas to draw checkered tiles & radial background dots matching original CSS style
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height

                            val stepX = canvasW / cols
                            val stepY = canvasH / rows

                            // Draw beautiful, clean light checkered floor
                            for (c in 0 until cols) {
                                for (r in 0 until rows) {
                                    val isEven = (c + r) % 2 == 0
                                    drawRect(
                                        color = if (isEven) slate100 else Color.White,
                                        topLeft = Offset(c * stepX, r * stepY),
                                        size = Size(stepX, stepY)
                                    )
                                }
                            }

                            // Draw radial dots: background-image: radial-gradient(#6366f1 1px, transparent 1px) with size 20dp & 10% opacity style
                            val dotSpacingPx = 20.dp.toPx()
                            val dotRadiusPx = 2.dp.toPx()
                            var currentX = 0f
                            while (currentX < canvasW) {
                                var currentY = 0f
                                while (currentY < canvasH) {
                                    drawCircle(
                                        color = indigo500.copy(alpha = 0.08f),
                                        radius = dotRadiusPx,
                                        center = Offset(currentX, currentY)
                                    )
                                    currentY += dotSpacingPx
                                }
                                currentX += dotSpacingPx
                            }

                            // Drawing connecting guiding paths between cells
                            val pPath = Path().apply {
                                moveTo(0.5f * stepX, 0.5f * stepY) // Entrance
                                lineTo(1.5f * stepX, 1.5f * stepY) // Center
                                lineTo(2.5f * stepX, 2.5f * stepY) // Cashier
                            }
                            drawPath(
                                path = pPath,
                                color = indigo500.copy(alpha = 0.15f),
                                style = Stroke(width = 3f)
                            )
                        }

                    // Static Base Elements placement
                    // Slot 0,0: MAIN DOOR / ENTRANCE 🚪
                    Box(
                        modifier = Modifier
                            .size(gridW - 8.dp, gridH - 12.dp)
                            .offset(x = 0.dp, y = 0.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚪", fontSize = 28.sp)
                            Text(
                                "ENTRANCE",
                                fontSize = 8.sp,
                                color = slate500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Slot 2,2: 🧾 CASHIER TABLE & MANUAL TAP AREA
                    val isVipActive = uiState.isVip
                    val pulseInfinite = rememberInfiniteTransition(label = "pulse")
                    val scaleFactor by pulseInfinite.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(gridW - 8.dp, gridH - 12.dp)
                            .offset(x = (gridW.value * 2).dp, y = (gridH.value * 2).dp)
                            .padding(4.dp)
                            .shadow(if (isVipActive) 10.dp else 4.dp, RoundedCornerShape(16.dp))
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = if (isVipActive) 2.5.dp else 1.dp,
                                color = if (isVipActive) indigo600 else slate200,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                viewModel.triggerTapBonus()
                            }
                            .testTag("cashier_tap_zone"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(2.dp)
                                .rotate(if (isVipActive) (scaleFactor - 1.0f) * 100f else 0f)
                        ) {
                            Text("🧾🛎️", fontSize = 28.sp)
                            Text(
                                "TAP COUNTER",
                                fontSize = 8.sp,
                                color = indigo600,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isVipActive) "+🧪 100!" else "+🪙 30!",
                                fontSize = 8.sp,
                                color = slate600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Display Active Products Shelves inside grid coordinate spots
                    // Slots coordinates mapping: Veggie (0,1), Bakery (1,1), Dairy (2,1), Gadget (0,2), Boutique (1,2)
                    val slotsCoordinates = mapOf(
                        "SHELF_VEGGIE" to Pair(0, 1),
                        "SHELF_BAKERY" to Pair(1, 1),
                        "SHELF_DAIRY" to Pair(2, 1),
                        "SHELF_ELECTRONICS" to Pair(0, 2),
                        "SHELF_BOUTIQUE" to Pair(1, 2)
                    )

                    slotsCoordinates.forEach { (id, coord) ->
                        val shelfInfo = GameRegistry.SHELVES.find { it.id == id }!!
                        val shelfLevel = uiState.ownedShelves[id]
                        val shape = RoundedCornerShape(16.dp)

                        Box(
                            modifier = Modifier
                                .size(gridW - 6.dp, gridH - 10.dp)
                                .offset(x = (gridW.value * coord.first).dp, y = (gridH.value * coord.second).dp)
                                .padding(4.dp)
                                .shadow(if (shelfLevel != null) 3.dp else 0.dp, shape)
                                .background(
                                    color = if (shelfLevel != null) Color.White else slate100,
                                    shape = shape
                                )
                                .border(
                                    width = if (shelfLevel != null) 2.dp else 1.dp,
                                    color = if (shelfLevel != null) Color(android.graphics.Color.parseColor(shelfInfo.hexColor)) else slate300,
                                    shape = shape
                                )
                                .clickable {
                                    activeTab = 0 // open shelves catalog automatically
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = if (shelfLevel != null) shelfInfo.emoji else "🔒",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = shelfInfo.name.substringBefore("Shelf").substringBefore("Stand").substringBefore("Rack").substringBefore("Chiller").substringBefore("Counter").trim(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (shelfLevel != null) indigo900 else slate500,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (shelfLevel != null) {
                                    Text(
                                        text = "LV. $shelfLevel",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = goldAccent
                                    )
                                } else {
                                    Text(
                                        text = "₹ ${shelfInfo.baseCost}",
                                        fontSize = 8.sp,
                                        color = slate500,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Decorative elements placement
                    // Bonsai: Slot (1, 0), Neon Open: Slot (2, 0)
                    val decorCoordinates = mapOf(
                        "DECOR_BONSAI" to Pair(1, 0),
                        "DECOR_NEON" to Pair(2, 0)
                    )

                    decorCoordinates.forEach { (id, coord) ->
                        val decor = GameRegistry.DECORATIONS.find { it.id == id }!!
                        val owned = uiState.ownedDecorations.contains(id)
                        val shape = RoundedCornerShape(16.dp)

                        Box(
                            modifier = Modifier
                                .size(gridW - 6.dp, gridH - 10.dp)
                                .offset(x = (gridW.value * coord.first).dp, y = (gridH.value * coord.second).dp)
                                .padding(4.dp)
                                .shadow(if (owned) 3.dp else 0.dp, shape)
                                .background(
                                    color = if (owned) Color.White else slate100,
                                    shape = shape
                                )
                                .border(
                                    width = if (owned) 2.dp else 1.dp,
                                    color = if (owned) Color(android.graphics.Color.parseColor(decor.hexColor)) else slate300,
                                    shape = shape
                                )
                                .clickable { activeTab = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (owned) decor.emoji else "🌱+",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = decor.name.substringBefore("Decorative").substringBefore("Bright").substringBefore("Bonsai").substringBefore("Glossy").trim(),
                                    fontSize = 8.sp,
                                    color = if (owned) indigo900 else slate500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 2. Animated active customers path slider overlay
                    customers.forEach { customer ->
                        val shelfCoord = customer.targetShelfId?.let { slotsCoordinates[it] } ?: Pair(1, 1)

                        // Calculate custom coordinates based on client State
                        val startCoord = Pair(0.5f, 0.5f) // entrance
                        val checkoutCoord = Pair(2.2f, 2.2f) // cashier register block
                        val shelfCoordF = Pair(shelfCoord.first + 0.5f, shelfCoord.second + 0.5f)

                        val currentCoord = when (customer.state) {
                            CustomerState.ENTERING -> {
                                lerpPair(startCoord, shelfCoordF, customer.progress)
                            }
                            CustomerState.BROWSING -> {
                                shelfCoordF
                            }
                            CustomerState.CHECKOUT -> {
                                lerpPair(shelfCoordF, checkoutCoord, customer.progress)
                            }
                            CustomerState.LEAVING -> {
                                lerpPair(checkoutCoord, startCoord, customer.progress)
                            }
                        }

                        // Translate to actual offset coordinates
                        val xOffset = (currentCoord.first * gridW.value).dp
                        val yOffset = (currentCoord.second * gridH.value).dp

                        Box(
                            modifier = Modifier
                                .offset(x = xOffset - 18.dp, y = yOffset - 18.dp)
                                .size(36.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, indigo600, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customer.emoji,
                                fontSize = 18.sp
                            )

                            // Dialogue Speech Bubble overlay
                            customer.speechBubble?.let { quote ->
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-28).dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, indigo200, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .widthIn(max = 120.dp)
                                ) {
                                    Text(
                                        text = quote,
                                        color = indigo900,
                                        fontSize = 8.sp,
                                        maxLines = 2,
                                        lineHeight = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 3. Floating Cash Popups rendering
                    coinPopups.forEach { popup ->
                        val px = (popup.x * gridW.value).dp
                        val py = (popup.y * gridH.value).dp

                        Box(
                            modifier = Modifier
                                .offset(x = px - 30.dp, y = py - 30.dp - ((12 - popup.ageTicks) * 3).dp)
                                .background(Color.Transparent)
                        ) {
                            Text(
                                text = popup.text,
                                color = if (popup.text.contains("VIP")) goldAccent else profitGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black,
                                        blurRadius = 3f
                                    )
                                ),
                                modifier = Modifier.testTag("floating_coin_popup")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. VIP License Subscription Offer Banner (High-fidelity matching the Vibrant Palette spec)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("vip_subscription_banner"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(colors = listOf(Color(0xFF4F46E5), Color(0xFF6D28D9)))
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💎", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PRO BOOST",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Get 50,000 Coins + 50% permanent boost!",
                                color = indigo100,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                triggerUpiIntent(context, upiId)
                            }
                        ) {
                            Text(
                                text = "₹50",
                                color = indigo700,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPI: $upiId",
                            fontSize = 10.sp,
                            color = indigo100.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Show QR Code toggle
                        Button(
                            onClick = { showQrDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("show_qr_button")
                        ) {
                            Icon(Icons.Filled.QrCode, contentDescription = "QR", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Show QR", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Copy UPI ID button
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", upiId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("copy_upi_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy UPI", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Verify & Claim Button
                        Button(
                            onClick = { showClaimDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("verify_subscription_button")
                        ) {
                            Text("Claim 50K", fontSize = 9.sp, color = indigo700, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Upgrade Catalog Selector Navigation Bar Group styled beautifully in Light Theme (slate-100 base)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(slate100, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "Stock Shelves",
                    emoji = "🛒",
                    selected = activeTab == 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_shelves"),
                    onClick = { activeTab = 0 }
                )
                Spacer(modifier = Modifier.width(4.dp))
                TabButton(
                    text = "Decorations",
                    emoji = "✨",
                    selected = activeTab == 1,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_decorations"),
                    onClick = { activeTab = 1 }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Upgrade Option list item contents
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeTab == 0) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("shelves_list")
                    ) {
                        items(GameRegistry.SHELVES) { shelf ->
                            val currentLevel = uiState.ownedShelves[shelf.id]
                            val isOwned = currentLevel != null

                            // calculate next cost
                            val cost = if (isOwned) {
                                (shelf.baseCost * (currentLevel!! + 1) * 0.8).toLong()
                            } else {
                                shelf.baseCost
                            }
                            val canAfford = uiState.coins >= cost

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isOwned) Color(android.graphics.Color.parseColor(shelf.hexColor)).copy(alpha = 0.5f) else slate200,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(android.graphics.Color.parseColor(shelf.hexColor)).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(shelf.emoji, fontSize = 24.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = shelf.name,
                                                fontWeight = FontWeight.Bold,
                                                color = indigo900,
                                                fontSize = 13.sp
                                            )
                                            if (isOwned) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "LVL $currentLevel",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = amber900,
                                                    modifier = Modifier
                                                        .background(amber100, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = shelf.description,
                                            fontSize = 10.sp,
                                            color = slate600,
                                            lineHeight = 12.sp,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Earnings: 🪙 ${shelf.baseEarnings * (currentLevel ?: 1)} per ${shelf.earnIntervalTicks / 10}s",
                                            fontSize = 9.sp,
                                            color = profitGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = { viewModel.buyOrUpgradeShelf(shelf.id) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isOwned) goldAccent else indigo600,
                                            disabledContainerColor = slate200
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("buy_upgrade_${shelf.id}")
                                    ) {
                                        Text(
                                            text = if (isOwned) "Upgrade\n🪙 $cost" else "Buy\n🪙 $cost",
                                            fontSize = 8.sp,
                                            color = if (canAfford) Color.White else slate400,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("decorations_list")
                    ) {
                        items(GameRegistry.DECORATIONS) { decor ->
                            val alreadyOwned = uiState.ownedDecorations.contains(decor.id)
                            val canAfford = uiState.coins >= decor.cost

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (alreadyOwned) Color(android.graphics.Color.parseColor(decor.hexColor)).copy(alpha = 0.5f) else slate200,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(android.graphics.Color.parseColor(decor.hexColor)).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(decor.emoji, fontSize = 24.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = decor.name,
                                            fontWeight = FontWeight.Bold,
                                            color = indigo900,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = decor.description,
                                            fontSize = 10.sp,
                                            color = slate600,
                                            lineHeight = 12.sp,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row {
                                            if (decor.coinMultiplier > 1f) {
                                                Text(
                                                    text = "Earnings Multiplier: +${((decor.coinMultiplier - 1f) * 100).toInt()}% coins ",
                                                    fontSize = 8.5.sp,
                                                    color = profitGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (decor.speedMultiplier > 1f) {
                                                Text(
                                                    text = "Crowd Delay: -${((decor.speedMultiplier - 1f) * 100).toInt()}% spawn delay",
                                                    fontSize = 8.5.sp,
                                                    color = indigo600,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = { viewModel.buyDecoration(decor.id) },
                                        enabled = !alreadyOwned && canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = indigo600,
                                            disabledContainerColor = slate200
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("buy_decor_${decor.id}")
                                    ) {
                                        Text(
                                            text = if (alreadyOwned) "Placed" else "Buy\n🪙 ${decor.cost}",
                                            fontSize = 8.5.sp,
                                            color = if (alreadyOwned) slate500 else if (canAfford) Color.White else slate400,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Modal Renaming Dialog (Light Theme Styling)
    if (showRenameDialog) {
        AlertDialog(
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Custom Supermarket Name", color = indigo900, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = shopNameInput,
                    onValueChange = { shopNameInput = it },
                    singleLine = true,
                    label = { Text("Display Name", color = indigo600) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = indigo900,
                        unfocusedTextColor = indigo900,
                        focusedBorderColor = indigo600,
                        unfocusedBorderColor = slate300.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameShop(shopNameInput)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600),
                    modifier = Modifier.testTag("submit_name_button")
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = slate600)
                }
            }
        )
    }

    // Scan QR Dialog Overlay (Retro scan aesthetic - Light Theme Container)
    if (showQrDialog) {
        AlertDialog(
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = "Scan to Subscribe UPI VIP",
                    color = indigo900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Transfer ₹50 directly using any UPI App (GPay/PhonePe/Paytm/BHIM).",
                        fontSize = 11.sp,
                        color = slate600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Draw a highly distinct, customized visual QR code representation on Canvas
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(slate100, RoundedCornerShape(8.dp))
                            .border(1.dp, slate300, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw structured dark blocks mimicking a real QR matrix
                            val boxSize = w / 7f

                            // Left Top finder pattern
                            drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(boxSize * 3, boxSize * 3))
                            drawRect(color = Color.White, topLeft = Offset(boxSize, boxSize), size = Size(boxSize, boxSize))

                            // Right Top finder pattern
                            drawRect(color = Color.Black, topLeft = Offset(w - boxSize * 3, 0f), size = Size(boxSize * 3, boxSize * 3))
                            drawRect(color = Color.White, topLeft = Offset(w - boxSize * 2, boxSize), size = Size(boxSize, boxSize))

                            // Left Bottom finder pattern
                            drawRect(color = Color.Black, topLeft = Offset(0f, h - boxSize * 3), size = Size(boxSize * 3, boxSize * 3))
                            drawRect(color = Color.White, topLeft = Offset(boxSize, h - boxSize * 2), size = Size(boxSize, boxSize))

                            // Draw central styling mockup lines
                            drawRect(color = Color.Black, topLeft = Offset(boxSize * 4, boxSize * 2), size = Size(boxSize, boxSize))
                            drawRect(color = Color.Black, topLeft = Offset(boxSize * 2, boxSize * 4), size = Size(boxSize, boxSize * 2))
                            drawRect(color = Color.Black, topLeft = Offset(boxSize * 4, boxSize * 4), size = Size(boxSize * 2, boxSize))
                            drawRect(color = Color.Black, topLeft = Offset(boxSize * 5, boxSize * 5), size = Size(boxSize, boxSize))

                            // Add a cute tiny central star badge representing UPI
                            drawCircle(color = indigo900, radius = boxSize * 0.7f, center = Offset(w / 2f, h / 2f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "UPI ID: $upiId",
                        fontWeight = FontWeight.Bold,
                        color = indigo900,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("upi_id_text")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Amount: ₹50",
                        fontWeight = FontWeight.Bold,
                        color = profitGreen,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600),
                    modifier = Modifier.testTag("close_qr_button")
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Claim Voucher Reward Prompt Block
    if (showClaimDialog) {
        AlertDialog(
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            onDismissRequest = { showClaimDialog = false },
            title = {
                Text("Confirm Transfer & Claim Bonus", color = indigo900, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Claiming grants VIP Mogul title and 50,000 game coins. Please first pay ₹50 to upi 8653157930-3@ibl. Entering UPI reference increases verification credentials.",
                        fontSize = 11.sp,
                        color = slate600,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = transactionInput,
                        onValueChange = { transactionInput = it },
                        singleLine = true,
                        placeholder = { Text("e.g. UPI Ref (Optional)", color = slate400, fontSize = 12.sp) },
                        label = { Text("Transaction Reference", color = indigo600) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = indigo900,
                            unfocusedTextColor = indigo900,
                            focusedBorderColor = indigo600,
                            unfocusedBorderColor = slate300.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_reference_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.claimPremiumVip(transactionInput)
                        showClaimDialog = false
                        transactionInput = ""
                        Toast.makeText(context, "🎉 VIP Subscription Activated! Coins Added!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = indigo600),
                    modifier = Modifier.testTag("submit_claim_button")
                ) {
                    Text("Claim Reward", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialog = false }) {
                    Text("Close", color = slate600)
                }
            }
        )
    }
}

@Composable
fun TabButton(
    text: String,
    emoji: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backColor = if (selected) Color(0xFF4F46E5) else Color.Transparent
    val borderCol = if (selected) Color(0xFF4F46E5) else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backColor)
            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = if (selected) Color.White else Color(0xFF64748B)
            )
        }
    }
}

private fun lerpPair(start: Pair<Float, Float>, end: Pair<Float, Float>, fraction: Float): Pair<Float, Float> {
    val x = start.first + (end.first - start.first) * fraction
    val y = start.second + (end.second - start.second) * fraction
    return Pair(x, y)
}

private fun triggerUpiIntent(context: Context, upiId: String) {
    try {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", "Supermarket Simulator VIP")
            .appendQueryParameter("am", "50")
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", "VIP Subscription Upgrade")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val chooser = Intent.createChooser(intent, "Pay ₹50 on UPI App")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "No UPI applications found. Please transfer manually or scan QR.", Toast.LENGTH_LONG).show()
    }
}
