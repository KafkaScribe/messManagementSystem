package com.example.messmanagementsystem.ui.admin

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.Expense
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.ExpenseRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCostScreen(
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val expenseRepository = remember { ExpenseRepository() }
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Meal Cost", "Other Cost")

    // Common State
    val amountText = remember { mutableStateOf("") }
    val descText = remember { mutableStateOf("") }
    
    // Meal Cost State
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    val dateString = remember(selectedDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
    }
    
    val members = remember { mutableStateListOf<User>() }
    val selectedShoppers = remember { mutableStateListOf<User>() }
    
    // Other Cost State
    var isShared by remember { mutableStateOf(true) }
    val targetUsers = remember { mutableStateListOf<User>() }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val userDetails = authRepository.getCurrentUserDetails().getOrNull()
        val messId = userDetails?.messId ?: return@LaunchedEffect
        val users = userRepository.getMembersByMess(messId)
        members.addAll(users)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Cost") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().weight(1f)) {
                if (selectedTabIndex == 0) {
                    // Meal Cost
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth().clickable {
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    selectedDate = newCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        enabled = false // so clickable works over it
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }

                OutlinedTextField(
                    value = amountText.value,
                    onValueChange = { amountText.value = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = descText.value,
                    onValueChange = { descText.value = it },
                    label = { Text("Details (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(8.dp))

                if (selectedTabIndex == 0) {
                    // Meal Cost - Shoppers Selection
                    Text("Select Shoppers:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(members) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (selectedShoppers.contains(user)) selectedShoppers.remove(user)
                                    else selectedShoppers.add(user)
                                }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedShoppers.contains(user),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedShoppers.add(user) else selectedShoppers.remove(user)
                                    }
                                )
                                Text(user.name.ifEmpty { user.email }, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                } else {
                    // Other Cost
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Type:")
                        Spacer(Modifier.width(8.dp))
                        RadioButton(selected = isShared, onClick = { isShared = true })
                        Text("Shared")
                        Spacer(Modifier.width(8.dp))
                        RadioButton(selected = !isShared, onClick = { isShared = false })
                        Text("Individual")
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    
                    Text(if (isShared) "Target Users (Optional for default all):" else "Select Target Users:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(members) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (targetUsers.contains(user)) targetUsers.remove(user)
                                    else targetUsers.add(user)
                                }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = targetUsers.contains(user),
                                    onCheckedChange = { checked ->
                                        if (checked) targetUsers.add(user) else targetUsers.remove(user)
                                    }
                                )
                                Text(user.name.ifEmpty { user.email }, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    onClick = {
                        val amount = amountText.value.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            scope.launch { snackbarHostState.showSnackbar("Enter a valid amount") }
                            return@Button
                        }
                        
                        if (selectedTabIndex == 1 && !isShared && targetUsers.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Select at least one user for individual cost") }
                            return@Button
                        }

                        scope.launch {
                            val userDetails = authRepository.getCurrentUserDetails().getOrNull()
                            val messId = userDetails?.messId ?: run {
                                snackbarHostState.showSnackbar("No mess assigned"); return@launch
                            }
                            val uid = authRepository.currentUser?.uid ?: ""

                            val expense = if (selectedTabIndex == 0) {
                                Expense(
                                    messId = messId,
                                    amount = amount,
                                    description = descText.value,
                                    date = System.currentTimeMillis(),
                                    addedBy = uid,
                                    expenseCategory = "meal_cost",
                                    expenseDate = selectedDate,
                                    shoppers = selectedShoppers.map { it.uid }
                                )
                            } else {
                                Expense(
                                    messId = messId,
                                    amount = amount,
                                    description = descText.value,
                                    date = System.currentTimeMillis(),
                                    addedBy = uid,
                                    expenseCategory = "other_cost",
                                    otherCostType = if (isShared) "shared" else "individual",
                                    targetUsers = targetUsers.map { it.uid }
                                )
                            }

                            val result = expenseRepository.addExpense(expense)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Cost added")
                                onBack()
                            } else {
                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to add cost")
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

