package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Change Manager") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            AllMembersScreen(
                onBack = onBack,
                onSelectMemberForManager = { selectedUser.value = it }
            )
            Button(
                enabled = selectedUser.value != null,
                onClick = {
                    scope.launch {
                        val user = AuthRepository().getCurrentUserDetails().getOrNull()
                        val messId = user?.messId
                        val newAdmin = selectedUser.value?.uid
                        if (messId == null || newAdmin == null) {
                            snackbarHostState.showSnackbar("Select a member")
                            return@launch
                        }
                        val result = userRepository.changeManager(messId, newAdmin)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Manager changed")
                            onBack()
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to change manager")
                        }
                    }
                }
            ) { Text("Make Manager") }
        }
    }
}

