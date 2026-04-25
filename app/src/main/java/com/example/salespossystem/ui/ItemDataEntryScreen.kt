package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme
import com.example.salespossystem.ui.theme.SidebarSelected
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ItemDataEntryScreen(viewModel: SalesViewModel = viewModel(), onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var imageUrl by remember { mutableStateOf("") }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUrl = it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Item Data Entry",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Image Selection Section
        Text("Product Image", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2D2D2D))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                .clickable { photoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Product Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, "Add Photo", tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Text("Enter image URL below", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        DataEntryField("Image URL (Direct link)", imageUrl) { imageUrl = it }

        DataEntryField("Product Name", name) { name = it }
        DataEntryField("Group / Category", group) { group = it }
        DataEntryField("Barcode", barcode) { barcode = it }
        DataEntryField("Cost Price", costPrice) { costPrice = it }
        DataEntryField("Sale Price", salePrice) { salePrice = it }
        DataEntryField("Unit (e.g., pcs, kg)", unit) { unit = it }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && barcode.isNotEmpty()) {
                    val sdf = SimpleDateFormat("dd-MMM-yy HH:mm:ss", Locale.US)
                    val now = sdf.format(Date())
                    val newProduct = ProductItem(
                        code = "",
                        name = name,
                        group = group.ifEmpty { "General" },
                        barcode = barcode,
                        cost = costPrice.ifEmpty { "0.00" },
                        salePrice = salePrice.ifEmpty { "0.00" },
                        active = true,
                        unit = unit,
                        created = now,
                        updated = now,
                        imageUrl = imageUrl
                    )
                    viewModel.addProduct(newProduct, 0.0)
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DataEntryField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SidebarSelected,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = SidebarSelected
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun ItemDataEntryScreenPreview() {
    SALESPOSSYSTEMTheme {
        ItemDataEntryScreen(onBack = {})
    }
}
