package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import com.example.messmanagementsystem.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeManagerScreen(
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectedUser = remember { mutableStateOf<User?>(null) }
    val userRepository = remember { UserRepository() }
    val notificationRepository = remember { NotificationRepository() }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val users = remember { mutableStateListOf<User>() }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = AuthRepository().getCurrentUserDetails().getOrNull()
        user?.messId?.let { messId ->
            val allMembers = userRepository.getMembersByMess(messId)
            // exclude the current admin from the list so they don't select themselves
            users.addAll(allMembers.filter { it.uid != user?.uid })
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Change Manager") },
            text = { Text("Are you sure you want to change the manager? Your role will become mess member, and ${selectedUser.value?.name?.ifEmpty { selectedUser.value?.email } ?: selectedUser.value?.email} will become the new admin.") },
            confirmButton = {
                Button(
                    enabled = !isSaving,
                    onClick = {
                        scope.launch {
                            isSaving = true
                            val user = AuthRepository().getCurrentUserDetails().getOrNull()
                            val messId = user?.messId
                            val currentAdminId = user?.uid
                            val newAdmin = selectedUser.value?.uid
                            if (messId == null || newAdmin == null || currentAdminId == null) {
                                snackbarHostState.showSnackbar("Select a member or missing user data")
                                isSaving = false
                                showConfirmDialog = false
                                return@launch
                            }
                            val result = userRepository.changeManager(messId, currentAdminId, newAdmin)
                            isSaving = false
                            showConfirmDialog = false
                            if (result.isSuccess) {
                                notificationRepository.sendNotificationToUsers(
                                    users.map { it.uid } + listOf(currentAdminId),
                                    messId,
                                    "Manager Changed",
                                    "The mess manager role has been transferred to ${selectedUser.value?.name?.ifEmpty { selectedUser.value?.email }}."
                                )
                                snackbarHostState.showSnackbar("Manager changed")
                                onBack()
                            } else {
                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to change manager")
                            }
                        }
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Confirm")
                    }
                }
            },
            dismissButton = {
                Button(
                    enabled = !isSaving,
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Reject")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Change Manager") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedUser.value?.let { it.name.ifEmpty { it.email } } ?: "Select a member",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.name.ifEmpty { user.email }) },
                            onClick = {
                                selectedUser.value = user
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedUser.value != null,
                onClick = {
                    showConfirmDialog = true
                }
            ) { 
                Text("Confirm Change Manager") 
            }
        }
    }
}
