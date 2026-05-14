package com.example.messmanagementsystem.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch

data class UserAction(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val isDanger: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPanelScreen(
    onBack: () -> Unit,
    onAllMembers: () -> Unit,
    onActiveMonthDetails: () -> Unit,
    onAllMonth: () -> Unit,
    onLeaveMessSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showLeaveDialog by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }
    val userRepository = remember { UserRepository() }

    val infoActions = listOf(
        UserAction("All Members", "View everyone in your mess", Icons.Default.List, onClick = onAllMembers),
        UserAction("Month Details", "Current month overview", Icons.Default.Info, onClick = onActiveMonthDetails),
        UserAction("All Months", "Browse past month records", Icons.Default.DateRange, onClick = onAllMonth)
    )

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLeaving) showLeaveDialog = false },
            icon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Leave Mess", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to leave this mess? You will lose access to all mess data.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLeaving = true
                            val user = AuthRepository().getCurrentUserDetails().getOrNull()
                            if (user != null) {
                                userRepository.declineInvite(user.uid)
                                onLeaveMessSuccess()
                            }
                            isLeaving = false
                            showLeaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isLeaving
                ) {
                    if (isLeaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onError)
                    else Text("Leave Mess")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isLeaving) showLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Member Panel", fontWeight = FontWeight.Bold)
                        Text(
                            "Your mess options",
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
            // Information section
            item {
                Text(
                    "Information",
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
                        infoActions.forEachIndexed { index, action ->
                            MemberActionRow(action)
                            if (index < infoActions.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                            }
                        }
                    }
                }
            }

            // Danger zone
            item {
                Text(
                    "Danger Zone",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    MemberActionRow(
                        UserAction(
                            "Leave Mess",
                            "Remove yourself from this mess",
                            Icons.Default.ExitToApp,
                            isDanger = true,
                            onClick = { showLeaveDialog = true }
                        )
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MemberActionRow(action: UserAction) {
    val iconBgColor = if (action.isDanger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val iconTint = if (action.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
                Icon(action.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
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
