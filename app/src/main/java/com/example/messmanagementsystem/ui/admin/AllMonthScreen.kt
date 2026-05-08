package com.example.messmanagementsystem.ui.admin

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.messmanagementsystem.data.model.MonthInfo
import com.example.messmanagementsystem.data.repository.AuthRepository
import com.example.messmanagementsystem.data.repository.DepositRepository
import com.example.messmanagementsystem.data.repository.ExpenseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var isGeneratingPdf by remember { mutableStateOf(false) }
    
    val depositRepo = remember { DepositRepository() }
    val expenseRepo = remember { ExpenseRepository() }
    var userMessId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = AuthRepository().getCurrentUserDetails().getOrNull()
        val messId = user?.messId
        userMessId = messId
        if (messId == null) return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        val snap = db.collection("messes").document(messId).collection("months").get().await()
        months.clear()
        months.addAll(snap.documents.mapNotNull { it.toObject(MonthInfo::class.java) })
    }

    val generatePdfAndShare = { month: MonthInfo ->
        scope.launch {
            if (isGeneratingPdf) return@launch
            val mId = userMessId ?: return@launch
            isGeneratingPdf = true
            try {
                snackbar.showSnackbar("Generating report for ${month.name.ifEmpty { month.id }}...")
                val deposits = depositRepo.getDepositsForMessByMonth(mId, month.id)
                val expenses = expenseRepo.getExpensesForMessByMonth(mId, month.id)
                
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    textSize = 14f
                }
                
                var yPosition = 40f
                canvas.drawText("Mess Report - ${month.name.ifEmpty { month.id }}", 40f, yPosition, paint)
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
                
                val file = File(context.cacheDir, "Mess_Report_${month.id}.pdf")
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
                snackbar.showSnackbar("Failed to generate PDF")
            } finally {
                isGeneratingPdf = false
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("All Month") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }) },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            items(months) { m ->
                ListItem(
                    modifier = Modifier.clickable { generatePdfAndShare(m) },
                    headlineContent = { Text(m.name.ifEmpty { m.id }) },
                    supportingContent = { Text("Created: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(m.createdAt))}") }
                )
            }
        }
    }
}

