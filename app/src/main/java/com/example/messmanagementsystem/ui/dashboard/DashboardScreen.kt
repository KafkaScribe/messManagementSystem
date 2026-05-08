package com.example.messmanagementsystem.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.6f)) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Mess Options",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                if (uiState.role == "admin") {
                    NavigationDrawerItem(
                        label = { Text("Admin Panel") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToAdmin()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else if (uiState.statusInMess == "approved") {
                    NavigationDrawerItem(
                        label = { Text("Member Panel") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToUserPanel()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mess Management System") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(androidx.compose.material.icons.Icons.Default.Notifications, contentDescription = "Notification") },
                        label = { Text("Notification") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Header Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome, ${uiState.userName}!",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else if (uiState.messId != null && uiState.statusInMess == "approved") {
                                // Section 1: Monthly Mess Details
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Monthly Mess Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Mess Name:")
                                            Text("%s".format(uiState.messName))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Mess Balance:")
                                            Text("৳%.2f".format(uiState.messBalance))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Deposit:")
                                            Text("৳%.2f".format(uiState.totalDeposit))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Meal Count:")
                                            Text("${uiState.totalMealCount}")
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Meal Cost:")
                                            Text("৳%.2f".format(uiState.totalMealCost))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Meal Rate:")
                                            Text("৳%.2f".format(uiState.mealRate))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Other Cost:")
                                            Text("৳%.2f".format(uiState.totalOtherCost))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Section 2: My Summary
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("My Summary", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("My Meal Count:")
                                            Text("${uiState.myMealCount}")
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("My Deposit:")
                                            Text("৳%.2f".format(uiState.myDeposit))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("My Cost:")
                                            Text("৳%.2f".format(uiState.myCost))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("My Balance:")
                                            Text("৳%.2f".format(uiState.myBalance))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Section 3: All Members Details
                                Text("All Members Balance & Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                uiState.memberStats.forEach { stat ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(stat.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Deposit: ৳%.2f".format(stat.deposit), style = MaterialTheme.typography.bodySmall)
                                                Text("Meals: ${stat.mealCount}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Cost: ৳%.2f".format(stat.cost), style = MaterialTheme.typography.bodySmall)
                                                Text("Balance: ৳%.2f".format(stat.balance), style = MaterialTheme.typography.bodySmall, color = if (stat.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text("Please join a mess to see the dashboard details.")
                            }
                        }
                    }
                    1 -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            if (uiState.statusInMess == "invited") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Mess Invitation", style = MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("You have been invited to join ${uiState.messName ?: "a mess"}.")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { viewModel.declineInvitation() }) {
                                                Text("Decline", color = MaterialTheme.colorScheme.error)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(onClick = { viewModel.acceptInvitation() }) {
                                                Text("Accept")
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No new notifications.", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("User Profile", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Name: ${uiState.userName}", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Role: ${uiState.role.uppercase()}", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Status: ${uiState.statusInMess ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logout")
                            }
                        }
                    }
                }
            }
        }
    }
}
