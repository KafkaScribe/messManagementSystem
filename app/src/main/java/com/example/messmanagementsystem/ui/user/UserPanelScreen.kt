package com.example.messmanagementsystem.ui.user

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch

data class UserAction(
    val label: String,
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

    val actions = listOf(
        UserAction("All Members", onAllMembers),
        UserAction("Active Month Details", onActiveMonthDetails),
        UserAction("All Months", onAllMonth),
        UserAction("Leave Mess") { showLeaveDialog = true }
    )

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLeaving) showLeaveDialog = false },
            title = { Text("Leave Mess") },
            text = { Text("Are you sure you want to leave this mess? This action cannot be undone here.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        isLeaving = true
                        val user = AuthRepository().getCurrentUserDetails().getOrNull()
                        if (user != null) {
                            userRepository.declineInvite(user.uid) // declineInvite unsets messId nicely
                            onLeaveMessSuccess()
                        }
                        isLeaving = false
                        showLeaveDialog = false
                    }
                }) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isLeaving) showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Panel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Mess Options",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(actions) { action ->
                Card(
                    onClick = action.onClick,
                    modifier = Modifier.padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    ListItem(
                        headlineContent = { Text(action.label) }
                    )
                }
            }
        }
    }
}
