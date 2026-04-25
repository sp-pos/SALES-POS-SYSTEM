package com.example.salespossystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.data.Invoice

@Composable
fun InvoiceDialog(invoice: Invoice, currencySymbol: String, onDismiss: () -> Unit, onPrint: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { Text("Invoice Processed", color = Color.White) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Invoice Number: ${invoice.invoiceNumber}", color = Color.LightGray)
                Text("Total Amount: $currencySymbol ${invoice.totalAmount}", color = Color.White, fontSize = 20.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onPrint,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Print Receipt")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = Color(0xFF4CAF50))
            }
        }
    )
}
