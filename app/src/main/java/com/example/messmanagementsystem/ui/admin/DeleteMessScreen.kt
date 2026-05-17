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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MessRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteMessScreen(
    onBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSaving by remember { androidx.compose.runtime.mutableStateOf(false) }
    val repo = remember { MessRepository() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Delete Mess", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("This will mark the mess as deleted. You can reverse in Firestore.")
            Button(
                enabled = !isSaving,
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
                    val result = repo.softDeleteMess(messId)
                    isSaving = false
                    if (result.isSuccess) {
                        snackbar.showSnackbar("Mess marked as deleted")
                        onBack()
                    } else {
                        snackbar.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to delete mess")
                    }
                }
            }) { 
                if (isSaving) androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                else Text("Confirm Delete") 
            }
        }
    }
}

