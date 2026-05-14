package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AdminAction(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color? = null,
    val isDanger: Boolean = false,
    val onClick: () -> Unit
)

data class AdminSection(
    val title: String,
    val actions: List<AdminAction>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onAddDeposit: () -> Unit,
    onAddMeal: () -> Unit,
    onAddCost: () -> Unit,
    onActiveMonthDetails: () -> Unit,
    onAllMembers: () -> Unit,
    onChangeManager: () -> Unit,
    onStartNewMonth: () -> Unit,
    onAllMonth: () -> Unit,
    onDeleteMess: () -> Unit,
    onRemoveMember: () -> Unit
) {
    val sections = listOf(
        AdminSection(
            title = "Members",
            actions = listOf(
                AdminAction("Add Member", "Invite a new member", Icons.Default.Person, onClick = onAddMember),
                AdminAction("Remove Member", "Remove an existing member", Icons.Default.Close, onClick = onRemoveMember),
                AdminAction("All Members", "View all member details", Icons.Default.List, onClick = onAllMembers)
            )
        ),
        AdminSection(
            title = "Finance",
            actions = listOf(
                AdminAction("Add Deposit", "Record a member deposit", Icons.Default.Add, onClick = onAddDeposit),
                AdminAction("Add Meal", "Log meals for a date", Icons.Default.Star, onClick = onAddMeal),
                AdminAction("Add Cost", "Record meal or other cost", Icons.Default.ShoppingCart, onClick = onAddCost)
            )
        ),
        AdminSection(
            title = "Reports",
            actions = listOf(
                AdminAction("Month Details", "View active month stats", Icons.Default.Info, onClick = onActiveMonthDetails),
                AdminAction("All Months", "Browse past months", Icons.Default.DateRange, onClick = onAllMonth)
            )
        ),
        AdminSection(
            title = "Management",
            actions = listOf(
                AdminAction("Change Manager", "Transfer admin role", Icons.Default.Settings, onClick = onChangeManager),
                AdminAction("Start New Month", "Close current & begin fresh", Icons.Default.Refresh, onClick = onStartNewMonth),
                AdminAction(
                    "Delete Mess", "Permanently delete this mess",
                    Icons.Default.Delete, isDanger = true, onClick = onDeleteMess
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", fontWeight = FontWeight.Bold)
                        Text(
                            "Manage your mess",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            sections.forEach { section ->
                item {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            section.actions.forEachIndexed { index, action ->
                                AdminActionRow(action)
                                if (index < section.actions.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacer
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AdminActionRow(action: AdminAction) {
    val iconBgColor = when {
        action.isDanger -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val iconTint = when {
        action.isDanger -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val textColor = if (action.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = action.onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(action.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
                Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
