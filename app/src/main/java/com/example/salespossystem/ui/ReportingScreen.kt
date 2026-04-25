package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.util.ExcelHelper
import com.example.salespossystem.util.PrintingService
import com.example.salespossystem.data.Invoice
import com.example.salespossystem.data.Customer
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.data.StockMovementEntry
import java.text.SimpleDateFormat
import java.util.*

enum class ReportingView {
    LIST, FILTERS, DETAILS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingScreen(viewModel: SalesViewModel = viewModel()) {
    var currentView by remember { mutableStateOf(ReportingView.LIST) }
    var selectedReport by remember { mutableStateOf("") }
    var selectedReportGroup by remember { mutableStateOf("") }
    
    val isAdmin = viewModel.currentUser?.accessLevel == "1"
    val currentUserName = viewModel.currentUser?.firstName ?: ""

    // Filter States
    var startDate by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.time) }
    var endDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("23:59") }
    
    var selectedCustomerName by remember { mutableStateOf("All") }
    var selectedStaffName by remember { mutableStateOf(if (isAdmin) "All" else currentUserName) }

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    val invoiceDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val isProductBasedReport = selectedReport == "Products" || selectedReport == "Item list" || selectedReport == "Voided items"
    val isPeopleReport = selectedReport == "Customers" || selectedReport == "Suppliers"
    val isHourlyReport = selectedReport == "Hourly sales"
    val isStockMovementReport = selectedReport == "Stock movement"
    val isLowStockReport = selectedReport == "Low stock warning"
    val isReorderReport = selectedReport == "Reorder product list"

    // Filtering logic
    val filteredInvoices = remember(selectedReport, selectedReportGroup, startDate, endDate, startTime, endTime, selectedCustomerName, selectedStaffName, viewModel.allInvoices.size) {
        viewModel.allInvoices.filter { invoice ->
            try {
                val parts = invoice.date.split(" ")
                val invoiceDatePart = parts[0]
                val invoiceTimePart = if (parts.size > 1) parts[1] else "00:00"
                
                val date = invoiceDateFormat.parse(invoiceDatePart)
                
                val startCal = Calendar.getInstance().apply { time = startDate; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                val endCal = Calendar.getInstance().apply { time = endDate; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
                
                val isWithinDate = date != null && !date.before(startCal.time) && !date.after(endCal.time)
                
                val isWithinTime = invoiceTimePart >= startTime && invoiceTimePart <= endTime

                val matchesType = when (selectedReportGroup) {
                    "Purchase" -> invoice.isPurchase
                    "Loss" -> invoice.paymentMethod == "DAMAGE"
                    "Sales" -> !invoice.isPurchase && invoice.paymentMethod != "DAMAGE"
                    "Stock" -> true // Include everything for stock reports
                    else -> true
                }
                
                val matchesCustomer = selectedCustomerName == "All" || invoice.customerName == selectedCustomerName
                val matchesUser = selectedStaffName == "All" || invoice.userName == selectedStaffName

                isWithinDate && isWithinTime && matchesType && matchesCustomer && matchesUser
            } catch (e: Exception) {
                false
            }
        }
    }

    // People List
    val peopleList = if (selectedReport == "Customers") viewModel.customers.toList() else if (selectedReport == "Suppliers") viewModel.suppliers.toList() else emptyList()

    // Aggregate Product Data
    val aggregatedProducts = if (isProductBasedReport) {
        filteredInvoices.flatMap { it.items }
            .groupBy { it.productName }
            .map { (name, items) ->
                Triple(name, items.sumOf { it.quantity }, items.sumOf { it.price * it.quantity })
            }.sortedByDescending { it.third }
    } else emptyList()

    // Aggregate Hourly Data
    val aggregatedHours = if (isHourlyReport) {
        filteredInvoices.groupBy { 
            try { it.date.split(" ")[1].split(":")[0] } catch(e: Exception) { "00" }
        }.map { (hour, invoices) ->
            Triple("$hour:00", invoices.size, invoices.sumOf { it.totalAmount })
        }.sortedBy { it.first }
    } else emptyList()

    // Stock Movement Data
    val stockMovements = if (isStockMovementReport) {
        filteredInvoices.flatMap { inv ->
            inv.items.map { item ->
                val isReturn = inv.paymentMethod.contains("RETURN", ignoreCase = true)
                val type = when {
                    inv.paymentMethod == "DAMAGE" -> "Damage"
                    inv.isPurchase -> if (isReturn) "Pur. Return" else "Purchase"
                    isReturn -> "Sale Return"
                    else -> "Sale"
                }
                val qtyChange = when {
                    inv.paymentMethod == "DAMAGE" -> -item.quantity.toDouble()
                    inv.isPurchase -> if (isReturn) -item.quantity.toDouble() else item.quantity.toDouble()
                    isReturn -> item.quantity.toDouble()
                    else -> -item.quantity.toDouble()
                }
                StockMovementEntry(inv.date.split(" ")[0], item.productName, qtyChange, type, inv.invoiceNumber)
            }
        }.sortedByDescending { it.date }
    } else emptyList()

    // Low Stock Data (Less than 5 pieces)
    val lowStockProducts = if (isLowStockReport) {
        val movedBarcodes = filteredInvoices.flatMap { it.items }.map { it.productId }.toSet()
        viewModel.products.filter { p ->
            val stock = viewModel.stockMap[p.barcode] ?: 0.0
            val isLow = stock < 5.0
            isLow && (movedBarcodes.contains(p.barcode) || movedBarcodes.isEmpty())
        }.map { it to (viewModel.stockMap[it.barcode] ?: 0.0) }
    } else emptyList()

    // Reorder Data (Less than 10 pieces)
    val reorderProducts = if (isReorderReport) {
        val movedBarcodes = filteredInvoices.flatMap { it.items }.map { it.productId }.toSet()
        viewModel.products.filter { p ->
            val stock = viewModel.stockMap[p.barcode] ?: 0.0
            val needsReorder = stock < 10.0
            needsReorder && (movedBarcodes.contains(p.barcode) || movedBarcodes.isEmpty())
        }.map { it to (viewModel.stockMap[it.barcode] ?: 0.0) }
    } else emptyList()

    // Excel Export
    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val outputStream = context.contentResolver.openOutputStream(it)
                if (outputStream != null) {
                    val success = if (isStockMovementReport) {
                        ExcelHelper.exportStockMovementToExcel(stockMovements, outputStream)
                    } else if (isLowStockReport) {
                        ExcelHelper.exportProductsToExcel(context, lowStockProducts.map { it.first }, viewModel.stockMap, outputStream)
                    } else if (isReorderReport) {
                        ExcelHelper.exportProductsToExcel(context, reorderProducts.map { it.first }, viewModel.stockMap, outputStream)
                    } else {
                        ExcelHelper.exportInvoicesToExcel(filteredInvoices, outputStream)
                    }
                    if (success) Toast.makeText(context, "Excel exported!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Excel Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // PDF/Print
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            val pdfDocument = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                val titlePaint = Paint()
                
                titlePaint.apply { color = AndroidColor.BLACK; textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                canvas.drawText(viewModel.companyName.ifEmpty { "SALES POS SYSTEM" }, 40f, 60f, titlePaint)
                
                paint.apply { color = AndroidColor.DKGRAY; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
                canvas.drawText("REPORT: ${selectedReport.uppercase()}", 40f, 85f, paint)
                if (!isPeopleReport) canvas.drawText("Period: ${dateFormat.format(startDate)} $startTime to ${dateFormat.format(endDate)} $endTime", 40f, 100f, paint)
                
                canvas.drawLine(40f, 115f, 555f, 115f, paint)
                var currentY = 150f
                paint.apply { color = AndroidColor.LTGRAY; style = Paint.Style.FILL }
                canvas.drawRect(40f, currentY - 20f, 555f, currentY + 5f, paint)
                paint.apply { color = AndroidColor.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); style = Paint.Style.FILL }

                if (isPeopleReport) {
                    canvas.drawText("CODE", 45f, currentY, paint)
                    canvas.drawText("NAME", 120f, currentY, paint)
                    canvas.drawText("PHONE", 300f, currentY, paint)
                    canvas.drawText("TAX #", 450f, currentY, paint)
                    paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f }
                    currentY += 25f
                    peopleList.forEach { p ->
                        if (currentY > 800f) return@forEach
                        canvas.drawText(p.code, 45f, currentY, paint); canvas.drawText(p.name.take(30), 120f, currentY, paint); canvas.drawText(p.phone, 300f, currentY, paint); canvas.drawText(p.taxNumber, 450f, currentY, paint)
                        canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint); currentY += 20f
                    }
                } else if (isProductBasedReport) {
                    canvas.drawText("PRODUCT NAME", 45f, currentY, paint); canvas.drawText("QTY", 350f, currentY, paint); canvas.drawText("TOTAL", 450f, currentY, paint)
                    paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f }
                    currentY += 25f
                    aggregatedProducts.forEach { prod ->
                        if (currentY > 800f) return@forEach
                        canvas.drawText(prod.first.take(45), 45f, currentY, paint); canvas.drawText(prod.second.toString(), 350f, currentY, paint); canvas.drawText(String.format(Locale.US, "%.2f", prod.third), 450f, currentY, paint)
                        canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint); currentY += 20f
                    }
                } else if (isStockMovementReport) {
                    canvas.drawText("DATE", 45f, currentY, paint); canvas.drawText("PRODUCT", 120f, currentY, paint); canvas.drawText("TYPE", 350f, currentY, paint); canvas.drawText("QTY", 480f, currentY, paint)
                    paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f }
                    currentY += 25f
                    stockMovements.forEach { sm ->
                        if (currentY > 800f) return@forEach
                        canvas.drawText(sm.date, 45f, currentY, paint); canvas.drawText(sm.productName.take(30), 120f, currentY, paint); canvas.drawText(sm.type, 350f, currentY, paint); canvas.drawText(String.format("%.2f", sm.change), 480f, currentY, paint)
                        canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint); currentY += 20f
                    }
                } else if (isLowStockReport || isReorderReport) {
                    val reportData = if (isLowStockReport) lowStockProducts else reorderProducts
                    canvas.drawText("BARCODE", 45f, currentY, paint); canvas.drawText("PRODUCT NAME", 150f, currentY, paint); canvas.drawText("GROUP", 400f, currentY, paint); canvas.drawText("STOCK", 500f, currentY, paint)
                    paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f }
                    currentY += 25f
                    reportData.forEach { (p, stock) ->
                        if (currentY > 800f) return@forEach
                        canvas.drawText(p.barcode, 45f, currentY, paint); canvas.drawText(p.name.take(35), 150f, currentY, paint); canvas.drawText(p.group.take(15), 400f, currentY, paint); canvas.drawText(String.format("%.2f", stock), 500f, currentY, paint)
                        canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint); currentY += 20f
                    }
                } else {
                    canvas.drawText("DATE", 45f, currentY, paint)
                    canvas.drawText("INVOICE #", 110f, currentY, paint)
                    canvas.drawText("PARTNER", 210f, currentY, paint)
                    canvas.drawText("STAFF", 380f, currentY, paint)
                    canvas.drawText("AMOUNT", 480f, currentY, paint)
                    paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 9f }
                    currentY += 25f
                    filteredInvoices.forEach { inv ->
                        if (currentY > 800f) return@forEach
                        canvas.drawText(inv.date.split(" ")[0], 45f, currentY, paint)
                        canvas.drawText(inv.invoiceNumber, 110f, currentY, paint)
                        canvas.drawText(inv.customerName.take(25), 210f, currentY, paint)
                        canvas.drawText(inv.userName.take(15), 380f, currentY, paint)
                        canvas.drawText(String.format(Locale.US, "%.2f", inv.totalAmount), 480f, currentY, paint)
                        canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint)
                        currentY += 20f
                    }
                }
                pdfDocument.finishPage(page)
                context.contentResolver.openOutputStream(targetUri)?.use { pdfDocument.writeTo(it) }
                Toast.makeText(context, "Report Generated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally { pdfDocument.close() }
        }
    }

    // Dialog States
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (pickingStartDate) startDate.time else endDate.time)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            @OptIn(ExperimentalMaterial3Api::class)
            TextButton(onClick = { datePickerState.selectedDateMillis?.let { if (pickingStartDate) startDate = Date(it) else endDate = Date(it) }; showDatePicker = false }) { Text("OK") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = datePickerState) }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        SecondaryReportingBar(title = if (selectedReport.isEmpty()) "Reports" else selectedReport, onBack = if (currentView != ReportingView.LIST) { {
            currentView = when (currentView) { ReportingView.DETAILS -> ReportingView.FILTERS; ReportingView.FILTERS -> { selectedReport = ""; selectedReportGroup = ""; selectedCustomerName = "All"; selectedStaffName = if (isAdmin) "All" else currentUserName; startTime = "00:00"; endTime = "23:59"; ReportingView.LIST }; else -> ReportingView.LIST }
        } } else null)

        when (currentView) {
            ReportingView.LIST -> { ReportListArea(onReportSelected = { report, group -> selectedReport = report; selectedReportGroup = group; selectedCustomerName = "All"; selectedStaffName = if (isAdmin) "All" else currentUserName; startTime = "00:00"; endTime = "23:59"; currentView = ReportingView.FILTERS }, modifier = Modifier.weight(1f)) }
            ReportingView.FILTERS -> { ReportingFilterSection(
                reportName = selectedReport, reportGroup = selectedReportGroup, 
                startDate = dateFormat.format(startDate), endDate = dateFormat.format(endDate),
                startTime = startTime, endTime = endTime,
                selectedCustomer = selectedCustomerName, selectedStaff = selectedStaffName,
                customers = if (selectedReportGroup == "Purchase") listOf("All") + viewModel.suppliers.map { it.name } else listOf("All") + viewModel.customers.map { it.name },
                staffList = remember(viewModel.allInvoices.size) {
                    if (isAdmin) {
                        val namesFromInvoices = viewModel.allInvoices.map { it.userName }.toSet()
                        val namesFromUsers = viewModel.users.map { it.firstName }.toSet()
                        val allNames = (namesFromInvoices + namesFromUsers + "Admin").filter { it.isNotEmpty() }.toSet()
                        listOf("All") + allNames.sorted()
                    } else {
                        listOf(currentUserName)
                    }
                },
                isAdmin = isAdmin,
                onStartDateClick = { pickingStartDate = true; showDatePicker = true },
                onEndDateClick = { pickingStartDate = false; showDatePicker = true },
                onStartTimeChange = { startTime = it },
                onEndTimeChange = { endTime = it },
                onCustomerChange = { selectedCustomerName = it }, onStaffChange = { selectedStaffName = it },
                onShowReport = { currentView = ReportingView.DETAILS },
                onPrint = {
                    val period = "${dateFormat.format(startDate)} $startTime to ${dateFormat.format(endDate)} $endTime"
                    if (isPeopleReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, "All", listOf("Code", "Name", "Phone", "TRN"), peopleList.map { listOf(it.code, it.name, it.phone, it.taxNumber) }, currency = viewModel.currencySymbol)
                    else if (isProductBasedReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Product", "Qty", "Total"), aggregatedProducts.map { listOf(it.first, it.second.toString(), String.format("%.2f", it.third)) }, aggregatedProducts.sumOf { it.third }, currency = viewModel.currencySymbol)
                    else if (isHourlyReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Hour", "Invoices", "Total"), aggregatedHours.map { listOf(it.first, it.second.toString(), String.format("%.2f", it.third)) }, aggregatedHours.sumOf { it.third }, currency = viewModel.currencySymbol)
                    else if (isStockMovementReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Date", "Product", "Ref", "Type", "Qty"), stockMovements.map { listOf(it.date, it.productName, it.reference, it.type, String.format("%.2f", it.change)) }, currency = viewModel.currencySymbol)
                    else if (isLowStockReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Barcode", "Product", "Group", "Stock"), lowStockProducts.map { listOf(it.first.barcode, it.first.name, it.first.group, String.format("%.2f", it.second)) }, currency = viewModel.currencySymbol)
                    else if (isReorderReport) PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Barcode", "Product", "Group", "Stock"), reorderProducts.map { listOf(it.first.barcode, it.first.name, it.first.group, String.format("%.2f", it.second)) }, currency = viewModel.currencySymbol)
                    else PrintingService.printReport(context, selectedReport, viewModel.companyName, viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber, period, listOf("Date", "Inv #", "Partner", "Staff", "Amount"), filteredInvoices.map { listOf(it.date.split(" ")[0], it.invoiceNumber, it.customerName, it.userName, String.format("%.2f", it.totalAmount)) }, filteredInvoices.sumOf { it.totalAmount }, currency = viewModel.currencySymbol)
                },
                onExcel = { excelLauncher.launch("${selectedReport.replace(" ", "_").lowercase()}_report.xlsx") },
                onPdf = { pdfLauncher.launch("${selectedReport.replace(" ", "_").lowercase()}_report.pdf") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) }
            ReportingView.DETAILS -> { ReportDetailsArea(
                reportName = selectedReport, 
                reportGroup = selectedReportGroup, 
                startDate = startDate, 
                endDate = endDate, 
                filteredInvoices = filteredInvoices, 
                aggregatedProducts = aggregatedProducts, 
                aggregatedHours = aggregatedHours, 
                peopleList = peopleList, 
                isProductBased = isProductBasedReport, 
                isPeopleBased = isPeopleReport, 
                isHourlyBased = isHourlyReport, 
                customerFilter = selectedCustomerName,
                modifier = Modifier.weight(1f),
                stockMovements = stockMovements,
                isStockMovement = isStockMovementReport,
                lowStockProducts = lowStockProducts,
                isLowStock = isLowStockReport,
                reorderProducts = reorderProducts,
                isReorderReport = isReorderReport
            ) }
        }
    }
}

@Composable
fun SecondaryReportingBar(title: String, onBack: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).background(HeaderBackground).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
        Row(modifier = Modifier.weight(1f).background(SidebarSelected, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(title, color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp)); IconButton(onClick = { }) { Icon(Icons.Default.FilterList, "Filters", tint = Color.White) }
    }
}

@Composable
fun ReportListArea(onReportSelected: (String, String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Select report to view or print", color = Color.White, fontSize = 16.sp); Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ReportGroupHeader("Sales") }; items(salesReports) { report -> ReportItem(report, onClick = { onReportSelected(report, "Sales") }) }
            item { Spacer(modifier = Modifier.height(16.dp)) }; item { ReportGroupHeader("Purchase") }; items(purchaseReports) { report -> ReportItem(report, onClick = { onReportSelected(report, "Purchase") }) }
            item { Spacer(modifier = Modifier.height(16.dp)) }; item { ReportGroupHeader("Loss and damage") }; items(lossReports) { report -> ReportItem(report, onClick = { onReportSelected(report, "Loss") }) }
            item { Spacer(modifier = Modifier.height(16.dp)) }; item { ReportGroupHeader("Stock control") }; items(stockReports) { report -> ReportItem(report, onClick = { onReportSelected(report, "Stock") }) }
        }
    }
}

@Composable
fun ReportGroupHeader(title: String) {
    Column { Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray) }
}

@Composable
fun ReportItem(name: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Description, null, tint = Color.Gray, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(12.dp)); Text(name, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (name == "Customers" || name == "Purchased items discounts") Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun ReportingFilterSection(
    reportName: String, reportGroup: String, startDate: String, endDate: String,
    startTime: String, endTime: String,
    selectedCustomer: String, selectedStaff: String,
    customers: List<String>, staffList: List<String>, 
    isAdmin: Boolean,
    onStartDateClick: () -> Unit, onEndDateClick: () -> Unit, 
    onStartTimeChange: (String) -> Unit, onEndTimeChange: (String) -> Unit,
    onCustomerChange: (String) -> Unit, onStaffChange: (String) -> Unit, 
    onShowReport: () -> Unit, onPrint: () -> Unit, onExcel: () -> Unit, onPdf: () -> Unit, modifier: Modifier = Modifier
) {
    val hoursList = (0..23).map { String.format("%02d:00", it) }
    
    Column(modifier = modifier.background(Color(0xFF202020)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Filters for $reportName", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))
        
        if (reportName != "Customers" && reportName != "Suppliers") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FilterLabel("Start Date")
                    Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp)).clickable { onStartDateClick() }.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(startDate, color = Color.White, fontSize = 12.sp) }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FilterLabel("End Date")
                    Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp)).clickable { onEndDateClick() }.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(endDate, color = Color.White, fontSize = 12.sp) }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FilterLabel("Start Time")
                    FilterDropdownField(startTime, hoursList, onStartTimeChange)
                }
                Column(modifier = Modifier.weight(1f)) {
                    FilterLabel("End Time")
                    FilterDropdownField(endTime, hoursList, onEndTimeChange)
                }
            }

            FilterLabel(if (reportGroup == "Purchase") "Suppliers" else "Customers"); FilterDropdownField(selectedCustomer, customers, onCustomerChange)
            FilterLabel("Staff"); FilterDropdownField(selectedStaff, staffList, onStaffChange, enabled = isAdmin)
            FilterLabel("Cash register"); FilterDropdownField("All", listOf("All"), {})
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) { Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = SidebarSelected)); Text("Include subgroups", color = Color.White, fontSize = 14.sp) }
        } else { Text("This report shows results for selected parameters.", color = Color.Gray, fontSize = 14.sp) }

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShowReport, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Show Report") }
            FilterButton(Icons.Default.Print, "Print", onPrint, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterButton(Icons.Default.TableChart, "Excel", onExcel, Modifier.weight(1f)); FilterButton(Icons.Default.PictureAsPdf, "PDF", onPdf, Modifier.weight(1f))
        }
    }
}

@Composable
fun ReportDetailsArea(
    reportName: String, 
    reportGroup: String, 
    startDate: Date, 
    endDate: Date, 
    filteredInvoices: List<Invoice>, 
    aggregatedProducts: List<Triple<String, Double, Double>>,
    aggregatedHours: List<Triple<String, Int, Double>>, 
    peopleList: List<Customer>, 
    isProductBased: Boolean, 
    isPeopleBased: Boolean, 
    isHourlyBased: Boolean, 
    customerFilter: String,
    modifier: Modifier = Modifier,
    stockMovements: List<StockMovementEntry> = emptyList(), 
    isStockMovement: Boolean = false,
    lowStockProducts: List<Pair<ProductItem, Double>> = emptyList(),
    isLowStock: Boolean = false,
    reorderProducts: List<Pair<ProductItem, Double>> = emptyList(),
    isReorderReport: Boolean = false
) {
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    val totalAmount = if (isProductBased) aggregatedProducts.sumOf { it.third } else if (isHourlyBased) aggregatedHours.sumOf { it.third } else if (!isPeopleBased && !isStockMovement && !isLowStock && !isReorderReport) filteredInvoices.sumOf { it.totalAmount } else 0.0
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Report: $reportName", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (!isPeopleBased) {
            val periodStr = "${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
            Text("Period: $periodStr", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isPeopleBased) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f)); Text("Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f)); Text("Phone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f)); Text("Tax #", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(peopleList) { p -> Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text(p.code, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(0.8f)); Text(p.name, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1.5f)); Text(p.phone, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1.2f)); Text(p.taxNumber, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1f)) }; HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) } }
        } else if (isProductBased) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Product Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f)); Text("Qty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.5f)); Text("Total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(aggregatedProducts) { prod -> Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text(prod.first, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(2f)); Text(String.format(Locale.US, "%.2f", prod.second), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(0.5f)); Text(String.format(Locale.US, "%.2f", prod.third), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f)) }; HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) }; item { ReportTotalRow(totalAmount) } }
        } else if (isHourlyBased) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hour", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("Inv Count", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text("Total Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(aggregatedHours) { h -> Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text(h.first, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(h.second.toString(), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Text(String.format(Locale.US, "%.2f", h.third), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }; HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) }; item { ReportTotalRow(totalAmount) } }
        } else if (isStockMovement) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text("Product", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f)); Text("Type", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f)); Text("Qty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(stockMovements) { sm -> Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text(sm.date, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(sm.productName, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1.5f)); Text(sm.type, color = if (sm.change > 0) Color.Green else Color.Red, fontSize = 11.sp, modifier = Modifier.weight(0.8f)); Text(String.format("%.2f", sm.change), color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End) }; HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) } }
        } else if (isLowStock || isReorderReport) {
            val reportData = if (isLowStock) lowStockProducts else reorderProducts
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Barcode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text("Product", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f)); Text("Stock", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { items(reportData) { (p, stock) -> Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { Text(p.barcode, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(p.name, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(2f)); Text(String.format("%.2f", stock), color = Color.Red, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End) }; HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) } }
        } else {
            Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                Text("Inv #", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                Text("Partner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                Text("Staff", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                Text("Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) { 
                items(filteredInvoices) { invoice -> 
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) { 
                        Text(invoice.date.split(" ")[0], color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                        Text(invoice.invoiceNumber, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text(invoice.customerName.take(15), color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text(invoice.userName, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                        Text(String.format(Locale.US, "%.2f", invoice.totalAmount), color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f)) 
                }
                item { ReportTotalRow(totalAmount) } 
            }
        }
    }
}

@Composable
fun ReportTotalRow(total: Double) { Spacer(modifier = Modifier.height(16.dp)); Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { Text("Total Amount: ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(String.format(Locale.US, "%.2f", total), color = SidebarSelected, fontWeight = FontWeight.Bold, fontSize = 18.sp) } }

@Composable
fun FilterLabel(text: String) { Text(text, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }

@Composable
fun FilterDropdownField(value: String, options: List<String>, onSelectionChange: (String) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().background(if(enabled) Color.Black else Color.DarkGray.copy(alpha = 0.3f)).border(1.dp, Color.DarkGray).clickable(enabled = enabled) { expanded = true }.padding(horizontal = 12.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text(value, color = if(enabled) Color.White else Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null, tint = if(enabled) Color.White else Color.Gray) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.8f).background(DashboardBackground)) { options.forEach { option -> DropdownMenuItem(text = { Text(option, color = Color.White) }, onClick = { onSelectionChange(option); expanded = false }) } }
    }
}

@Composable
fun FilterButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(48.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray), contentPadding = PaddingValues(horizontal = 8.dp)) { Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(label, fontSize = 13.sp) }
}

val salesReports = listOf("Products", "Product groups", "Customers", "Tax rates", "Users", "Item list", "Payment types", "Payment types by users", "Payment types by customers", "Refunds", "Invoice list", "Daily sales", "Hourly sales", "Hourly sales by product groups", "Table or order number", "Profit & margin", "Unpaid sales", "Starting cash entries", "Voided items", "Discounts granted", "Items discounts")
val purchaseReports = listOf("Products", "Suppliers", "Unpaid purchase", "Purchase discounts", "Purchased items discounts", "Purchase invoice list", "Tax rates")
val lossReports = listOf("Products")
val stockReports = listOf("Reorder product list", "Low stock warning", "Stock movement")
