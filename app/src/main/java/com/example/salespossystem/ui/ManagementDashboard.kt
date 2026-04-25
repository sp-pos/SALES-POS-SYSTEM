@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.content.Intent
import androidx.core.net.toUri
import com.example.salespossystem.AppDestinations
import com.example.salespossystem.data.*
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme
import com.example.salespossystem.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ManagementDashboard(
    viewModel: SalesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onNavigate: (AppDestinations) -> Unit = {}
) {
    val adminId = viewModel.currentUser?.uid ?: ""

    LaunchedEffect(adminId) {
        if (adminId.isNotEmpty()) {
            authViewModel.fetchStaffUsers(adminId)
        }
    }

    ManagementDashboardContent(
        currencySymbol = viewModel.currencySymbol,
        allInvoices = viewModel.allInvoices,
        expenses = viewModel.expenses,
        staffUsers = authViewModel.staffUsers,
        onNavigate = onNavigate
    )
}

@Composable
fun ManagementDashboardContent(
    currencySymbol: String,
    allInvoices: List<Invoice>,
    expenses: List<Expense>,
    staffUsers: List<User>,
    onNavigate: (AppDestinations) -> Unit = {}
) {
    var selectedDateRange by remember { mutableStateOf("Today") }
    var selectedStaff by remember { mutableStateOf<User?>(null) }
    var showDateMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF1976D2)).statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate(AppDestinations.HOME) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "SALES POS SYSTEM ADMIN",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedStaff != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Staff: ${selectedStaff?.name}",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear Staff Filter",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp).clickable { selectedStaff = null }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp).clickable { showDateMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedDateRange, color = Color.White, fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = showDateMenu, onDismissRequest = { showDateMenu = false }) {
                            listOf("Today", "Yesterday", "Last 7 Days", "This Month").forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range) },
                                    onClick = {
                                        selectedDateRange = range
                                        showDateMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { 
                            Toast.makeText(context, "Notifications Clicked", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        BadgedBox(
                            badge = { Badge { Text("3") } }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
                .verticalScroll(rememberScrollState())
        ) {
            DashboardBody(
                currencySymbol, 
                allInvoices, 
                expenses, 
                staffUsers, 
                selectedDateRange, 
                selectedStaff,
                { selectedStaff = it },
                onNavigate
            )
        }
    }
}

@Composable
fun DashboardBody(
    currencySymbol: String,
    allInvoices: List<Invoice>,
    allExpenses: List<Expense>,
    staffUsers: List<User>,
    selectedDateRange: String,
    selectedStaff: User?,
    onStaffSelected: (User?) -> Unit,
    onNavigate: (AppDestinations) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val today = dateFormat.format(Date())
    
    fun isDateInRange(dateStr: String, range: String): Boolean {
        val invoiceDate = try { dateFormat.parse(dateStr) } catch(_: Exception) { return false } ?: return false
        val cal = Calendar.getInstance()
        val now = cal.time
        
        return when (range) {
            "Today" -> dateStr.startsWith(today)
            "Yesterday" -> {
                cal.add(Calendar.DATE, -1)
                val yesterdayStr = dateFormat.format(cal.time)
                dateStr.startsWith(yesterdayStr)
            }
            "Last 7 Days" -> {
                cal.add(Calendar.DATE, -6)
                val limit = cal.time
                invoiceDate.after(limit) || dateStr.startsWith(dateFormat.format(limit))
            }
            "This Month" -> {
                val monthYear = SimpleDateFormat("MM-yyyy", Locale.US).format(now)
                dateStr.contains(monthYear)
            }
            "DayBeforeYesterday" -> {
                cal.add(Calendar.DATE, -2)
                dateStr.startsWith(dateFormat.format(cal.time))
            }
            "Prev7Days" -> {
                val calEnd = Calendar.getInstance(); calEnd.add(Calendar.DATE, -7)
                val calStart = Calendar.getInstance(); calStart.add(Calendar.DATE, -13)
                (invoiceDate.after(calStart.time) || dateStr.startsWith(dateFormat.format(calStart.time))) && 
                (invoiceDate.before(calEnd.time) || dateStr.startsWith(dateFormat.format(calEnd.time)))
            }
            "PrevMonth" -> {
                cal.add(Calendar.MONTH, -1)
                val monthYear = SimpleDateFormat("MM-yyyy", Locale.US).format(cal.time)
                dateStr.contains(monthYear)
            }
            else -> true
        }
    }

    val filteredInvoices = allInvoices.filter { 
        isDateInRange(it.date, selectedDateRange) && (selectedStaff == null || it.userName == selectedStaff.name) 
    }
    val filteredExpenses = allExpenses.filter { 
        isDateInRange(it.date, selectedDateRange) && (selectedStaff == null || it.userName == selectedStaff.name) 
    }

    val totalSales = filteredInvoices.sumOf { it.totalAmount }
    val totalOrders = filteredInvoices.size
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val netProfit = totalSales - totalExpenses

    // Trend calculation logic
    val prevRange = when (selectedDateRange) {
        "Today" -> "Yesterday"
        "Yesterday" -> "DayBeforeYesterday"
        "Last 7 Days" -> "Prev7Days"
        "This Month" -> "PrevMonth"
        else -> ""
    }

    val prevInvoices = if (prevRange.isNotEmpty()) allInvoices.filter { 
        isDateInRange(it.date, prevRange) && (selectedStaff == null || it.userName == selectedStaff.name) 
    } else emptyList()
    val prevExpenses = if (prevRange.isNotEmpty()) allExpenses.filter { 
        isDateInRange(it.date, prevRange) && (selectedStaff == null || it.userName == selectedStaff.name) 
    } else emptyList()

    val prevSales = prevInvoices.sumOf { it.totalAmount }
    val prevTotalExpenses = prevExpenses.sumOf { it.amount }
    val prevNetProfit = prevSales - prevTotalExpenses

    fun calculateTrend(current: Double, previous: Double): Triple<String?, Boolean, Boolean> {
        if (prevRange.isEmpty() || previous == 0.0) return Triple(null, true, true)
        val diff = ((current - previous) / previous) * 100
        return Triple(
            String.format(Locale.US, "%.0f%%", Math.abs(diff)),
            diff >= 0, // isUp
            diff >= 0  // isGood (for sales/profit)
        )
    }

    val (sTrend, sUp, sGood) = calculateTrend(totalSales, prevSales)
    val (eTrend, eUp, _) = calculateTrend(totalExpenses.toDouble(), prevTotalExpenses.toDouble())
    val (pTrend, pUp, pGood) = calculateTrend(netProfit, prevNetProfit)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCardNew("Total Sales", "$currencySymbol ${String.format(Locale.US, "%,.0f", totalSales)}", sTrend, sUp, sGood, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            StatCardNew("Total Orders", totalOrders.toString(), null, true, true, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            // Expenses: if Up, it's NOT good (Red)
            StatCardNew("Expenses", "$currencySymbol ${String.format(Locale.US, "%,.0f", totalExpenses)}", eTrend, eUp, !eUp, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            StatCardNew("Net Profit", "$currencySymbol ${String.format(Locale.US, "%,.0f", netProfit)}", pTrend, pUp, pGood, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Staff Sales Analytics Chart
        AnalyticsSection(staffUsers, if (selectedStaff != null) filteredInvoices else allInvoices, currencySymbol, selectedStaff)

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Staff / POS Operators Table
        StaffTableSection(staffUsers, filteredInvoices, currencySymbol, selectedStaff, onStaffSelected, onNavigate)

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Bottom Grid (Top Customers & Top Products)
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) { TopCustomersSection(filteredInvoices, onNavigate, currencySymbol) }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) { TopProductsSection(filteredInvoices, onNavigate) }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 5. Messages & Map (Placeholders)
        MessagesAndMapSection(onNavigate)
    }
}

@Composable
fun StatCardNew(title: String, value: String, trend: String?, isUp: Boolean, isGood: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (trend != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isGood) Color(0xFF4CAF50) else Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(trend, fontSize = 11.sp, color = if (isGood) Color(0xFF4CAF50) else Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSection(staffUsers: List<User>, allInvoices: List<Invoice>, currencySymbol: String, selectedStaff: User? = null) {
    var selectedFilter by remember { mutableStateOf("Daily") }
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val today = dateFormat.format(Date())

    val filteredForAnalytics = remember(selectedFilter, allInvoices) {
        allInvoices.filter { inv ->
            val cal = Calendar.getInstance()
            when (selectedFilter) {
                "Daily" -> inv.date.startsWith(today)
                "Weekly" -> {
                    val invoiceDate = try { dateFormat.parse(inv.date) } catch (_: Exception) { null }
                    if (invoiceDate != null) {
                        cal.add(Calendar.DATE, -7)
                        invoiceDate.after(cal.time) || inv.date.startsWith(dateFormat.format(cal.time))
                    } else false
                }
                "Monthly" -> {
                    val monthYear = SimpleDateFormat("MM-yyyy", Locale.US).format(Date())
                    inv.date.contains(monthYear)
                }
                else -> true
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.StackedLineChart, contentDescription = null, tint = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Staff Sales Analytics", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1976D2))
                }
                Row {
                    FilterChip("Daily", selectedFilter == "Daily") { selectedFilter = "Daily" }
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip("Weekly", selectedFilter == "Weekly") { selectedFilter = "Weekly" }
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip("Monthly", selectedFilter == "Monthly") { selectedFilter = "Monthly" }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple Custom Line Chart using Canvas
            val topStaffData = staffUsers.map { staff ->
                val sales = filteredForAnalytics.filter { it.userName == staff.name }.sumOf { it.totalAmount }
                staff.name to sales
            }.sortedByDescending { it.second }.take(5)

            val maxSale = topStaffData.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
            val lineColors = listOf(Color(0xFF1976D2), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFE91E63), Color(0xFF9C27B0))

            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    topStaffData.forEachIndexed { staffIndex, staffPair ->
                        val salesAmount = staffPair.second
                        val color = lineColors.getOrElse(staffIndex) { Color.Gray }
                        
                        // প্রতিটি স্টাফের জন্য একটি লাইন (শুরু থেকে তাদের সেলস ভ্যালু পর্যন্ত)
                        val path = Path().apply {
                            val startX = 0f
                            val startY = size.height
                            val endX = size.width * ((staffIndex + 1) / 5f)
                            val endY = size.height - (salesAmount.toFloat() / maxSale.toFloat() * size.height * 0.8f)
                            
                            moveTo(startX, startY)
                            quadraticTo(endX / 2, startY, endX, endY)
                        }
                        drawPath(path, color = color, style = Stroke(width = 3.dp.toPx()))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend for Top 5 Staff
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 3
            ) {
                topStaffData.forEachIndexed { index, pair ->
                    StaffLegend(
                        pair.first, 
                        lineColors.getOrElse(index) { Color.Gray }, 
                        String.format(Locale.US, "%,.0f", pair.second),
                        currencySymbol
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF1976D2) else Color(0xFFF0F2F5),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = if (selected) Color.White else Color.Gray)
    }
}

@Composable
fun StaffLegend(name: String, color: Color, amount: String, currencySymbol: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text("$currencySymbol $amount", fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun StaffTableSection(
    staffUsers: List<User>, 
    invoices: List<Invoice>, 
    currencySymbol: String, 
    selectedStaff: User?,
    onStaffSelected: (User?) -> Unit,
    onNavigate: (AppDestinations) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Staff / POS Operators",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1976D2)
                )
                Button(
                    onClick = { onNavigate(AppDestinations.MANAGEMENT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New Staff", fontSize = 11.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).padding(8.dp)) {
                Text("Name", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("POS ID", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Sales", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Status", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            
            // Rows
            val staffList = staffUsers
            staffList.forEach { staff ->
                val sales = invoices.filter { it.userName == staff.name }.sumOf { it.totalAmount }
                val isSelected = selectedStaff?.uid == staff.uid
                
                HorizontalDivider(color = if (isSelected) Color(0xFF1976D2) else Color(0xFFEEEEEE), thickness = if (isSelected) 2.dp else 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                        .clickable { 
                            if (isSelected) onStaffSelected(null) else onStaffSelected(staff)
                        }
                        .padding(8.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                            Text(staff.name.take(1), color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(staff.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("POS-0${staffList.indexOf(staff)+1}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Text("POS-0${staffList.indexOf(staff)+1}", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color(0xFF1976D2))
                    Text("$currencySymbol ${String.format(Locale.US, "%,.0f", sales)}", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if(!staff.isLocked) Color(0xFF4CAF50) else Color.Red))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if(!staff.isLocked) "Active" else "Offline", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun TopCustomersSection(invoices: List<Invoice>, onNavigate: (AppDestinations) -> Unit, currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Top Customers", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1976D2))
                Text(
                    "View All",
                    color = Color(0xFF1976D2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate(AppDestinations.CUSTOMERS_SUPPLIERS) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val topCustomers = invoices.groupBy { it.customerName }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            if (topCustomers.isEmpty()) {
                Text("No sales found", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                topCustomers.forEach { (name, amount) ->
                    CustomerItem(name.ifEmpty { "Walking Customer" }, String.format(Locale.US, "%,.0f", amount), color = Color(0xFF4CAF50), currencySymbol = currencySymbol)
                }
            }
        }
    }
}

@Composable
fun CustomerItem(name: String, amount: String, color: Color, currencySymbol: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text("Total Purchases: $currencySymbol $amount", fontSize = 9.sp, color = color)
        }
    }
}

@Composable
fun TopProductsSection(invoices: List<Invoice>, onNavigate: (AppDestinations) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Top Selling Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2)
                )
                Text(
                    "View All",
                    color = Color(0xFF1976D2),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate(AppDestinations.PRODUCTS) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            val topProducts = invoices.flatMap { it.items }
                .groupBy { it.productName }
                .mapValues { entry ->
                    val qty = entry.value.sumOf { it.quantity }
                    val total = entry.value.sumOf { it.price * it.quantity }
                    qty to total
                }
                .toList()
                .sortedByDescending { it.second.first }
                .take(5)

            if (topProducts.isEmpty()) {
                Text("No sales found", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                topProducts.forEachIndexed { index, item ->
                    val name = item.first
                    val stats = item.second
                    // এখানে প্রোডাক্টের নাম এবং কোয়ান্টিটি পাঠানো হচ্ছে
                    ProductItemRow(
                        rank = "${index + 1}.",
                        name = name,
                        qty = "Qty: ${stats.first}",
                        trend = "+${String.format(Locale.US, "%,.0f", stats.second)}",
                        color = Color(0xFF4CAF50) // ট্রেন্ডের কালার নীল করা হলো
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemRow(rank: String, name: String, qty: String, trend: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // র‍্যাঙ্ক কালার Black করা হলো
        Text(
            text = rank,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(25.dp),
            color = Color(0xFF4CAF50)
        )
        // প্রোডাক্টের নাম কালার Black করা হলো যাতে দেখা যায়
        Text(
            text = name,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f),
            color = Color(0xFF4CAF50),
            maxLines = 1
        )
        // কোয়ান্টিটি কালার DarkGray করা হলো
        Text(
            text = qty,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // ট্রেন্ড কালার প্যারামিটার থেকে আসবে
        Text(
            text = trend,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true, widthDp = 400)
@Composable
fun ManagementDashboardPreview() {
    val today = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
    val sampleItems = listOf(
        CartItem("P1", "Product A", 10.0, 100.0),
        CartItem("P2", "Product B", 5.0, 200.0)
    )
    val sampleInvoices = listOf(
        Invoice("INV1", today, sampleItems, 1500.0, 0.0, 1500.0, "Cash", "Shop", "Addr", "123", "Tax", "John Doe", "456", userName = "Staff A"),
        Invoice("INV2", today, sampleItems, 2500.0, 0.0, 2500.0, "Card", "Shop", "Addr", "123", "Tax", "Jane Smith", "789", userName = "Staff B")
    )
    val sampleStaff = listOf(
        User(uid = "1", name = "Staff A", role = "STAFF", isLocked = false),
        User(uid = "2", name = "Staff B", role = "STAFF", isLocked = false),
        User(uid = "3", name = "Staff C", role = "STAFF", isLocked = true)
    )
    val sampleExpenses = listOf(
        Expense(1L, today, "Food", 200.0, "Lunch", "Admin")
    )

    SALESPOSSYSTEMTheme(isAdmin = true) {
        ManagementDashboardContent(
            currencySymbol = "₹",
            allInvoices = sampleInvoices,
            expenses = sampleExpenses,
            staffUsers = sampleStaff
        )
    }
}


@Composable
fun MessagesAndMapSection(onNavigate: (AppDestinations) -> Unit) {
    val context = LocalContext.current
    Column {
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp).clickable { 
                Toast.makeText(context, "Opening Messages...", Toast.LENGTH_SHORT).show()
            },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
             Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Messages", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.weight(1f))
                    Text("No new messages", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Open Inbox", color = Color(0xFF1976D2), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Staff Live Locations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4CAF50))
                    Icon(
                        Icons.Default.Fullscreen, 
                        contentDescription = "Expand", 
                        modifier = Modifier.clickable {
                            val gmmIntentUri = "geo:0,0?q=shops".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {}
                        }
                    )
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    val singapore = LatLng(1.35, 103.87)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(singapore, 10f)
                    }
                    val markerState = rememberMarkerState(position = singapore)
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = markerState,
                            title = "Staff A",
                            snippet = "Last seen: 5m ago"
                        )
                    }
                }
            }
        }
    }
}
