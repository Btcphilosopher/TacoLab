package com.example.ui.tacolab

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.TacoLabViewModel
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TacoLabMainScreen(
    viewModel: TacoLabViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LAB",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "TACOLAB 2030",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    // Quick stats pill
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.ElectricBolt, "Active Hub", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Text("CORE ACTIVE", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Role switcher tab bar matching system specs
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentRole == "CUSTOMER",
                    onClick = { viewModel.setRole("CUSTOMER") },
                    label = { Text("Customer") },
                    icon = { Icon(Icons.Filled.Fastfood, "Customer app view") },
                    modifier = Modifier.testTag("nav_customer")
                )
                NavigationBarItem(
                    selected = currentRole == "KITCHEN_KDS",
                    onClick = { viewModel.setRole("KITCHEN_KDS") },
                    label = { Text("KDS Monitor") },
                    icon = { Icon(Icons.Filled.Monitor, "Kitchen screen View") },
                    modifier = Modifier.testTag("nav_kds")
                )
                NavigationBarItem(
                    selected = currentRole == "STAFF_ADMIN",
                    onClick = { viewModel.setRole("STAFF_ADMIN") },
                    label = { Text("Staff/Dispatch") },
                    icon = { Icon(Icons.Filled.People, "Employee portal View") },
                    modifier = Modifier.testTag("nav_staff")
                )
                NavigationBarItem(
                    selected = currentRole == "ARCHITECT_SPECS",
                    onClick = { viewModel.setRole("ARCHITECT_SPECS") },
                    label = { Text("Blueprints") },
                    icon = { Icon(Icons.Filled.Terminal, "Architecture Blueprint Specifications") },
                    modifier = Modifier.testTag("nav_blueprints")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Transitions between interfaces
            AnimatedContent(
                targetState = currentRole,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "role_reveal"
            ) { role ->
                when (role) {
                    "CUSTOMER" -> CustomerDashboard(viewModel)
                    "KITCHEN_KDS" -> KitchenKdsDashboard(viewModel)
                    "STAFF_ADMIN" -> StaffAdminDashboard(viewModel)
                    "ARCHITECT_SPECS" -> ArchitectSpecsDashboard()
                }
            }

            // Snackbar notification system
            snackbarMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearMessage() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. CUSTOMER DASHBOARD
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerDashboard(viewModel: TacoLabViewModel) {
    val loyalty by viewModel.loyaltyProfile.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val cartItems by viewModel.cart.collectAsStateWithLifecycle()
    val favMeals by viewModel.favoriteMeals.collectAsStateWithLifecycle()
    val customItem by viewModel.customizingItem.collectAsStateWithLifecycle()
    val aiRecommend by viewModel.aiRecommendation.collectAsStateWithLifecycle()
    val dietLimit by viewModel.selectedDietType.collectAsStateWithLifecycle()
    val weatherState by viewModel.simulatedWeather.collectAsStateWithLifecycle()

    var showCartDialog by remember { mutableStateOf(false) }
    var showDriveThruSim by remember { mutableStateOf(false) }
    var showTableQrSim by remember { mutableStateOf(false) }
    var showLocatorSim by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Tacos", "Burritos", "Quesadillas", "Nachos", "Bowls", "Fries", "Drinks", "Desserts")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Loyalty Card Widget with Streaks (Gamified Rewards Engine)
        item {
            loyalty?.let { activeLoyalty ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background gradient
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TACO REWARDS CENTER",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = activeLoyalty.tier.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = when (activeLoyalty.tier) {
                                            "Taco Legend" -> MaterialTheme.colorScheme.primary
                                            "Taco Elite" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${activeLoyalty.pointsBalance} PTS",
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Points ProgressBar
                            if (activeLoyalty.tier != "Taco Legend") {
                                val progressRatio = activeLoyalty.pointsBalance.toFloat() / (activeLoyalty.pointsBalance + activeLoyalty.pointsToNextTier)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = { progressRatio },
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Earn ${activeLoyalty.pointsToNextTier} pts more to level up!",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "Next Tier: ${getNextTier(activeLoyalty.tier)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "🏆 Max Tier Unlocked! VIP Exclusive drops are active.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Streak Counter / Referral metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.LocalFireDepartment, "Streak", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "${activeLoyalty.orderStreakCount} Daily Streak",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.People, "Referrals", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "${activeLoyalty.referralsCount} Referrals",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Simulation Controls & Sub Navigation Desks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Table QR Order System
                Button(
                    onClick = { showTableQrSim = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_desk_table_qr")
                ) {
                    Icon(Icons.Filled.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Table QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ObsidianDark)
                }

                // Drive Thru Console
                Button(
                    onClick = { showDriveThruSim = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_desk_drive_thru")
                ) {
                    Icon(Icons.Filled.DirectionsCar, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Drive-Thru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Store Locator
                Button(
                    onClick = { showLocatorSim = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_desk_locator")
                ) {
                    Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stores", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // AI RECOMENDATION ENGINE (Bento design)
        item {
            aiRecommend?.let { rec ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PERSONALIZED AI RECOMMENDATION",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_reco_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = rec.tagline.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text(weatherState, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = rec.title,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = rec.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", rec.item.price)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${rec.item.calories} kcal",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Customizer override
                                    IconButton(
                                        onClick = { viewModel.setCustomizingItem(rec.item) },
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, "Customize", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = { viewModel.quickOrderMeal(rec.item, "PICKUP") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("ai_reco_quick_order")
                                    ) {
                                        Icon(Icons.Filled.FlashOn, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quick Buy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Diet Preference Tuning chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Filter Limit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoolGrey)
                        listOf("All (No Limit)", "Vegan", "Keto", "High Protein").forEach { diet ->
                            val isSelected = dietLimit == diet
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(50))
                                    .clickable { viewModel.setDietLimit(diet) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = diet.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // MAIN MENU SECTION & FILTER CATEGORIES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EXPLORE MENU",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                // Category scroll row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Button(
                            onClick = { selectedCategory = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dynamic Menu Items Grid Box
        val filteredMenuItems = if (selectedCategory == "All") menuItems else menuItems.filter { it.category == selectedCategory }
        if (filteredMenuItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items available in this category.",
                        color = CoolGrey,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredMenuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_item_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Circle Food Icon mockup with specific custom emojis representing fast food
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val foodEmoji = when (item.category) {
                                    "Tacos" -> "🌮"
                                    "Burritos" -> "🌯"
                                    "Quesadillas" -> "🫓"
                                    "Nachos" -> "🌽"
                                    "Bowls" -> "🥣"
                                    "Fries" -> "🍟"
                                    "Drinks" -> "🥤"
                                    "Desserts" -> "🍩"
                                    else -> "🍲"
                                }
                                Text(foodEmoji, fontSize = 32.sp)
                            }

                            // Meta info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (item.isAvailable) MaterialTheme.colorScheme.onBackground else CoolGrey,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = CoolGrey,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", item.basePrice)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${item.calories} kcal",
                                        fontSize = 11.sp,
                                        color = CoolGrey
                                    )
                                }
                            }

                            // Dynamic checkout helper button
                            if (item.isAvailable) {
                                Button(
                                    onClick = { viewModel.applyDefaultCustomization(item) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .height(38.dp)
                                ) {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Sold Out grey overlay shadow screen
                        if (!item.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "SOLD OUT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CreamyWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorites Quick-Order section
        if (favMeals.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FAVORITE LAB MEALS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favMeals) { fav ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable {
                                        // Instantly Quick order
                                        val converters = TacoLabConverters()
                                        val itemsList = converters.fromCustomizedItemList(fav.customizationJson)
                                        if (itemsList.isNotEmpty()) {
                                            viewModel.quickOrderMeal(itemsList[0], "PICKUP")
                                        }
                                    },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = fav.mealName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteFavoriteMeal(fav.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(fav.baseItemName, fontSize = 11.sp, color = CoolGrey, maxLines = 1)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$${String.format("%.2f", fav.price)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Quick Reorder",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Floating Cart indicator button
    if (cartItems.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = { showCartDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("floating_cart_button")
            ) {
                Icon(Icons.Filled.ShoppingCart, "Shopping Cart")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${cartItems.size} ORDER ITEMS",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CreamyWhite)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$${String.format("%.2f", cartItems.sumOf { it.price })}",
                        fontSize = 11.sp,
                        color = ObsidianDark,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

    // CUSTOMIZER DIALOG (Deep Customisation Layer)
    customItem?.let { item ->
        Dialog(onDismissRequest = { viewModel.setCustomizingItem(null) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .testTag("customizer_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Title Header info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TACO CUSTOMIZER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { viewModel.setCustomizingItem(null) }) {
                            Icon(Icons.Filled.Close, null, tint = CreamyWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.baseItemName,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = CreamyWhite
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Price & Calorie indicator row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonCard)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DYNAMIC REAL-TIME PRICE", fontSize = 9.sp, color = CoolGrey)
                            Text(
                                "$${String.format("%.2f", item.price)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.testTag("customizer_live_price")
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("CALORIE ESTIMATE", fontSize = 9.sp, color = CoolGrey)
                            Text(
                                "${item.calories} kcal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("customizer_live_calories")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Choices list scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Category 1: Proteins
                        if (item.category != "Drinks" && item.category != "Desserts" && item.category != "Fries") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("SELECT PROTEIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Beef", "Chicken", "Pork", "Vegan", "None").forEach { prot ->
                                        val isSel = item.selectedProtein == prot
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary else CarbonCard)
                                                .clickable { viewModel.updateCustomizingProtein(prot) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                prot,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) ObsidianDark else CreamyWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Category 2: Spices
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("SPICE LEVEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Mild", "Medium", "Hot", "Inferno").forEach { spc ->
                                        val isSel = item.selectedSpice == spc
                                        val spcColor = when (spc) {
                                            "Inferno" -> MaterialTheme.colorScheme.primary
                                            "Hot" -> QuesoGold
                                            else -> LimeAgave
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, if (isSel) spcColor else Color.Transparent, RoundedCornerShape(8.dp))
                                                .background(if (isSel) spcColor.copy(alpha = 0.25f) else CarbonCard)
                                                .clickable { viewModel.updateCustomizingSpice(spc) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                spc,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) spcColor else CreamyWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Category 3: Fresh Salsas
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("SELECT SALSAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Pico", "Salsa Verde", "Fire Salsa").forEach { sls ->
                                        val isSel = item.selectedSalsas.contains(sls)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else CarbonCard)
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleCustomizingSalsa(sls) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                sls,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) MaterialTheme.colorScheme.secondary else CreamyWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Category 4: Cheeses
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("CHEESE SELECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Cheddar", "Cotija", "Queso Sauce", "None").forEach { chs ->
                                        val isSel = item.selectedCheese == chs
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else CarbonCard)
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.tertiary else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { viewModel.updateCustomizingCheese(chs) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                chs,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) MaterialTheme.colorScheme.tertiary else CreamyWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Category 5: Gourmet Extras (Price additions)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("GOURMET EXTRAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                val extraOptions = listOf(
                                    Pair("Guac", "+$1.99"),
                                    Pair("Jalapeños", "+$0.30"),
                                    Pair("Beans", "+$0.49"),
                                    Pair("Rice", "+$0.49"),
                                    Pair("Crema", "+$0.39")
                                )
                                FlowRow(
                                    maxItemsInEachRow = 3,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    extraOptions.forEach { opt ->
                                        val isSel = item.selectedExtras.contains(opt.first)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else CarbonCard)
                                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleCustomizingExtra(opt.first) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                "${opt.first} (${opt.second})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) MaterialTheme.colorScheme.primary else CreamyWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Category 6: Wraps
                            if (item.category == "Tacos" || item.category == "Burritos" || item.category == "Quesadillas") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("TORTILLA TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("Flour Tortilla", "Corn Tortilla", "Bowl (None)").forEach { wrp ->
                                            val isSel = item.selectedTortilla == wrp
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else CarbonCard)
                                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.updateCustomizingTortilla(wrp) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    wrp.replace(" (None)", ""),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSel) MaterialTheme.colorScheme.primary else CreamyWhite
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "This standard retail item is already perfectly optimized for speed in our lab. No additional customizable matrices are required.",
                                fontSize = 13.sp,
                                color = CoolGrey,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Allergen warning alert
                    Text(
                        "⚠️ ALLERGEN NOTICE: Prepared in environments containing trace wheat-gluten, pasteurized cow milk/lactose, and organic soybean oils.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save as favorite field + add to cart triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var favName by remember { mutableStateOf("") }
                        var savingMode by remember { mutableStateOf(false) }

                        if (savingMode) {
                            OutlinedTextField(
                                value = favName,
                                onValueChange = { favName = it },
                                placeholder = { Text("E.g. Spicy Cheatday", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedTextColor = CreamyWhite,
                                    focusedTextColor = CreamyWhite
                                )
                            )

                            Button(
                                onClick = {
                                    if (favName.isNotBlank()) {
                                        viewModel.saveFavoriteMeal(item, favName)
                                        savingMode = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Save", color = ObsidianDark)
                            }
                        } else {
                            Button(
                                onClick = { savingMode = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Filled.FavoriteBorder, "Add to favorited list", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = { viewModel.addCustomizedToCart() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_add_to_cart_final")
                        ) {
                            Icon(Icons.Filled.ShoppingCart, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock Customization", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // CART SUMMARY DIALOG
    if (showCartDialog && cartItems.isNotEmpty()) {
        Dialog(onDismissRequest = { showCartDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .testTag("cart_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ORDER LIST",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Alex's Order Tally",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = CreamyWhite
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(cartItems) { index, cartItem ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CarbonCard)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cartItem.baseItemName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = CreamyWhite
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val customSummary = mutableListOf<String>()
                                        if (cartItem.selectedProtein != "None") customSummary.add(cartItem.selectedProtein)
                                        if (cartItem.selectedSpice != "Medium") customSummary.add(cartItem.selectedSpice)
                                        if (cartItem.selectedCheese != "Cheddar") customSummary.add(cartItem.selectedCheese)
                                        customSummary.addAll(cartItem.selectedExtras)
                                        customSummary.add(cartItem.selectedTortilla.replace(" (None)", ""))

                                        Text(
                                            customSummary.joinToString(", "),
                                            fontSize = 11.sp,
                                            color = CoolGrey,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${cartItem.calories} kcal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "$${String.format("%.2f", cartItem.price)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeFromCart(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Checkout billing breakdown
                    val subtotal = cartItems.sumOf { it.price }
                    val ptsEarnable = (subtotal * 15).toInt()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order Subtotal", fontSize = 12.sp, color = CoolGrey)
                                Text("$${String.format("%.2f", subtotal)}", fontSize = 12.sp, color = CreamyWhite)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Frictionless Lab Tax (0.0%)", fontSize = 12.sp, color = CoolGrey)
                                Text("$0.00", fontSize = 12.sp, color = CreamyWhite)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = ObsidianDark)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("TOTAL COST", fontWeight = FontWeight.Black, fontSize = 16.sp, color = CreamyWhite)
                                Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(
                                "⚡ Will earn you +$ptsEarnable Rewards points!",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout choices (Drive-Thru, Delivery, Dine-In, Store Pickup)
                    var selectedType by remember { mutableStateOf("PICKUP") }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SELECT DISPATCH PATHWAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("PICKUP", "DRIVE_THRU", "DINE_IN", "DELIVERY").forEach { type ->
                                val isSel = selectedType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) MaterialTheme.colorScheme.primary else CarbonCard)
                                        .clickable { selectedType = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        type.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSel) ObsidianDark else CreamyWhite
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Loyalty Points Redeem check
                    var redeemLoyaltySelected by remember { mutableStateOf(false) }
                    val userPoints = loyalty?.pointsBalance ?: 0
                    val canRedeem = userPoints >= 300 // e.g. 300 pts gets free meal!

                    if (userPoints > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canRedeem) { redeemLoyaltySelected = !redeemLoyaltySelected },
                            border = BorderStroke(1.dp, if (redeemLoyaltySelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = redeemLoyaltySelected && canRedeem,
                                    onCheckedChange = { if (canRedeem) redeemLoyaltySelected = it },
                                    enabled = canRedeem,
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                                )
                                Column {
                                    Text(
                                        "REDEEM REWARDS POINTS FOR 100% OFF",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (canRedeem) MaterialTheme.colorScheme.secondary else CoolGrey
                                    )
                                    Text(
                                        if (canRedeem) "Cost: 300 Loyalty Points. You have $userPoints pts." else "Unlock free tacos once you hit 300 points (Currently: $userPoints pts).",
                                        fontSize = 10.sp,
                                        color = CoolGrey
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit ordering checkout button
                    Button(
                        onClick = {
                            viewModel.checkoutCart(
                                orderType = selectedType,
                                redeemPoints = redeemLoyaltySelected && canRedeem,
                                pointsCost = 300
                            )
                            showCartDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_cart_checkout_submit")
                    ) {
                        Icon(Icons.Filled.CreditCard, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (redeemLoyaltySelected && canRedeem) "SUBMIT WITH REWARDS" else "PAY SECURELY ($${String.format("%.2f", subtotal)})",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // DRIVE-THRU MODAL CONSOLE
    if (showDriveThruSim) {
        Dialog(onDismissRequest = { showDriveThruSim = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("drive_thru_dialog")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DRIVE-THRU HUD PORTAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { showDriveThruSim = false }) {
                            Icon(Icons.Filled.Close, null, tint = CreamyWhite)
                        }
                    }

                    Text("Futuristic Drive-Thru Mode", fontWeight = FontWeight.Black, fontSize = 20.sp, color = CreamyWhite)
                    Text(
                        "Our 2030 smart drive-thru centers use autonomous license plate scanners and RFID tracking. Pre-order, join the virtual lane cue, grab your food, and drive straight off!",
                        fontSize = 12.sp,
                        color = CoolGrey,
                        lineHeight = 16.sp
                    )

                    // Virtual queue tracker simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonCard)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.Sensors, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                    Text("Virtual Lane Queue Check", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("AUTO ON", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Divider(color = ObsidianDark)

                            listOf(
                                Pair("Aproaching sensor zone", "SUCCESS"),
                                Pair("Scanning QR signature", "SUCCESS"),
                                Pair("Vehicle plate validation", "VALID"),
                                Pair("Lane target assignment", "LANE B8")
                            ).forEach { step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(step.first, fontSize = 12.sp, color = CoolGrey)
                                    Text(step.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // QR display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Large QR representation
                            Icon(
                                Icons.Filled.QrCode,
                                null,
                                tint = ObsidianDark,
                                modifier = Modifier.size(140.dp)
                            )
                            Text(
                                "TACOLAB PASS QR-CODE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ObsidianDark
                            )
                            Text(
                                "Scan this at the Speaker Post or Window-Mojo",
                                fontSize = 10.sp,
                                color = ObsidianDark.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Button(
                        onClick = { showDriveThruSim = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Keep QR Active", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // TABLE QR SCAN SIMULATOR
    if (showTableQrSim) {
        Dialog(onDismissRequest = { showTableQrSim = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("table_qr_dialog")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "IN-RESTAURANT TABLE ORDERING",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { showTableQrSim = false }) {
                            Icon(Icons.Filled.Close, null, tint = CreamyWhite)
                        }
                    }

                    Text("Direct-to-Table QR Service", fontWeight = FontWeight.Black, fontSize = 20.sp, color = CreamyWhite)
                    Text(
                        "Walked into a TACOLAB site? Scan the plate QR on your table to automatically assign delivery, unlock instantaneous table split-bill features, and re-order on the fly!",
                        fontSize = 12.sp,
                        color = CoolGrey,
                        lineHeight = 16.sp
                    )

                    // Table selector slider simulation
                    var simulatingScan by remember { mutableStateOf(false) }
                    var scannedTable by remember { mutableStateOf("Table 14") }

                    if (simulatingScan) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CarbonCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                Text("Engaging Camera Optics QR Scanner...", fontSize = 11.sp, color = CoolGrey)
                            }
                        }

                        LaunchedEffect(Unit) {
                            delay(2000)
                            simulatingScan = false
                            viewModel.showMessage("Successfully bound order stream to Seat: Table 14!")
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("BOUND TARGET", fontSize = 10.sp, color = CoolGrey, fontWeight = FontWeight.Bold)
                                    Text(scannedTable, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = ObsidianDark)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Seating Reservation Check", fontSize = 12.sp, color = CoolGrey)
                                    Text("Occupied (Active)", fontSize = 12.sp, color = CreamyWhite)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Smart Split-Billing", fontSize = 12.sp, color = CoolGrey)
                                    Text("Ready (Split with 3 friends)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }

                        Button(
                            onClick = { simulatingScan = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Table QR Scan", color = ObsidianDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // STORE LOCATOR MODAL
    if (showLocatorSim) {
        Dialog(onDismissRequest = { showLocatorSim = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("locator_dialog")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "REALTIME STORE GPS STATUS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { showLocatorSim = false }) {
                            Icon(Icons.Filled.Close, null, tint = CreamyWhite)
                        }
                    }

                    Text("TACOLAB Location Finder", fontWeight = FontWeight.Black, fontSize = 20.sp, color = CreamyWhite)

                    // Mock Google Maps Graphic Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonCard)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing custom vector-styled dots representing map streets
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = ObsidianDark,
                                size = size
                            )
                            // Draw grid lines representing simulated streets
                            val step = 40.dp.toPx()
                            for (x in 0..size.width.toInt() step step.toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                                    end = androidx.compose.ui.geometry.Offset(x.toFloat(), size.height)
                                )
                            }
                            for (y in 0..size.height.toInt() step step.toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                                    end = androidx.compose.ui.geometry.Offset(size.width, y.toFloat())
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.MyLocation, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                            Text("Mocking Live Locator Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                        }
                    }

                    // List of locations with peak time markers
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("TACOLAB Lava Lakeside", "0.8 miles away", "Drive-thru wait: 3 min (Low)"),
                            Triple("TACOLAB Downtown Silicon", "2.1 miles away", "Drive-thru wait: 12 min (Peak)"),
                            Triple("TACOLAB Sunset Lab Quad", "4.6 miles away", "Drive-thru wait: 1 min (Vip Only)")
                        ).forEach { loc ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(loc.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CreamyWhite)
                                        Text(loc.second, fontSize = 10.sp, color = CoolGrey)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(loc.third, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

// Helper methods
fun getNextTier(current: String): String = when (current) {
    "Rookie Taco Fan" -> "Taco Regular"
    "Taco Regular" -> "Taco Elite"
    "Taco Elite" -> "Taco Legend"
    else -> "Taco Legend"
}


// ==========================================
// 2. KITCHEN DISPLAY SYSTEM (KDS) VIEW
// ==========================================
@Composable
fun KitchenKdsDashboard(viewModel: TacoLabViewModel) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KDS Dashboard Metadata Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KITCHEN CONTROLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Kitchen Display Desk (KDS)",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Button(
                onClick = { viewModel.clearCart(); viewModel.showMessage("KDS Orders purged.") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Filled.ClearAll, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All Queue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Grid list of active orders
        val activeKitchenOrders = orders.filter { it.status == "PENDING" || it.status == "PREPARING" || it.status == "DRIVETHRU_QUEUE" }

        if (activeKitchenOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.Inbox,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        "KDS Queue is empty!",
                        fontWeight = FontWeight.Bold,
                        color = CoolGrey,
                        fontSize = 15.sp
                    )
                    Text(
                        "Place an order in the Customer tab to watch it stream here in real-time.",
                        fontSize = 11.sp,
                        color = CoolGrey.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeKitchenOrders) { ord ->
                    val prepTimeFraction = ord.prepTimeRemaining.toFloat() / 180f
                    // Dynamically set card urgency border color based on time remaining
                    val urgencyColor = when {
                        ord.prepTimeRemaining <= 30 -> MaterialTheme.colorScheme.primary // Hot burning red-coral
                        ord.prepTimeRemaining <= 100 -> QuesoGold                      // Warming yellow
                        else -> LimeAgave                                             // Safe chill green
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, urgencyColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .testTag("kds_card_${ord.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Header: Order ID, Type, Prep Timer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "ORDER #${ord.id}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        ord.orderType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(urgencyColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${ord.prepTimeRemaining}s",
                                        fontWeight = FontWeight.Black,
                                        color = if (urgencyColor == LimeAgave) ObsidianDark else CreamyWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                            // Itemization Customized breakdown list (Moshi deserialization helper)
                            val converters = TacoLabConverters()
                            val itemsList = converters.fromCustomizedItemList(ord.itemsJson)

                            itemsList.forEach { cItem ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        cItem.baseItemName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    val customStr = mutableListOf<String>()
                                    if (cItem.selectedProtein != "None") customStr.add("Protein: " + cItem.selectedProtein)
                                    if (cItem.selectedSpice != "Medium") customStr.add("Spice: " + cItem.selectedSpice)
                                    if (cItem.selectedCheese != "Cheddar") customStr.add("Cheese: " + cItem.selectedCheese)
                                    customStr.addAll(cItem.selectedExtras)
                                    customStr.add("Wrap: " + cItem.selectedTortilla.replace(" (None)", ""))

                                    Text(
                                        "• " + customStr.joinToString(", "),
                                        fontSize = 10.sp,
                                        color = CoolGrey,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            // Meta attachments (Drive-Thru Lane target, Table numbers)
                            if (ord.orderType == "DINE_IN" && ord.tableNumber != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.TableBar, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text("Dine-In target: ${ord.tableNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (ord.orderType == "DRIVE_THRU" && ord.laneAssignment != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.DirectionsCar, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                    Text("Lane assign: ${ord.laneAssignment}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Flow buttons action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (ord.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.kitchenUpdateOrderStatus(ord.id, "PREPARING") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Prep", fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.kitchenUpdateOrderStatus(ord.id, "COMPLETED") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Complete", fontSize = 11.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteOrder(ord.id) },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. STAFF APP / ADMIN CONSOLE VIEW
// ==========================================
@Composable
fun StaffAdminDashboard(viewModel: TacoLabViewModel) {
    var activeSubTab by remember { mutableStateOf("INVENTORY") } // INVENTORY, ANALYTICS, DISPATCH
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Staff Header details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STAFF OPERATIONS CONSOLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Associate Portal & Hub",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .background(CarbonCard)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "Store #7124 (Lakeside)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Sub tabs picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("INVENTORY", "DISPATCH", "ANALYTICS").forEach { tab ->
                val isSelected = activeSubTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else CarbonCard)
                        .clickable { activeSubTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tab,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) ObsidianDark else CreamyWhite
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Inventory Stocking view
        if (activeSubTab == "INVENTORY") {
            Text(
                "Mark ingredients or retail catalog items as sold out to dynamically remove them from the Customer ordering matrix instantly:",
                fontSize = 11.sp,
                color = CoolGrey,
                lineHeight = 15.sp
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(menuItems) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.category + " Category", fontSize = 10.sp, color = CoolGrey)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    if (item.isAvailable) "IN STOCK" else "SOLD OUT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    color = if (item.isAvailable) LimeAgave else MaterialTheme.colorScheme.primary
                                )
                                Switch(
                                    checked = item.isAvailable,
                                    onCheckedChange = { viewModel.toggleMenuAvailable(item.id, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = LimeAgave)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dispatch simulator (Delivery Integration door)
        else if (activeSubTab == "DISPATCH") {
            Text(
                "Realtime Delivery Fleet Router & Dispatch. Tracks external api feeds (Uber Eats, DoorDash, Just Eat) alongside unified ETA predictions.",
                fontSize = 11.sp,
                color = CoolGrey,
                lineHeight = 15.sp
            )

            val deliveryOrders = orders.filter { it.orderType == "DELIVERY" }

            if (deliveryOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DirectionsBike, null, tint = CoolGrey, modifier = Modifier.size(38.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No Delivery dispatch records active.", fontSize = 13.sp, color = CoolGrey)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(deliveryOrders) { dOrd ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                dOrd.deliveryPartner ?: "DoorDash",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text("ID #${dOrd.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Text(
                                        "STATUS: ${dOrd.status}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (dOrd.status) {
                                            "COMPLETED" -> LimeAgave
                                            else -> QuesoGold
                                        }
                                    )
                                }

                                Divider(color = ObsidianDark)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("ETA TO TARGET", fontSize = 9.sp, color = CoolGrey)
                                        Text("${dOrd.deliveryEta} mins estimated", fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TOTAL EARNINGS", fontSize = 9.sp, color = CoolGrey)
                                        Text("$${String.format("%.2f", dOrd.totalPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Analytics Peak Load Monitor
        else if (activeSubTab == "ANALYTICS") {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Peak hour load metrics
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("STORE THROUGHPUT RATE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Orders / Hour", fontSize = 10.sp, color = CoolGrey)
                                Text("48.4 orders avg", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Lead Preparation Speed", fontSize = 10.sp, color = CoolGrey)
                                Text("102 seconds target", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Visual Custom Canvas showing load curves
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PEAK BUSINESS CURVES (SIM)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(ObsidianDark),
                            contentAlignment = Alignment.Center
                        ) {
                            // Custom load curve graph representing peak hours
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(0f, size.height * 0.8f)
                                path.quadraticTo(size.width * 0.3f, size.height * 0.2f, size.width * 0.5f, size.height * 0.1f)
                                path.quadraticTo(size.width * 0.7f, size.height * 0.4f, size.width, size.height * 0.7f)
                                drawPath(
                                    path = path,
                                    color = CoralSpicy,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                        Text("Spikes noted at Lunch block (12:00 - 13:30) and Late Night volcano block (22:00 - 01:20).", fontSize = 10.sp, color = CoolGrey)
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. ARCHITECT BLUEPRINTS SPECIFICATION VIEW
// ==========================================
@Composable
fun ArchitectSpecsDashboard() {
    var activeBlueTab by remember { mutableStateOf("ROUTING_formula") } // MICROSERVICES, IOS_SWIFTUR, ROUTING_formula

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Spec Headers
        Column {
            Text(
                text = "ENGINEERING BLUEPRINTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "TACOLAB 2030 Ecosystem",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Segment Picker controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Pair("ROUTING_formula", "Routing Formulas"),
                Pair("MICROSERVICES", "Backend Services"),
                Pair("IOS_SWIFTUI", "iOS SwiftUI Spec")
            ).forEach { item ->
                val isSelected = activeBlueTab == item.first
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else CarbonCard)
                        .clickable { activeBlueTab = item.first }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ObsidianDark else CreamyWhite
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeBlueTab == "ROUTING_formula") {
                SpecCard(
                    title = "Autonomous Driver Dispatch Integration & Batching Formula",
                    content = "To balance third-party delivery dispatch (Uber/DoorDash) with direct fleet couriers, we utilize a custom batch optimization math algorithm to scale costs down under peak traffic:\n\n" +
                            "WaitTime(e) = BasePrep(s) + Σ(Dist(c) * SpeedConst(traffic)) - PriorityRatio(VIP_loyalty)\n\n" +
                            "• Ensures 95% of orders arrive sub-15 minutes or inside the predicted drive-thru slot.\n" +
                            "• Avoids double charging by using safe distributed locking databases."
                )

                SpecCard(
                    title = "Double-Charge Safety & PCI Compliance Protocol",
                    content = "1. Tokenized Apple Pay / Google Pay signatures bypass storage of clear credit credentials in database caches.\n" +
                            "2. Distributed locking keys via Redis matching transactional nonce to protect orders from duplicate triggers (no multi-button clicks failures!)."
                )
            } else if (activeBlueTab == "MICROSERVICES") {
                SpecCard(
                    title = "1. Order Microservice (Java Spring / GCP)",
                    content = "Tracks shopping cart validation, lifecycle states, and payment captures. Communicates state shifts securely to the Kitchen Service over RabbitMQ message queues."
                )
                SpecCard(
                    title = "2. Loyalty & Gamified Rewards Engine",
                    content = "Validates user referal codes and steak progress metrics. Manages point redemption triggers with standard rate limits to prevent referral spoof loops."
                )
                SpecCard(
                    title = "3. AI Menu Recommendation Microservice",
                    content = "Calculates weather trends, historical customer orders, and seasonal peaks to recommend the ultimate customized taco or burrito on demand."
                )
            } else if (activeBlueTab == "IOS_SWIFTUI") {
                SpecCard(
                    title = "SwiftUI Customization Layer Blueprint Structure",
                    content = "struct CustomizeItemView: View {\n" +
                            "  @StateVar var item: CustomizedItem\n" +
                            "  var body: some View {\n" +
                            "    VStack(spacing: 12) {\n" +
                            "      LivePriceHeader(price: item.recalculatedPrice())\n" +
                            "      ProteinSelector(protein: \$item.selectedProtein)\n" +
                            "      SpiceRocker(spice: \$item.selectedSpice)\n" +
                            "      ExtrasGrid(extras: \$item.selectedExtras)\n" +
                            "      Spacer()\n" +
                            "    }\n" +
                            "    .background(Color.obsidianDark)\n" +
                            "  }\n" +
                            "}"
                )
            }
        }
    }
}

@Composable
fun SpecCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text(
                content,
                fontSize = 12.sp,
                color = CoolGrey,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
