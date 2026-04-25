package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.CardBackground
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.ui.theme.SidebarSelected
import java.io.File
import java.io.FileOutputStream
import java.util.*

@Composable
fun CloudBackupScreen(viewModel: SalesViewModel = viewModel()) {
    val context = LocalContext.current
    var isBackingUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf(viewModel.backupEmail.ifEmpty { viewModel.currentUser?.email ?: "" }) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val json = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (json != null) {
                    viewModel.restoreFromJson(json)
                    Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to restore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            tint = SidebarSelected,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Cloud Backup & Restore",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Secure your data and restore anytime",
            color = Color.Gray,
            fontSize = 13.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox(Modifier.weight(1f), "Products", viewModel.products.size.toString(), Icons.Default.Inventory)
            StatBox(Modifier.weight(1f), "Customers", viewModel.customers.size.toString(), Icons.Default.People)
            StatBox(Modifier.weight(1f), "Invoices", viewModel.allInvoices.size.toString(), Icons.AutoMirrored.Filled.ReceiptLong)
        }

        Spacer(modifier = Modifier.height(16.dp))

        BackupInfoCard(
            icon = Icons.Default.History,
            label = "Last Successful Backup",
            value = viewModel.lastBackupDate
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Cloud Account Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SidebarSelected,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = SidebarSelected
            ),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                isBackingUp = true
                val date = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                viewModel.saveBackupInfo(email, date)
                backupAndShareFiles(context, viewModel)
                isBackingUp = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected),
            shape = RoundedCornerShape(12.dp),
            enabled = !isBackingUp && email.isNotEmpty()
        ) {
            if (isBackingUp) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...")
            } else {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backup to Google Drive", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { filePickerLauncher.launch("application/json") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SidebarSelected),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SidebarSelected)
        ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore Data from JSON", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Backup generates a PDF for viewing and a JSON file for restoring. Save both to your Google Drive.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, count: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        color = CardBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = SidebarSelected, modifier = Modifier.size(20.dp))
            Text(count, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun BackupInfoCard(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.LightGray)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, color = Color.Gray, fontSize = 12.sp)
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun backupAndShareFiles(context: Context, viewModel: SalesViewModel) {
    try {
        val uris = ArrayList<android.net.Uri>()
        val timestamp = System.currentTimeMillis()

        // 1. Generate Full PDF with Data
        val pdfFile = File(context.cacheDir, "POS_Backup_$timestamp.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var y = 40f

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("POS SYSTEM DATA BACKUP", 40f, y, paint)
        y += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Date: ${viewModel.lastBackupDate}", 40f, y, paint)
        y += 40f

        // Products in PDF
        paint.isFakeBoldText = true
        canvas.drawText("PRODUCTS LIST:", 40f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        viewModel.products.forEach {
            if (y > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
            canvas.drawText("- ${it.name} | Price: ${it.salePrice} | Code: ${it.code}", 50f, y, paint)
            y += 15f
        }
        y += 30f

        // Invoices in PDF
        if (y > 800) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }
        paint.isFakeBoldText = true
        canvas.drawText("INVOICES SUMMARY:", 40f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        viewModel.allInvoices.forEach {
            if (y > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
            canvas.drawText("- No: ${it.invoiceNumber} | Total: ${it.totalAmount} | Date: ${it.date}", 50f, y, paint)
            y += 15f
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(FileOutputStream(pdfFile))
        pdfDocument.close()
        uris.add(androidx.core.content.FileProvider.getUriForFile(context, "com.example.salespossystem.fileprovider", pdfFile))

        // 2. Generate JSON for Restore
        val jsonFile = File(context.cacheDir, "POS_Restore_Data_$timestamp.json")
        jsonFile.writeText(viewModel.generateBackupJson())
        uris.add(androidx.core.content.FileProvider.getUriForFile(context, "com.example.salespossystem.fileprovider", jsonFile))

        // 3. Share Both Files
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save Backup Files to Drive"))

    } catch (e: Exception) {
        android.util.Log.e("Backup", "Error: ${e.message}")
    }
}
