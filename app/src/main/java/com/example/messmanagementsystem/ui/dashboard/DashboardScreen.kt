package com.example.messmanagementsystem.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messmanagementsystem.ui.components.*
import com.example.messmanagementsystem.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToUserPanel: () -> Unit,
    onNavigateToMessSelection: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.messId, uiState.isLoading) {
        if (!uiState.isLoading && uiState.messId == null) {
            onNavigateToMessSelection()
        }
    }
    LaunchedEffect(Unit) { viewModel.refreshData() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.72f),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GradientTealStart, GradientTealEnd)))
                        .padding(horizontal = 20.dp, vertical = 32.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                uiState.userName.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(uiState.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(uiState.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                }
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                if (uiState.role == "admin") {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Admin Panel") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onNavigateToAdmin() },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else if (uiState.statusInMess == "approved") {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.List, null) },
                        label = { Text("Member Panel") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; onNavigateToUserPanel() },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = onLogout,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        var showEditProfileDialog by remember { mutableStateOf(false) }
        var editName by remember { mutableStateOf("") }
        var editPhone by remember { mutableStateOf("") }
        var isSavingProfile by remember { mutableStateOf(false) }
        val userRepository = remember { com.example.messmanagementsystem.data.repository.UserRepository() }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (uiState.messName != null) {
                                Text(uiState.messName!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { selectedTab = 1 }) {
                            BadgedBox(badge = { if (uiState.notifications.any { !it.isRead }) Badge() }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") }, selected = selectedTab == 0, onClick = { selectedTab = 0 })
                    NavigationBarItem(
                        icon = { BadgedBox(badge = { if (uiState.notifications.any { !it.isRead }) Badge() }) { Icon(Icons.Default.Notifications, null) } },
                        label = { Text("Alerts") }, selected = selectedTab == 1, onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") }, selected = selectedTab == 2, onClick = { selectedTab = 2 })
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> HomeTab(uiState, paddingValues, viewModel)
                1 -> NotificationsTab(uiState, paddingValues, viewModel)
                2 -> ProfileTab(uiState, paddingValues, onEditProfile = { editName = uiState.userName; editPhone = uiState.phoneNumber; showEditProfileDialog = true }, onLogout = onLogout)
            }

            if (showEditProfileDialog) {
                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text("Edit Profile", fontWeight = FontWeight.SemiBold) },
                    text = {
                        Column {
                            MessOutlinedTextField(value = editName, onValueChange = { editName = it }, label = "Name")
                            Spacer(modifier = Modifier.height(8.dp))
                            MessOutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = "Phone Number")
                        }
                    },
                    confirmButton = {
                        Button(enabled = !isSavingProfile, onClick = {
                            scope.launch {
                                isSavingProfile = true
                                val result = userRepository.updateProfile(uiState.userId, editName, editPhone)
                                isSavingProfile = false
                                if (result.isSuccess) { showEditProfileDialog = false; viewModel.refreshData() }
                                else snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to update profile")
                            }
                        }) {
                            if (isSavingProfile) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Save")
                        }
                    },
                    dismissButton = { TextButton(enabled = !isSavingProfile, onClick = { showEditProfileDialog = false }) { Text("Cancel") } }
                )
            }
        }
    }
}

@Composable
private fun HomeTab(uiState: DashboardState, paddingValues: PaddingValues, viewModel: DashboardViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text(uiState.userName.firstOrNull()?.uppercase() ?: "U", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Good day,", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(uiState.userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.isLoading) {
            item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
            return@LazyColumn
        }

        if (uiState.messId == null || uiState.statusInMess != "approved") {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Join or create a mess to see your dashboard.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            return@LazyColumn
        }

        // Mess Overview
        item {
            SectionTitle("Mess Overview")
            Spacer(Modifier.height(12.dp))
            GradientHighlightCard(gradientColors = listOf(GradientTealStart, GradientTealEnd)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Mess Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Text("৳%.2f".format(uiState.messBalance), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalance, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Deposit", "৳%.2f".format(uiState.totalDeposit), modifier = Modifier.weight(1f))
                StatCard("Other Cost", "৳%.2f".format(uiState.totalOtherCost), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Meals", "${uiState.totalMealCount}", modifier = Modifier.weight(1f))
                StatCard("Meal Rate", "৳%.2f".format(uiState.mealRate), modifier = Modifier.weight(1f))
            }
        }

        // My Summary
        item {
            SectionTitle("My Summary")
            Spacer(Modifier.height(12.dp))
            GradientHighlightCard(gradientColors = listOf(GradientIndigoStart, GradientIndigoEnd)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("My Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Text("৳%.2f".format(uiState.myBalance), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Meals: ${uiState.myMealCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("My Deposit", "৳%.2f".format(uiState.myDeposit), modifier = Modifier.weight(1f))
                StatCard("My Cost", "৳%.2f".format(uiState.myCost), modifier = Modifier.weight(1f))
            }
        }

        // Members
        if (uiState.memberStats.isNotEmpty()) {
            item { SectionTitle("Members Overview") }
            items(uiState.memberStats) { stat ->
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text(stat.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Deposit ৳%.0f  •  ${stat.mealCount} meals  •  Cost ৳%.0f".format(stat.deposit, stat.cost), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("৳%.2f".format(stat.balance), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (stat.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Text("balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTab(uiState: DashboardState, paddingValues: PaddingValues, viewModel: DashboardViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        if (uiState.statusInMess == "invited") {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Mess Invitation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("You've been invited to join ${uiState.messName ?: "a mess"}.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { viewModel.declineInvitation() }, enabled = !uiState.isLoading) { Text("Decline") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { viewModel.acceptInvitation() }, enabled = !uiState.isLoading) {
                                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Accept")
                            }
                        }
                    }
                }
            }
        }

        if (uiState.notifications.isEmpty() && uiState.statusInMess != "invited") {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        Text("No notifications yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.notifications) { notif ->
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(8.dp).offset(y = 6.dp).clip(CircleShape).background(if (notif.isRead) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!notif.isRead) { TextButton(onClick = { viewModel.markNotificationAsRead(notif.id) }) { Text("Read", style = MaterialTheme.typography.labelSmall) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(uiState: DashboardState, paddingValues: PaddingValues, onEditProfile: () -> Unit, onLogout: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(2.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(GradientTealStart, GradientTealEnd)))) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Text(uiState.userName.firstOrNull()?.uppercase() ?: "U", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(uiState.userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(uiState.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ProfileRow(icon = Icons.Default.Phone, label = "Phone", value = uiState.phoneNumber.ifEmpty { "Not set" })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(icon = Icons.Default.Star, label = "Role", value = uiState.role.uppercase())
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(icon = Icons.Default.Info, label = "Status", value = uiState.statusInMess?.replaceFirstChar { it.uppercase() } ?: "Unknown")
                }
            }
        }
        item { MessPrimaryButton(text = "Edit Profile", onClick = onEditProfile, modifier = Modifier.fillMaxWidth(), icon = Icons.Default.Edit) }
        item {
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Default.ExitToApp, null); Spacer(Modifier.width(8.dp)); Text("Logout", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
