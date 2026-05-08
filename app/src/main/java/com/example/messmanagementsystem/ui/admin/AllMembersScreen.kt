package com.example.messmanagementsystem.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messmanagementsystem.data.model.Deposit
import com.example.messmanagementsystem.data.model.Expense
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.DepositRepository
import com.example.messmanagementsystem.data.repository.ExpenseRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMembersScreen(
    onBack: () -> Unit,
    onSelectMemberForManager: (User) -> Unit
) {
    val users = remember { mutableStateListOf<User>() }
    val userRepository = remember { UserRepository() }
    val depositRepo = remember { DepositRepository() }
    val expenseRepo = remember { ExpenseRepository() }

    val monthId = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) }
    var deposits by remember { mutableStateOf<List<Deposit>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    LaunchedEffect(Unit) {
        val user = AuthRepository().getCurrentUserDetails().getOrNull()
        user?.messId?.let { messId ->
            users.clear()
            users.addAll(userRepository.getMembersByMess(messId))
            deposits = depositRepo.getDepositsForMessByMonth(messId, monthId)
            expenses = expenseRepo.getExpensesForMessByMonth(messId, monthId)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("All Members") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) }
    ) { paddingValues ->
        MembersList(paddingValues, users, deposits, expenses, onSelectMemberForManager)
    }
}

@Composable
private fun MembersList(
    paddingValues: PaddingValues,
    users: List<User>,
    deposits: List<Deposit>,
    expenses: List<Expense>,
    onSelect: (User) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(8.dp)
    ) {
        items(users) { u ->
            val userDeposit = deposits.filter { it.userId == u.uid }.sumOf { it.amount }
            // For expenses, if it's "meal_cost", we can divide amount by shoppers.size if this user is a shopper
            val mealCost = expenses.filter { it.expenseCategory == "meal_cost" && it.shoppers.contains(u.uid) }
                .sumOf { if (it.shoppers.isNotEmpty()) it.amount / it.shoppers.size else 0.0 }
                
            val indvCost = expenses.filter { it.expenseCategory == "other_cost" && it.otherCostType == "individual" && it.targetUsers.contains(u.uid) }
                .sumOf { if (it.targetUsers.isNotEmpty()) it.amount / it.targetUsers.size else 0.0 }
                
            val sharedCostTotal = expenses.filter { it.expenseCategory == "other_cost" && it.otherCostType == "shared" }
                .sumOf { if (users.isNotEmpty()) it.amount / users.size else 0.0 }
            
            val totalCost = mealCost + indvCost + sharedCostTotal
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .clickable { onSelect(u) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(u.name.ifEmpty { u.email }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(u.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Month Deposit:")
                        Text("৳${"%.2f".format(userDeposit)}")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Month Cost:")
                        Text("৳${"%.2f".format(totalCost)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    val balance = userDeposit - totalCost
                    val balanceColor = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Balance:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "৳${"%.2f".format(balance)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = balanceColor
                        )
                    }
                }
            }
        }
    }
}
