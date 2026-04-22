package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

data class AdminAction(
    val label: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onAddDeposit: () -> Unit,
    onAddMeal: () -> Unit,
    onAddCost: () -> Unit,
    onActiveMonthDetails: () -> Unit,
    onAllMembers: () -> Unit,
    onChangeManager: () -> Unit,
    onStartNewMonth: () -> Unit,
    onAllMonth: () -> Unit,
    onDeleteMess: () -> Unit
) {
    val actions = listOf(
        AdminAction("Add Mess Member", onAddMember),
        AdminAction("Add Deposit", onAddDeposit),
        AdminAction("Add Meal", onAddMeal),
        AdminAction("Add Cost", onAddCost),
        AdminAction("Active Month Details", onActiveMonthDetails),
        AdminAction("All Member", onAllMembers),
        AdminAction("Change Manager", onChangeManager),
        AdminAction("Start New Month", onStartNewMonth),
        AdminAction("All Month", onAllMonth),
        AdminAction("Delete Mess", onDeleteMess)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Mess Manager",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(actions) { action ->
                Card(
                    onClick = action.onClick,
                    modifier = Modifier.padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    ListItem(
                        headlineContent = { Text(action.label) }
                    )
                }
            }
        }
    }
}

