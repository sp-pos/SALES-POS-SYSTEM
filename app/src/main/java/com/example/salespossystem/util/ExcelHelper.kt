package com.example.salespossystem.util

import android.content.Context
import android.net.Uri
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.data.Invoice
import com.example.salespossystem.data.StockMovementEntry
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

object ExcelHelper {

    fun exportProductsToExcel(
        context: Context, 
        products: List<ProductItem>, 
        stockMap: Map<String, Double>,
        outputStream: OutputStream
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Products")
            
            // Create Header
            val headerRow = sheet.createRow(0)
            val headers = listOf("Barcode", "Name", "Price", "Balance")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            // Fill Data
            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.barcode)
                row.createCell(1).setCellValue(product.name)
                row.createCell(2).setCellValue(product.salePrice)
                row.createCell(3).setCellValue(stockMap[product.barcode] ?: 0.0)
            }
            
            workbook.write(outputStream)
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportInvoicesToExcel(
        invoices: List<Invoice>,
        outputStream: OutputStream
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Report")
            
            val headerRow = sheet.createRow(0)
            val headers = listOf("Date", "Invoice No", "Customer/Supplier", "Items", "Subtotal", "Discount", "Total", "Payment")
            headers.forEachIndexed { index, h -> headerRow.createCell(index).setCellValue(h) }
            
            invoices.forEachIndexed { index, inv ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(inv.date)
                row.createCell(1).setCellValue(inv.invoiceNumber)
                row.createCell(2).setCellValue(inv.customerName)
                row.createCell(3).setCellValue(inv.items.joinToString { "${it.productName}(${it.quantity})" })
                row.createCell(4).setCellValue(inv.subtotal)
                row.createCell(5).setCellValue(inv.discount)
                row.createCell(6).setCellValue(inv.totalAmount)
                row.createCell(7).setCellValue(inv.paymentMethod)
            }
            
            workbook.write(outputStream)
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportStockMovementToExcel(
        movements: List<StockMovementEntry>,
        outputStream: OutputStream
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Stock Movement")
            
            val headerRow = sheet.createRow(0)
            val headers = listOf("Date", "Product", "Reference", "Type", "Quantity Change")
            headers.forEachIndexed { index, h -> headerRow.createCell(index).setCellValue(h) }
            
            movements.forEachIndexed { index, sm ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(sm.date)
                row.createCell(1).setCellValue(sm.productName)
                row.createCell(2).setCellValue(sm.reference)
                row.createCell(3).setCellValue(sm.type)
                row.createCell(4).setCellValue(sm.change)
            }
            
            workbook.write(outputStream)
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importProductsFromExcel(context: Context, uri: Uri): List<Pair<ProductItem, Double>> {
        val productList = mutableListOf<Pair<ProductItem, Double>>()
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                val workbook = WorkbookFactory.create(it)
                val sheet = workbook.getSheetAt(0)
                
                // Skip Header
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    
                    val barcode = row.getCell(0)?.toString()?.trim() ?: ""
                    val name = row.getCell(1)?.toString()?.trim() ?: ""
                    val price = row.getCell(2)?.toString()?.trim() ?: "0.0"
                    val balanceStr = row.getCell(3)?.toString()?.trim() ?: "0.0"
                    
                    val balance = balanceStr.toDoubleOrNull() ?: 0.0
                    
                    if (barcode.isNotEmpty() && name.isNotEmpty()) {
                        val product = ProductItem(
                            code = barcode, // Default code as barcode
                            name = name,
                            group = "Default", // Default group
                            barcode = barcode,
                            cost = "0.0", // Default cost
                            salePrice = price,
                            active = true,
                            unit = "Pcs" // Default unit
                        )
                        productList.add(Pair(product, balance))
                    }
                }
                workbook.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return productList
    }
}
