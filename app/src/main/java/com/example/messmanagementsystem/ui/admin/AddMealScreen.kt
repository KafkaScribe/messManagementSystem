package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.MealSelection
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.MealRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBack: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userRepo = remember { UserRepository() }
    val mealRepo = remember { MealRepository() }
    
    val members = remember { mutableStateListOf<User>() }
    val selections = remember { mutableStateMapOf<String, Triple<Boolean, Boolean, Boolean>>() }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateText = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = AuthRepository().getCurrentUserDetails().getOrNull()
        val messId = currentUser?.messId ?: return@LaunchedEffect
        val fetchedMembers = userRepo.getMembersByMess(messId)
        members.clear()
        members.addAll(fetchedMembers)
        
        for (user in fetchedMembers) {
            selections[user.uid] = Triple(true, true, true) // default all meals to selected
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Meal") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        bottomBar = {
            Button(
                onClick = {
                    if (isSaving) return@Button
                    scope.launch {
                        isSaving = true
                        var successCount = 0
                        for (user in members) {
                            val sel = selections[user.uid] ?: Triple(false, false, false)
                            val mealSel = MealSelection(
                                dateId = dateText,
                                userId = user.uid,
                                messId = user.messId ?: "",
                                hasBreakfast = sel.first,
                                hasLunch = sel.second,
                                hasDinner = sel.third
                            )
                            val result = mealRepo.setMealForUser(user.uid, mealSel)
                            if (result.isSuccess) successCount++
                        }
                        snackbar.showSnackbar("Saved meals for $successCount members")
                        isSaving = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            OutlinedTextField(
                value = dateText,
                onValueChange = { },
                label = { Text("Selected Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = user.name.ifEmpty { "Unknown User" }, style = MaterialTheme.typography.titleMedium)
                            Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val sel = selections[user.uid] ?: Triple(false, false, false)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sel.first, onCheckedChange = { selections[user.uid] = sel.copy(first = it) })
                                    Text("Breakfast", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sel.second, onCheckedChange = { selections[user.uid] = sel.copy(second = it) })
                                    Text("Lunch", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sel.third, onCheckedChange = { selections[user.uid] = sel.copy(third = it) })
                                    Text("Dinner", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

