package com.example.salespossystem.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.salespossystem.data.Expense
import com.example.salespossystem.data.Invoice
import java.text.SimpleDateFormat
import java.util.*

object PrintingService {
    fun printInvoice(context: Context, invoice: Invoice, currency: String) {
        val webView = WebView(context)
        
        val taxableAmount = invoice.subtotal - invoice.discount
        val taxAmount = invoice.totalAmount - taxableAmount
        val hasTax = taxAmount > 0.01
        val isReturn = invoice.paymentMethod.contains("RETURN", ignoreCase = true)
        val isDamage = invoice.paymentMethod.equals("DAMAGE", ignoreCase = true)

        val documentTitle = when {
            isDamage -> "DAMAGE INVOICE"
            invoice.isPurchase && isReturn -> "PURCHASE INVOICE RETURN"
            invoice.isPurchase && hasTax -> "PURCHASE TAX INVOICE"
            invoice.isPurchase -> "PURCHASE INVOICE"
            isReturn -> "SALE INVOICE RETURN"
            hasTax -> "TAX INVOICE"
            else -> "RETAIL INVOICE"
        }
        
        val partnerLabel = when {
            isDamage -> "Reason"
            invoice.isPurchase -> "Supplier"
            else -> "Customer"
        }

        val primaryColor = when {
            isDamage -> "#dc3545"
            isReturn -> "#f44336" 
            invoice.isPurchase -> "#2196F3" 
            else -> "#007bff"
        }
        
        val totalItems = invoice.items.sumOf { it.quantity }
        
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
                @page { margin: 5mm; size: auto; }
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 5mm; margin: 0; font-size: 11px; color: #222; line-height: 1.4; }
                .container { max-width: 800px; margin: 0 auto; border: 1px solid #eee; padding: 15px; background: #fff; }
                
                .header { text-align: center; margin-bottom: 20px; border-bottom: 3px solid $primaryColor; padding-bottom: 15px; }
                .header h2 { margin: 0; font-size: 28px; color: $primaryColor; text-transform: uppercase; letter-spacing: 1px; }
                .header p { margin: 3px 0; color: #555; font-size: 13px; }
                .doc-type { font-size: 20px; font-weight: bold; margin-top: 12px; color: #333; letter-spacing: 4px; text-transform: uppercase; }
                
                .info-section { display: flex; justify-content: space-between; margin-bottom: 20px; padding: 12px 10px; background-color: #f9f9f9; border-radius: 5px; }
                .info-box { flex: 1; }
                .info-box p { margin: 4px 0; }
                .info-box strong { color: #333; }
                .bill-to-label { color: $primaryColor; font-weight: bold; font-size: 12px; margin-bottom: 5px; text-decoration: underline; }
                
                .table { width: 100%; border-collapse: collapse; margin-bottom: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.02); }
                .table th { background-color: $primaryColor; color: white; padding: 10px 5px; font-size: 11px; text-transform: uppercase; border: 1px solid $primaryColor; }
                .table td { padding: 10px 5px; border-bottom: 1px solid #eee; font-size: 11px; color: #333; }
                .table tr:nth-child(even) { background-color: #fcfcfc; }
                
                .summary-container { width: 100%; display: flex; justify-content: flex-end; margin-top: 15px; }
                .summary { width: 260px; background: #fefefe; padding: 10px; border: 1px solid #f0f0f0; border-radius: 4px; }
                .summary-row { display: flex; justify-content: space-between; padding: 5px 0; border-bottom: 1px solid #f5f5f5; }
                .summary-row span:first-child { color: #555; font-weight: 600; }
                .total-row { font-size: 18px; font-weight: bold; color: $primaryColor; border-top: 2px solid $primaryColor; margin-top: 8px; padding-top: 12px; border-bottom: none; }
                
                .stamp-container { text-align: center; margin-top: 25px; }
                .stamp { 
                    display: inline-block; 
                    padding: 10px 30px; 
                    border: 4px solid ${if (isDamage || isReturn) "#dc3545" else "#28a745"}; 
                    color: ${if (isDamage || isReturn) "#dc3545" else "#28a745"}; 
                    border-radius: 10px; 
                    font-weight: 900; 
                    font-size: 18px; 
                    text-transform: uppercase; 
                    transform: rotate(-10deg); 
                    opacity: 0.7;
                    box-shadow: 2px 2px 5px rgba(0,0,0,0.1);
                }
                
                .signature-section { display: flex; justify-content: space-between; margin-top: 80px; }
                .signature-box { width: 200px; border-top: 1.5px solid #333; text-align: center; font-size: 11px; padding-top: 8px; color: #333; font-weight: bold; }
                
                .footer { text-align: center; margin-top: 40px; font-size: 10px; color: #666; border-top: 1px solid #eee; padding-top: 20px; }
                .thanks { font-style: italic; font-weight: bold; color: $primaryColor; font-size: 14px; margin-bottom: 8px; }
                .terms { text-align: left; font-size: 10px; color: #777; margin-top: 25px; border: 1px solid #eee; padding: 10px; background: #fafafa; border-radius: 4px; }
                .user-info { font-size: 10px; color: #888; text-align: left; margin-top: 10px; }
            </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>${invoice.companyName}</h2>
                        <p>${invoice.companyAddress}</p>
                        <p><strong>Phone:</strong> ${invoice.companyPhone} &nbsp; | &nbsp; <strong>TRN:</strong> ${invoice.companyTaxNumber}</p>
                        <div class="doc-type">$documentTitle</div>
                    </div>
                    
                    <div class="info-section">
                        <div class="info-box">
                            <div class="bill-to-label">BILL TO:</div>
                            <p style="font-size: 15px; font-weight: bold; color: #000; margin-bottom: 5px;">${invoice.customerName.replace("Supplier: ", "").replace("Internal: ", "")}</p>
                            ${if (invoice.customerPhone.isNotEmpty()) "<p><strong>Phone:</strong> ${invoice.customerPhone}</p>" else ""}
                            ${if (invoice.customerAddress.isNotEmpty()) "<p><strong>Address:</strong> ${invoice.customerAddress}</p>" else ""}
                            ${if (invoice.customerTaxNumber.isNotEmpty()) "<p><strong>TRN:</strong> ${invoice.customerTaxNumber}</p>" else ""}
                        </div>
                        <div class="info-box" style="text-align: right;">
                            <p><strong>Invoice No:</strong> <span style="color: $primaryColor; font-weight: bold;">#${invoice.invoiceNumber}</span></p>
                            <p><strong>Date & Time:</strong> ${invoice.date}</p>
                            <p><strong>Payment Mode:</strong> ${invoice.paymentMethod}</p>
                            <p><strong>Currency:</strong> $currency</p>
                        </div>
                    </div>
                    
                    <table class="table">
                        <thead>
                            <tr>
                                <th width="5%" align="center">SL</th>
                                <th align="left">Product Description</th>
                                <th width="10%" align="center">Unit</th>
                                <th width="10%" align="center">Qty</th>
                                <th width="15%" align="right">Unit Price</th>
                                <th width="15%" align="right">Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${invoice.items.mapIndexed { index, it ->
                                """<tr>
                                    <td align="center">${index + 1}</td>
                                    <td>
                                        <div style="font-weight: bold; font-size: 12px;">${it.productName}</div>
                                        <div style="font-size: 9px; color: #777;">ID: ${it.productId}</div>
                                    </td>
                                    <td align="center">${it.unit}</td>
                                    <td align="center"><strong>${if (it.quantity % 1.0 == 0.0) String.format(Locale.US, "%.0f", it.quantity) else String.format(Locale.US, "%.2f", it.quantity)}</strong></td>
                                    <td align="right">${String.format(Locale.US, "%.2f", it.price)}</td>
                                    <td align="right"><strong>${String.format(Locale.US, "%.2f", it.price * it.quantity)}</strong></td>
                                </tr>"""
                            }.joinToString("")}
                        </tbody>
                    </table>
                    
                    <div class="summary-container">
                        <div class="summary">
                            <div class="summary-row"><span>Total Items</span><span>${if (totalItems % 1.0 == 0.0) String.format(Locale.US, "%.0f", totalItems) else String.format(Locale.US, "%.2f", totalItems)}</span></div>
                            <div class="summary-row"><span>Subtotal (Excl. Tax)</span><span>${String.format(Locale.US, "%.2f", invoice.subtotal)}</span></div>
                            ${if (invoice.discount > 0) """<div class="summary-row" style="color: #d32f2f;"><span>Discount (-)</span><span>${String.format(Locale.US, "%.2f", invoice.discount)}</span></div>""" else ""}
                            ${if (hasTax) """<div class="summary-row"><span>Vat / Tax (+)</span><span>${String.format(Locale.US, "%.2f", taxAmount)}</span></div>""" else ""}
                            <div class="summary-row total-row"><span>GRAND TOTAL</span><span>${String.format(Locale.US, "%.2f", invoice.totalAmount)} $currency</span></div>
                        </div>
                    </div>
                    
                    <div class="stamp-container">
                        <div class="stamp">${if (isDamage) "RECORDED" else invoice.paymentMethod + " PAID"}</div>
                    </div>
                    
                    <div class="terms">
                        <strong>Terms & Conditions:</strong><br>
                        1. Please keep this invoice for any future reference or warranty claims.<br>
                        2. Goods once sold are only returnable under company policy within 7 days.<br>
                        3. Any discrepancy must be reported within 24 hours of purchase.<br>
                        4. This is a computer-generated invoice and does not require a physical seal.
                    </div>
                    
                    <div class="signature-section">
                        <div class="signature-box">Customer's Signature</div>
                        <div class="signature-box">Authorized Signature</div>
                    </div>
                    
                    <div class="user-info">
                        Served by: ${invoice.userName}
                    </div>
                    
                    <div class="footer">
                        <p class="thanks">Thank you for your business!</p>
                        <p>This is a computer generated invoice.</p>
                        <p>Software Powered by SALES POS SYSTEM</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Invoice_${invoice.invoiceNumber}")
                printManager.print("Invoice_${invoice.invoiceNumber}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    fun printExpense(context: Context, expense: Expense, companyName: String, companyAddress: String, companyPhone: String, companyTax: String, currency: String = "BDT") {
        val webView = WebView(context)
        val primaryColor = "#673AB7"
        
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
                @page { margin: 10mm; size: auto; }
                body { font-family: 'Segoe UI', sans-serif; padding: 10mm; font-size: 12px; color: #333; line-height: 1.6; }
                .container { max-width: 700px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; }
                .header { text-align: center; border-bottom: 2px solid $primaryColor; padding-bottom: 15px; margin-bottom: 20px; }
                .header h2 { margin: 0; color: $primaryColor; font-size: 24px; }
                .title { font-size: 18px; font-weight: bold; text-align: center; margin: 20px 0; text-decoration: underline; }
                .details { margin-bottom: 30px; }
                .details p { margin: 8px 0; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; }
                .details strong { color: #555; }
                .amount-box { text-align: center; margin: 30px 0; padding: 15px; border: 2px dashed $primaryColor; background: #f9f9f9; }
                .amount { font-size: 24px; font-weight: bold; color: $primaryColor; }
                .signature-section { display: flex; justify-content: space-between; margin-top: 60px; }
                .sig { width: 180px; border-top: 1px solid #333; text-align: center; padding-top: 5px; }
                .footer { text-align: center; margin-top: 40px; color: #999; font-size: 10px; }
            </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>$companyName</h2>
                        <p>$companyAddress</p>
                        <p>Phone: $companyPhone | TRN: $companyTax</p>
                    </div>
                    <div class="title">EXPENSE VOUCHER</div>
                    <div class="details">
                        <p><strong>Voucher ID:</strong> <span>#EXP-${expense.id}</span></p>
                        <p><strong>Date:</strong> <span>${expense.date}</span></p>
                        <p><strong>Category:</strong> <span>${expense.category}</span></p>
                        <p><strong>Description:</strong> <span>${expense.description.ifEmpty { "N/A" }}</span></p>
                        <p><strong>Spent By:</strong> <span>${expense.userName}</span></p>
                    </div>
                    <div class="amount-box">
                        <p>TOTAL AMOUNT</p>
                        <div class="amount">${String.format(Locale.US, "%.2f", expense.amount)} $currency</div>
                    </div>
                    <div class="signature-section">
                        <div class="sig">Receiver's Signature</div>
                        <div class="sig">Authorized By</div>
                    </div>
                    <div class="footer">
                        <p>Generated on: ${SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())}</p>
                        <p>Software Powered by SALES POS SYSTEM</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Expense_Voucher_${expense.id}")
                printManager.print("Expense_Voucher_${expense.id}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    fun printReport(
        context: Context,
        reportName: String,
        companyName: String,
        companyAddress: String,
        companyPhone: String,
        companyTax: String,
        period: String,
        headers: List<String>,
        rows: List<List<String>>,
        totalAmount: Double? = null,
        currency: String
    ) {
        val webView = WebView(context)
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
                @page { margin: 10mm; size: A4; }
                body { font-family: 'Segoe UI', sans-serif; font-size: 11px; color: #333; line-height: 1.4; }
                .header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }
                .header h2 { margin: 0; font-size: 18px; text-transform: uppercase; }
                .report-title { font-size: 14px; font-weight: bold; margin: 10px 0; text-decoration: underline; }
                .table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                .table th { background-color: #f2f2f2; border: 1px solid #ddd; padding: 8px; text-align: left; font-weight: bold; }
                .table td { border: 1px solid #ddd; padding: 6px 8px; }
                .total-row { font-weight: bold; background-color: #f9f9f9; }
                .footer { text-align: center; margin-top: 30px; font-size: 9px; color: #999; }
            </style>
            </head>
            <body>
                <div class="header">
                    <h2>${companyName.ifEmpty { "SALES POS SYSTEM" }}</h2>
                    <p>$companyAddress</p>
                    <p>Phone: $companyPhone | TRN: $companyTax</p>
                    <div class="report-title">REPORT: ${reportName.uppercase()}</div>
                    <p>Period: $period</p>
                </div>
                <table class="table">
                    <thead>
                        <tr>
                            ${headers.joinToString("") { "<th>$it</th>" }}
                        </tr>
                    </thead>
                    <tbody>
                        ${rows.joinToString("") { row ->
                            "<tr>" + row.joinToString("") { "<td>$it</td>" } + "</tr>"
                        }}
                        ${if (totalAmount != null) """
                            <tr class="total-row">
                                <td colspan="${headers.size - 1}" style="text-align: right;">GRAND TOTAL:</td>
                                <td>${String.format(Locale.US, "%.2f", totalAmount)} $currency</td>
                            </tr>
                        """ else ""}
                    </tbody>
                </table>
                <div class="footer">
                    <p>Generated on: ${SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())}</p>
                    <p>Software Powered by SALES POS SYSTEM</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Report_$reportName")
                printManager.print("Report_$reportName", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    fun printStaffList(
        context: Context,
        staffList: List<com.example.salespossystem.data.User>,
        companyName: String = "SALES POS SYSTEM",
        companyAddress: String = "",
        companyPhone: String = "",
        companyTaxNumber: String = "",
        currency: String = "SAR"
    ) {
        val headers = listOf("Name", "Email", "Phone", "POS No", "ID Card", "Nationality")
        val rows = staffList.map { staff ->
            listOf(
                staff.name,
                staff.email,
                staff.phoneNumber,
                staff.posNo,
                staff.idCardNumber,
                staff.nationality
            )
        }
        
        printReport(
            context = context,
            reportName = "Staff List",
            companyName = companyName,
            companyAddress = companyAddress,
            companyPhone = companyPhone,
            companyTax = companyTaxNumber,
            period = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
            headers = headers,
            rows = rows,
            currency = currency
        )
    }

    fun printStaffProfile(
        context: Context,
        staff: com.example.salespossystem.data.User,
        companyName: String = "SALES POS SYSTEM",
        companyAddress: String = "",
        companyPhone: String = "",
        companyTaxNumber: String = ""
    ) {
        val webView = WebView(context)
        val primaryColor = "#2196F3"
        
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
                @page { margin: 10mm; size: A4; }
                body { font-family: 'Segoe UI', sans-serif; padding: 10mm; color: #333; line-height: 1.6; background: #fff; }
                .container { max-width: 800px; margin: 0 auto; border: 2px solid #eee; padding: 30px; border-radius: 10px; position: relative; }
                .header { text-align: center; border-bottom: 3px solid $primaryColor; padding-bottom: 20px; margin-bottom: 30px; }
                .header h1 { margin: 0; color: $primaryColor; font-size: 28px; text-transform: uppercase; }
                
                .profile-header { display: flex; align-items: center; margin-bottom: 30px; background: #f8f9fa; padding: 20px; border-radius: 8px; }
                .profile-img { width: 100px; height: 100px; background: #ddd; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 40px; color: #666; margin-right: 25px; border: 3px solid $primaryColor; }
                .profile-title h2 { margin: 0; font-size: 24px; color: #222; }
                .profile-title p { margin: 5px 0 0; color: $primaryColor; font-weight: bold; font-size: 16px; text-transform: uppercase; }
                
                .section-title { font-size: 18px; font-weight: bold; color: $primaryColor; border-left: 5px solid $primaryColor; padding-left: 10px; margin: 25px 0 15px; background: #e3f2fd; padding-top: 5px; padding-bottom: 5px; }
                
                .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
                .info-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
                .info-item label { display: block; font-size: 11px; color: #777; font-weight: bold; text-transform: uppercase; }
                .info-item span { font-size: 14px; color: #333; font-weight: 500; }
                
                .footer { text-align: center; margin-top: 50px; font-size: 10px; color: #999; border-top: 1px solid #eee; padding-top: 20px; }
                .signature-box { margin-top: 60px; display: flex; justify-content: space-between; }
                .sig { width: 200px; border-top: 1.5px solid #333; text-align: center; padding-top: 8px; font-weight: bold; }
                
                .watermark { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); font-size: 80px; color: rgba(0,0,0,0.03); font-weight: bold; pointer-events: none; white-space: nowrap; }
            </style>
            </head>
            <body>
                <div class="container">
                    <div class="watermark">STAFF PROFILE</div>
                    <div class="header">
                        <h1>$companyName</h1>
                        <p style="margin: 2px 0; font-size: 14px; color: #555;">$companyAddress</p>
                        <p style="margin: 2px 0; font-size: 12px; color: #666;">Phone: $companyPhone ${if (companyTaxNumber.isNotEmpty()) "| TRN: $companyTaxNumber" else ""}</p>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 10px 0;">
                        <p style="font-weight: bold; margin-top: 5px;">EMPLOYEE INFORMATION CARD</p>
                    </div>
                    
                    <div class="profile-header">
                        <div class="profile-img">👤</div>
                        <div class="profile-title">
                            <h2>${staff.name}</h2>
                            <p>${if (staff.role == "ADMIN") "Administrator" else "Staff Member"}</p>
                        </div>
                    </div>
                    
                    <div class="section-title">Personal Identification</div>
                    <div class="grid">
                        <div class="info-item"><label>Full Name</label><span>${staff.name}</span></div>
                        <div class="info-item"><label>Gender</label><span>${staff.gender.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Date of Birth</label><span>${staff.dateOfBirth.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Blood Group</label><span>${staff.bloodType.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Nationality</label><span>${staff.nationality.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Email Address</label><span>${staff.email}</span></div>
                        <div class="info-item"><label>ID Card Number</label><span>${staff.idCardNumber.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Iqama ID</label><span>${staff.iqamaId.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Passport ID</label><span>${staff.passportId.ifEmpty { "N/A" }}</span></div>
                    </div>
                    
                    <div class="section-title">Employment Details</div>
                    <div class="grid">
                        <div class="info-item"><label>POS Number</label><span>${staff.posNo.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Designation / Rank</label><span>${staff.rank.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Section / Dept</label><span>${staff.sectionName.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Route Area</label><span>${staff.routeArea.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>User UID</label><span>${staff.uid}</span></div>
                        <div class="info-item"><label>Account Status</label><span>${if (staff.isLocked) "LOCKED" else "ACTIVE"}</span></div>
                    </div>
                    
                    <div class="section-title">Contact Information</div>
                    <div class="grid">
                        <div class="info-item"><label>Phone Number</label><span>${staff.phoneNumber.ifEmpty { "N/A" }}</span></div>
                        <div class="info-item"><label>Work Address</label><span>${staff.address.ifEmpty { "N/A" }}</span></div>
                    </div>
                    
                    <div class="signature-box">
                        <div class="sig">Employee Signature</div>
                        <div class="sig">Authorized Authority</div>
                    </div>
                    
                    <div class="footer">
                        <p>Printed on: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}</p>
                        <p>This is a formal document generated by SALES POS SYSTEM</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Profile_${staff.name.replace(" ", "_")}")
                printManager.print("Profile_${staff.name}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}
