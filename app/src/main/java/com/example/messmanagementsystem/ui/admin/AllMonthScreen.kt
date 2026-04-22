package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.MonthInfo
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMonthScreen(
    onBack: () -> Unit
) {
    val months = remember { mutableStateListOf<MonthInfo>() }

    LaunchedEffect(Unit) {
        val user = AuthRepository().getCurrentUserDetails().getOrNull()
        val messId = user?.messId ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        val snap = db.collection("messes").document(messId).collection("months").get().await()
        months.clear()
        months.addAll(snap.documents.mapNotNull { it.toObject(MonthInfo::class.java) })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("All Month") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            items(months) { m ->
                ListItem(
                    headlineContent = { Text(m.id) },
                    supportingContent = { Text("Created: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(m.createdAt))}") }
                )
            }
        }
    }
}

