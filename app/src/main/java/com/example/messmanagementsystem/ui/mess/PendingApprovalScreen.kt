package com.example.messmanagementsystem.ui.mess

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PendingApprovalScreen(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            // Since we don't know the full material-icons library loaded, we can use a text symbol or standard icon if available.
            // Simplified for now without an icon.
            contentDescription = "Pending Approval",
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Approval Pending",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your request to join the mess has been sent. Please wait for the admin to approve you.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onLogoutClick) {
            Text("Logout")
        }
    }
}
// Placeholder icon to avoid build errors:
@Composable
fun Icon(contentDescription: String, modifier: Modifier) {
    Box(modifier = modifier)
}
