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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MonthRepository
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Start New Month") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Create the current month for tracking.")
            Button(onClick = {
                scope.launch {
                    val user = AuthRepository().getCurrentUserDetails().getOrNull()
                    val messId = user?.messId
                    if (messId == null) {
                        snackbar.showSnackbar("No mess assigned")
                        return@launch
                    }
                    val result = repo.startNewMonth(messId)
                    if (result.isSuccess) {
                        snackbar.showSnackbar("Month started: ${result.getOrNull()}")
                        onBack()
                    } else {
                        snackbar.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to start month")
                    }
                }
            }) { Text("Start") }
        }
    }
}

