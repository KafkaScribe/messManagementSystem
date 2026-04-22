package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.Deposit
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.DepositRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepositScreen(
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val amountText = remember { mutableStateOf("") }
    val noteText = remember { mutableStateOf("") }
    // Repositories
    val authRepository = remember { AuthRepository() }
    val depositRepository = remember { DepositRepository() }
    val userRepository = remember { UserRepository() }

    val members = remember { mutableStateListOf<User>() }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userDetails = authRepository.getCurrentUserDetails().getOrNull()
        val messId = userDetails?.messId ?: return@LaunchedEffect
        val users = userRepository.getMembersByMess(messId)
        members.addAll(users)
        selectedUser = users.find { it.uid == userDetails.uid } ?: users.firstOrNull()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Deposit") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedUser?.name?.ifEmpty { selectedUser?.email } ?: "Select Member",
                    onValueChange = { },
                    label = { Text("Member") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    members.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.name.ifEmpty { user.email }) },
                            onClick = {
                                selectedUser = user
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedTextField(
                value = amountText.value,
                onValueChange = { amountText.value = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedTextField(
                value = noteText.value,
                onValueChange = { noteText.value = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = {
                val amount = amountText.value.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    scope.launch { snackbarHostState.showSnackbar("Enter a valid amount") }
                    return@Button
                }
                val targetUser = selectedUser
                if (targetUser == null) {
                    scope.launch { snackbarHostState.showSnackbar("Please select a member") }
                    return@Button
                }
                scope.launch {
                    val userDetails = authRepository.getCurrentUserDetails().getOrNull()
                    val messId = userDetails?.messId ?: run {
                        snackbarHostState.showSnackbar("No mess assigned"); return@launch
                    }
                    val deposit = Deposit(
                        messId = messId,
                        userId = targetUser.uid,
                        amount = amount,
                        note = noteText.value,
                        date = System.currentTimeMillis()
                    )
                    val result = depositRepository.addDeposit(deposit)
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Deposit added")
                        onBack()
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to add deposit")
                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}

