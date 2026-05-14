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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MonthRepository
import com.example.messmanagementsystem.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartNewMonthScreen(
    onBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val repo = remember { MonthRepository() }
    val notificationRepository = remember { NotificationRepository() }
    var isSaving by remember { mutableStateOf(false) }
    var monthName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Start New Month") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Create the current month for tracking.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = monthName,
                onValueChange = { monthName = it },
                label = { Text("Month Name (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                scope.launch {
                    isSaving = true
                    val user = AuthRepository().getCurrentUserDetails().getOrNull()
                    val messId = user?.messId
                    if (messId == null) {
                        snackbar.showSnackbar("No mess assigned")
                        isSaving = false
                        return@launch
                    }
                    val result = repo.startNewMonth(messId, monthName)
                    isSaving = false
                    if (result.isSuccess) {
                        val userRepository = com.example.messmanagementsystem.data.repository.UserRepository()
                        val members = userRepository.getMembersByMess(messId)
                        notificationRepository.sendNotificationToUsers(
                            members.map { it.uid },
                            messId,
                            "New Month Started",
                            "A new month has been started. Balances and calculations are tracking the new active month."
                        )
                        snackbar.showSnackbar("Month started: ${result.getOrNull()}")
                        onBack()
                    } else {
                        snackbar.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to start month")
                    }
                }
            }) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Start")
                }
            }
        }
    }
}

