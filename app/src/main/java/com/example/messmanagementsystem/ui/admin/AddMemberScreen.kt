package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    onBack: () -> Unit
) {
    val emailText = remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Mess Member") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = emailText.value,
                onValueChange = { emailText.value = it },
                label = { Text("User Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                val email = emailText.value.trim()
                if (email.isEmpty()) {
                    scope.launch { snackbar.showSnackbar("Enter an email") }
                    return@Button
                }
                scope.launch {
                    val user = AuthRepository().getCurrentUserDetails().getOrNull()
                    val messId = user?.messId
                    if (messId == null) {
                        snackbar.showSnackbar("No mess assigned")
                        return@launch
                    }
                    val result = userRepository.addMemberByEmail(messId, email)
                    if (result.isSuccess) {
                        snackbar.showSnackbar("User invited to join the mess")
                        onBack()
                    } else {
                        snackbar.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to invite member")
                    }
                }
            }, modifier = Modifier.padding(top = 12.dp)) {
                Text("Add")
            }
        }
    }
}

