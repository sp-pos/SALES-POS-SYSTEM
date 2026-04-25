package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.data.PrintStationEntity
import com.example.salespossystem.ui.theme.*

@Composable
fun PrintStationsScreen(viewModel: SalesViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<PrintStationEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStations = viewModel.printStations.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.type.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        PrintStationsTopBar(
            onAddClick = { showAddDialog = true },
            onDeleteClick = {
                selectedStation?.let {
                    viewModel.deletePrintStation(it)
                    selectedStation = null
                }
            },
            onRefresh = { viewModel.loadDataFromDatabase() }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search printer by name, type or address...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SidebarSelected,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.printStations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PrintStationsEmptyContent(onAddClick = { showAddDialog = true })
                }
            } else if (filteredStations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No printers match your search", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredStations) { station ->
                        PrintStationRow(
                            station = station,
                            isSelected = selectedStation == station,
                            onClick = { selectedStation = if (selectedStation == station) null else station }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPrintStationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, connection, address ->
                viewModel.addPrintStation(
                    PrintStationEntity(
                        name = name,
                        type = type,
                        connectionType = connection,
                        address = address
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PrintStationRow(
    station: PrintStationEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) SidebarSelected.copy(alpha = 0.15f) else CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SidebarSelected) else null,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isSelected) SidebarSelected else Color.DarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (station.type == "Thermal") Icons.Default.Print else Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(station.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (station.isDefault) {
                        Surface(color = AccentGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("DEFAULT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Text("${station.type} | ${station.connectionType}: ${station.address}", color = Color.Gray, fontSize = 12.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Available / Online", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun AddPrintStationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var printerName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Thermal") }
    var connection by remember { mutableStateOf("Bluetooth") }
    var printerAddress by remember { mutableStateOf("") }
    var showDeviceList by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    val networkDevices = remember { mutableStateListOf<NsdServiceInfo>() }
    var isScanningNetwork by remember { mutableStateOf(false) }

    val discoveryListener = remember {
        object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) { isScanningNetwork = true }
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("_ipp") || service.serviceType.contains("_printer") || service.serviceType.contains("_pdl-datastream")) {
                    if (networkDevices.none { it.serviceName == service.serviceName }) {
                        networkDevices.add(service)
                    }
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                networkDevices.removeIf { it.serviceName == service.serviceName }
            }
            override fun onDiscoveryStopped(regType: String) { isScanningNetwork = false }
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) { isScanningNetwork = false }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) { isScanningNetwork = false }
        }
    }

    DisposableEffect(connection) {
        if (connection == "IP") {
            try {
                nsdManager.discoverServices("_ipp._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                nsdManager.discoverServices("_pdl-datastream._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                nsdManager.discoverServices("_printer._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            } catch (e: Exception) { e.printStackTrace() }
        }
        onDispose {
            if (connection == "IP") {
                try { nsdManager.stopServiceDiscovery(discoveryListener) } catch (e: Exception) {}
            }
        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            showDeviceList = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Add Print Station", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = printerName,
                    onValueChange = { printerName = it },
                    label = { Text("Printer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                
                Text("Printer Type", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip("Thermal", type == "Thermal") { type = "Thermal" }
                    TypeChip("A4 / Office", type == "A4") { type = "A4" }
                }

                Text("Connection", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip("Bluetooth", connection == "Bluetooth") { connection = "Bluetooth" }
                    TypeChip("Network/IP", connection =="IP") { connection = "IP" }
                    TypeChip("USB", connection == "USB") { connection = "USB" }
                }

                OutlinedTextField(
                    value = printerAddress,
                    onValueChange = { printerAddress = it },
                    label = { Text(if (connection == "IP") "IP Address (e.g. 192.168.1.100)" else "MAC / Port Address") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (connection == "Bluetooth") {
                            IconButton(onClick = { 
                                val allGranted = permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
                                if (allGranted) showDeviceList = true else launcher.launch(permissions.toTypedArray())
                            }) {
                                Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = "Scan", tint = SidebarSelected)
                            }
                        } else if (connection == "IP") {
                             IconButton(onClick = { showDeviceList = !showDeviceList }) {
                                Icon(Icons.Default.NetworkCheck, contentDescription = "Scan Network", tint = SidebarSelected)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                if (showDeviceList) {
                    if (connection == "Bluetooth") {
                        Text("Nearby / Paired Devices:", color = SidebarSelected, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                        } else true

                        val pairedDevices: List<BluetoothDevice> = if (hasConnectPermission) {
                            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
                        } else emptyList()
                        
                        if (pairedDevices.isEmpty()) {
                            Text("No paired Bluetooth devices found.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                items(pairedDevices) { device ->
                                    val devName = try {
                                        if (hasConnectPermission) device.name ?: "Unknown" else "Unknown"
                                    } catch (e: SecurityException) {
                                        "Unknown"
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                printerName = devName
                                                printerAddress = device.address
                                                showDeviceList = false
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bluetooth, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(devName, color = Color.White, fontSize = 13.sp)
                                    }
                                    HorizontalDivider(color = Color.DarkGray)
                                }
                            }
                        }
                    } else if (connection == "IP") {
                        Text("Network Printers Found:", color = SidebarSelected, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        if (networkDevices.isEmpty()) {
                             Text(if (isScanningNetwork) "Scanning..." else "No network printers found. Make sure the printer is on the same WiFi.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                items(networkDevices) { device ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                printerName = device.serviceName
                                                // Address resolution usually happens during connection, for now we set a placeholder or just name
                                                printerAddress = "Resolving..." 
                                                // In a real app, you'd call nsdManager.resolveService here
                                                nsdManager.resolveService(device, object : NsdManager.ResolveListener {
                                                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                                                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                                        printerAddress = serviceInfo.host.hostAddress ?: ""
                                                    }
                                                })
                                                showDeviceList = false
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Router, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(device.serviceName, color = Color.White, fontSize = 13.sp)
                                    }
                                    HorizontalDivider(color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (printerName.isNotBlank()) onConfirm(printerName, type, connection, printerAddress) },
                colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)
            ) {
                Text("Add Station")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun TypeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (isSelected) SidebarSelected else Color.DarkGray,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun PrintStationsEmptyContent(onAddClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.PrintDisabled,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No print stations configured",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onAddClick) {
            Text(
                text = "Add your first printer",
                color = SidebarSelected,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PrintStationsTopBar(
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HeaderBackground)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProductAction(Icons.Default.Refresh, "Refresh", onClick = onRefresh)
        ProductAction(Icons.Default.Add, "Add station", onClick = onAddClick)
        ProductAction(Icons.Default.Delete, "Delete", onClick = onDeleteClick)
        Spacer(modifier = Modifier.weight(1f))
        ProductAction(Icons.Default.Help, "Help")
    }
}
