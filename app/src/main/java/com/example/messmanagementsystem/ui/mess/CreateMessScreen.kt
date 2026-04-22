package com.example.messmanagementsystem.ui.mess

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messmanagementsystem.data.repository.AuthRepository

@Composable
fun CreateMessScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: MessViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val authRepo = remember { AuthRepository() } // Simplified access to userId

    LaunchedEffect(uiState) {
        if (uiState is MessUiState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create a Mess", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Mess Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState is MessUiState.Error) {
            Text(
                (uiState as MessUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val userId = authRepo.currentUser?.uid
                if (userId != null) {
                    viewModel.createMess(name, location, userId)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState != MessUiState.Loading && name.isNotBlank() && location.isNotBlank()
        ) {
            if (uiState == MessUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Create")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
