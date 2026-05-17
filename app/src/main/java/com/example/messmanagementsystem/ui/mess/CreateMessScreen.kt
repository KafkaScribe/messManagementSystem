package com.example.messmanagementsystem.ui.mess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.ui.components.MessOutlinedTextField
import com.example.messmanagementsystem.ui.components.MessPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMessScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: MessViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val authRepo = remember { AuthRepository() }

    LaunchedEffect(uiState) { if (uiState is MessUiState.Success) onSuccess() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create a Mess", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(horizontal = 28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.HomeWork, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Set Up Your Mess", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Create a new mess for you and your members", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(40.dp))
            MessOutlinedTextField(value = name, onValueChange = { name = it }, label = "Mess Name", leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp)) })
            Spacer(modifier = Modifier.height(12.dp))
            MessOutlinedTextField(value = location, onValueChange = { location = it }, label = "Location", leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp)) })
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState is MessUiState.Error) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text((uiState as MessUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            MessPrimaryButton(text = "Create Mess", onClick = { val userId = authRepo.currentUser?.uid; if (userId != null) viewModel.createMess(name, location, userId) }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank() && location.isNotBlank(), isLoading = uiState == MessUiState.Loading)
        }
    }
}
