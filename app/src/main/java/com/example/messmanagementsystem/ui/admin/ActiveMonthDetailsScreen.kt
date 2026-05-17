package com.example.messmanagementsystem.ui.admin

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.messmanagementsystem.data.model.Deposit
import com.example.messmanagementsystem.data.model.Expense
import com.example.messmanagementsystem.data.model.MealSelection
import com.example.messmanagementsystem.data.model.User
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.DepositRepository
import com.example.messmanagementsystem.data.repository.ExpenseRepository
import com.example.messmanagementsystem.data.repository.MealRepository
import com.example.messmanagementsystem.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveMonthDetailsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val depositRepo = remember { DepositRepository() }
    val expenseRepo = remember { ExpenseRepository() }
    val userRepository = remember { UserRepository() }
    val mealRepo = remember { MealRepository() }

    val monthId = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) }
    
    var deposits by remember { mutableStateOf<List<Deposit>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var meals by remember { mutableStateOf<List<MealSelection>>(emptyList()) }
    
    val tabs = listOf("Meal", "Deposit", "Meal Cost", "Individual Other Cost", "Shared Other Cost")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val user = AuthRepository().getCurrentUserDetails().getOrNull()
        val messId = user?.messId ?: return@LaunchedEffect
        
        val users = userRepository.getMembersByMess(messId)
        members = users
        
        deposits = depositRepo.getDepositsForMessByMonth(messId, monthId)
        expenses = expenseRepo.getExpensesForMessByMonth(messId, monthId)
        meals = mealRepo.getMealsForMessByMonth(users.map { it.uid }, monthId)
    }

    val generatePdfAndShare = {
        scope.launch {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    textSize = 14f
                }
                
                var yPosition = 40f
                canvas.drawText("Mess Report - Month: $monthId", 40f, yPosition, paint)
                yPosition += 30f
                
                canvas.drawText("Total Deposits: ৳${deposits.sumOf { it.amount }}", 40f, yPosition, paint)
                yPosition += 20f
                canvas.drawText("Total Expenses: ৳${expenses.sumOf { it.amount }}", 40f, yPosition, paint)
                yPosition += 30f

                canvas.drawText("--- Deposits ---", 40f, yPosition, paint)
                yPosition += 20f
                deposits.forEach {
                    canvas.drawText("Amount: ৳${it.amount} | Note: ${it.note}", 40f, yPosition, paint)
                    yPosition += 20f
                }
                yPosition += 10f

                canvas.drawText("--- Expenses ---", 40f, yPosition, paint)
                yPosition += 20f
                expenses.forEach {
                    val desc = if (it.description.isNotEmpty()) it.description else it.expenseCategory
                    canvas.drawText("Amount: ৳${it.amount} | $desc", 40f, yPosition, paint)
                    yPosition += 20f
                }

                pdfDocument.finishPage(page)
                
                val file = File(context.cacheDir, "Mess_Report_$monthId.pdf")
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Active Month Data", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { generatePdfAndShare() }) {
                        Icon(Icons.Default.Share, contentDescription = "Download Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary, actionIconContentColor = MaterialTheme.colorScheme.onPrimary)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 0.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> { // Meal
                        val totalBreakfast = meals.count { it.hasBreakfast }
                        val totalLunch = meals.count { it.hasLunch }
                        val totalDinner = meals.count { it.hasDinner }
                        val totalMeals = totalBreakfast + totalLunch + totalDinner
                        item { 
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Total Meals: $totalMeals", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Breakfast: $totalBreakfast")
                                    Text("Lunch: $totalLunch")
                                    Text("Dinner: $totalDinner")
                                }
                            }
                        }
                    }
                    1 -> { // Deposit
                        items(deposits) { deposit ->
                            ListItem(
                                headlineContent = { Text("৳${deposit.amount}") },
                                supportingContent = { 
                                    val userName = members.find { m -> m.uid == deposit.userId }?.name ?: "Unknown"
                                    Text("User: $userName" + (if (deposit.note.isNotEmpty()) " | Note: ${deposit.note}" else "")) 
                                },
                                overlineContent = { Text(formatDate(deposit.date)) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        if (deposits.isEmpty()) { item { Text("No deposits this month.") } }
                    }
                    2 -> { // Meal Cost
                        val mealCosts = expenses.filter { it.expenseCategory == "meal_cost" }
                        items(mealCosts) { exp ->
                            ListItem(
                                headlineContent = { Text("৳${exp.amount}") },
                                supportingContent = { 
                                    val shopperNames = exp.shoppers.mapNotNull { sid -> members.find { m -> m.uid == sid }?.name }.joinToString()
                                    Text("Shoppers: ${shopperNames.ifEmpty { "None" }}" + (if (exp.description.isNotEmpty()) " | ${exp.description}" else "")) 
                                },
                                overlineContent = { Text(formatDate(exp.expenseDate.takeIf { it > 0 } ?: exp.date)) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        if (mealCosts.isEmpty()) { item { Text("No meal costs this month.") } }
                    }
                    3 -> { // Individual Other Cost
                        val individualCosts = expenses.filter { it.expenseCategory == "other_cost" && it.otherCostType == "individual" }
                        items(individualCosts) { exp ->
                            ListItem(
                                headlineContent = { Text("৳${exp.amount}") },
                                supportingContent = { 
                                    val targetNames = exp.targetUsers.mapNotNull { sid -> members.find { m -> m.uid == sid }?.name }.joinToString()
                                    Text("Users: ${targetNames.ifEmpty { "None" }} | ${exp.description}") 
                                },
                                overlineContent = { Text(formatDate(exp.date)) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        if (individualCosts.isEmpty()) { item { Text("No individual costs this month.") } }
                    }
                    4 -> { // Shared Other Cost
                        val sharedCosts = expenses.filter { it.expenseCategory == "other_cost" && it.otherCostType == "shared" }
                        items(sharedCosts) { exp ->
                            ListItem(
                                headlineContent = { Text("৳${exp.amount}") },
                                supportingContent = { Text("Shared among all members | ${exp.description}") },
                                overlineContent = { Text(formatDate(exp.date)) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        if (sharedCosts.isEmpty()) { item { Text("No shared costs this month.") } }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
