package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import com.example.messmanagementsystem.ui.components.MessOutlinedTextField
import com.example.messmanagementsystem.ui.components.MessPrimaryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(onBack: () -> Unit) {
    val emailText = remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Member", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            MessOutlinedTextField(value = emailText.value, onValueChange = { emailText.value = it }, label = "User Email", leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp)) })
            Spacer(modifier = Modifier.height(20.dp))
            MessPrimaryButton(
                text = "Invite Member",
                isLoading = isSaving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val email = emailText.value.trim()
                    if (email.isEmpty()) { scope.launch { snackbar.showSnackbar("Enter an email") }; return@MessPrimaryButton }
                    scope.launch {
                        isSaving = true
                        val user = AuthRepository().getCurrentUserDetails().getOrNull()
                        val messId = user?.messId
                        if (messId == null) { snackbar.showSnackbar("No mess assigned"); isSaving = false; return@launch }
                        val result = userRepository.addMemberByEmail(messId, email)
                        isSaving = false
                        if (result.isSuccess) { snackbar.showSnackbar("User invited to join the mess"); onBack() }
                        else snackbar.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to invite member")
                    }
                }
            )
        }
    }
}
