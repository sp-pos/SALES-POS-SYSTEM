package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.AppDestinations
import com.example.salespossystem.util.PrintingService
import com.example.salespossystem.data.CartItem
import com.example.salespossystem.data.Customer
import com.example.salespossystem.data.ProductItem
import java.util.Locale

val DarkBackground = Color(0xFF1E1E1E)
val SidebarBackground = Color(0xFF2D2D2D)
val HeaderBackground = Color(0xFF333333)
val BorderColor = Color(0xFF444444)
val AccentGreen = Color(0xFF4CAF50)
val AccentRed = Color(0xFFE53935)
val AccentBlue = Color(0xFF2196F3)

@Composable
fun SaleScreen(viewModel: SalesViewModel = viewModel(), onNavigate: (AppDestinations) -> Unit = {}) {
    var showProductSearch by remember { mutableStateOf(false) }
    var showCustomerSelect by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var barcodeInput by remember { mutableStateOf("") }
    var isReturnMode by remember { mutableStateOf(false) }
    
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedItemForQty by remember { mutableStateOf<CartItem?>(null) }

    val context = LocalContext.current
    val invoice = viewModel.lastInvoice

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraScanner = true
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        SaleTopBar(
            isReturnMode = isReturnMode,
            onNavigateBack = { onNavigate(AppDestinations.HOME) },
            onToggleReturnMode = { isReturnMode = !isReturnMode },
            barcodeValue = barcodeInput,
            onBarcodeChange = { barcodeInput = it },
            onAddClick = { showProductSearch = true },
            onSearchClick = { showProductSearch = true },
            onScanClick = {
                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    showCameraScanner = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onBarcodeSubmit = {
                val foundProduct = viewModel.products.find { it.barcode == barcodeInput }
                if (foundProduct != null) {
                    viewModel.addToCart(foundProduct)
                    barcodeInput = ""
                }
            }
        )
        Box(modifier = Modifier.weight(1f)) {
            OrderTable(viewModel, onQtyClick = {
                selectedItemForQty = it
                showQuantityDialog = true
            })
        }
        OrderSummary(viewModel, isReturnMode)
        SaleBottomActions(
            viewModel = viewModel,
            isReturnMode = isReturnMode,
            onSearchClick = { showProductSearch = true },
            onCustClick = { showCustomerSelect = true },
            onDiscClick = { showDiscountDialog = true }
        )

        if (showProductSearch) {
            ProductSearchDialog(
                products = viewModel.products,
                onDismiss = { showProductSearch = false },
                onProductSelect = { 
                    viewModel.addToCart(it)
                    showProductSearch = false
                }
            )
        }
        
        if (showCustomerSelect) {
            CustomerSelectDialog(
                customers = viewModel.customers,
                onDismiss = { showCustomerSelect = false },
                onCustomerSelect = {
                    viewModel.selectedCustomer = it
                    showCustomerSelect = false
                }
            )
        }
        
        if (showDiscountDialog) {
            DiscountDialog(
                currentDiscount = viewModel.discountAmount,
                onDismiss = { showDiscountDialog = false },
                onConfirm = {
                    viewModel.discountAmount = it
                    showDiscountDialog = false
                }
            )
        }

        if (showQuantityDialog && selectedItemForQty != null) {
            QuantityEditDialog(
                item = selectedItemForQty!!,
                onDismiss = { showQuantityDialog = false },
                onConfirm = { newQty ->
                    viewModel.updateCartItemQuantity(selectedItemForQty!!.productId, newQty)
                    showQuantityDialog = false
                }
            )
        }

        if (showCameraScanner) {
            BarcodeScannerDialog(
                viewModel = viewModel,
                onDismiss = { showCameraScanner = false },
                onReviewOrder = { showCameraScanner = false }
            )
        }

        if (invoice != null) {
            InvoiceDialog(
                invoice = invoice,
                currencySymbol = viewModel.currencySymbol,
                onDismiss = { viewModel.clearInvoice() },
                onPrint = { 
                    PrintingService.printInvoice(context, invoice, viewModel.currencySymbol)
                    viewModel.clearInvoice()
                }
            )
        }
    }
}

@Composable
fun ProductSearchDialog(
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onProductSelect: (ProductItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBackground,
        title = { Text("Search Products", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = AccentGreen,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredProducts) { product ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onProductSelect(product)
                                }
                                .padding(12.dp)
                        ) {
                            Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Price: ${product.salePrice}", fontSize = 13.sp, color = Color.Gray)
                        }
                        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text("Close", color = AccentBlue) 
            }
        }
    )
}

@Composable
fun QuantityEditDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var qtyText by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBackground,
        title = { Text("Edit Quantity", color = Color.White) },
        text = {
            Column {
                Text(item.productName, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toDoubleOrNull() ?: item.quantity
                    onConfirm(qty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = Color.Gray) 
            }
        }
    )
}

@Composable
fun DiscountDialog(
    currentDiscount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var discountText by remember { mutableStateOf(currentDiscount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBackground,
        title = { Text("Apply Discount", color = Color.White) },
        text = {
            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("Discount Amount", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = Color.Gray
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = discountText.toDoubleOrNull() ?: 0.0
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = Color.Gray) 
            }
        }
    )
}

@Composable
fun SaleTopBar(
    isReturnMode: Boolean,
    onNavigateBack: () -> Unit,
    onToggleReturnMode: () -> Unit,
    barcodeValue: String,
    onBarcodeChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onScanClick: () -> Unit = {},
    onBarcodeSubmit: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(HeaderBackground)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = if (isReturnMode) "Sale Return" else "Sale", 
            color = Color.White, 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(HeaderBackground)
            ) {
                DropdownMenuItem(
                    text = { Text(if (isReturnMode) "Regular Sale" else "Sale Return", color = Color.White) },
                    onClick = {
                        onToggleReturnMode()
                        showMenu = false
                    }
                )
            }
        }
        
        Row(
            modifier = Modifier
                .height(40.dp)
                .background(Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search, 
                contentDescription = null, 
                tint = Color.Gray, 
                modifier = Modifier.size(20.dp)
            )
            
            BasicTextField(
                value = barcodeValue,
                onValueChange = onBarcodeChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onBarcodeSubmit()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (barcodeValue.isEmpty()) {
                        Text("Barcode...", color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = onScanClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode", tint = AccentGreen)
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Open product list", tint = AccentGreen)
            }
        }

        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.FindInPage, contentDescription = "Open product list", tint = Color.White)
        }
    }
}

@Composable
fun OrderTable(viewModel: SalesViewModel, onQtyClick: (CartItem) -> Unit) {
    val cartItems = viewModel.cartItems
    val listState = rememberLazyListState()

    LaunchedEffect(cartItems.size) {
        if (cartItems.isNotEmpty()) {
            listState.animateScrollToItem(cartItems.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackground)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Product", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Qty", Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 12.sp)
            Text("Price", Modifier.width(60.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp)
            Text("Total", Modifier.width(60.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp)
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Scan or search products", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(state = listState) {
                items(cartItems) { item ->
                    CartItemRow(item, viewModel, onQtyClick)
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, viewModel: SalesViewModel, onQtyClick: (CartItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.productName, Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
        
        Row(
            modifier = Modifier.width(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.decrementCartItem(item.productId) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.RemoveCircle, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            Surface(
                onClick = { onQtyClick(item) },
                color = Color(0xFF3D3D3D),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Text(
                    text = item.quantity.toString(), 
                    color = Color.White, 
                    textAlign = TextAlign.Center, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            IconButton(onClick = { viewModel.incrementCartItem(item.productId) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AddCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
            }
        }

        Text(String.format(Locale.US, "%.2f", item.price), Modifier.width(60.dp), color = Color.White, textAlign = TextAlign.End, fontSize = 14.sp)
        Text(String.format(Locale.US, "%.2f", item.price * item.quantity), Modifier.width(60.dp), color = Color.White, textAlign = TextAlign.End, fontSize = 14.sp)
    }
}


@Composable
fun OrderSummary(viewModel: SalesViewModel, isReturnMode: Boolean) {
    val subtotal = viewModel.getSubtotal()
    val discount = viewModel.discountAmount
    val taxAmount = viewModel.getTaxAmount()
    val total = viewModel.getTotalPrice()
    val activeTax = viewModel.getActiveTaxRateItem()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SidebarBackground)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", color = Color.LightGray, fontSize = 14.sp)
            Text(String.format(Locale.US, "%.2f", subtotal), color = Color.LightGray, fontSize = 14.sp)
        }
        if (discount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Discount", color = AccentRed, fontSize = 14.sp)
                Text("-${String.format(Locale.US, "%.2f", discount)}", color = AccentRed, fontSize = 14.sp)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(activeTax?.name ?: "VAT (0%)", color = Color.LightGray, fontSize = 14.sp)
            Text(String.format(Locale.US, "%.2f", taxAmount), color = Color.LightGray, fontSize = 14.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (isReturnMode) "Return Total" else "Total", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            val currencySymbol = viewModel.currencySymbol
            Text("${currencySymbol} ${String.format(Locale.US, "%.2f", total)}", color = if (isReturnMode) AccentRed else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        viewModel.selectedCustomer?.let { customer ->
            Text(
                text = "Customer: ${customer.name}",
                color = AccentBlue,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SaleBottomActions(
    viewModel: SalesViewModel,
    isReturnMode: Boolean,
    onSearchClick: () -> Unit,
    onCustClick: () -> Unit,
    onDiscClick: () -> Unit
) {
    val paymentMethod = if (isReturnMode) "CASH RETURN" else viewModel.selectedPaymentMethod
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SidebarBackground)
            .padding(4.dp)
    ) {
        if (!isReturnMode) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PaymentButton(
                    label = "CASH", 
                    onClick = { viewModel.selectedPaymentMethod = "CASH" }, 
                    modifier = Modifier.weight(1f), 
                    accentColor = if (viewModel.selectedPaymentMethod == "CASH") AccentGreen else null
                )
                PaymentButton(
                    label = "CARD", 
                    onClick = { viewModel.selectedPaymentMethod = "CARD" }, 
                    modifier = Modifier.weight(1f),
                    accentColor = if (viewModel.selectedPaymentMethod == "CARD") AccentGreen else null
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(2.dp),
            color = if (isReturnMode) AccentRed else AccentGreen,
            shape = RoundedCornerShape(4.dp),
            onClick = { viewModel.processPayment(paymentMethod) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(if (isReturnMode) "PROCESS RETURN" else "PAYMENT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SidebarActionSmall(Icons.Default.Search, "Search", Modifier.weight(1f), onClick = onSearchClick)
            SidebarActionSmall(Icons.Default.Percent, "Disc.", Modifier.weight(1f), onClick = onDiscClick)
            SidebarActionSmall(Icons.Default.Person, "Cust.", Modifier.weight(1f), onClick = onCustClick)
            SidebarActionSmall(Icons.Default.Delete, "Void", Modifier.weight(1f), bgColor = AccentRed, onClick = { viewModel.clearCart() })
        }
    }
}

@Composable
fun CustomerSelectDialog(customers: List<Customer>, onDismiss: () -> Unit, onCustomerSelect: (Customer) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBackground,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) } },
        title = { Text("Select Customer", color = Color.White) },
        text = {
            LazyColumn {
                items(customers) { customer ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { onCustomerSelect(customer) },
                        color = Color.Transparent
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(customer.name, color = Color.White, fontWeight = FontWeight.Bold)
                            if (customer.phone.isNotEmpty()) {
                                Text(customer.phone, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    )
}
