package com.example.salespossystem.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "products", primaryKeys = ["barcode", "userName"])
data class ProductEntity(
    val barcode: String,
    val userName: String,
    val code: String,
    val name: String,
    val group: String,
    val cost: String,
    val salePrice: String,
    val active: Boolean,
    val unit: String,
    val imageUrl: String = "",
    val stock: Double = 0.0
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val taxNumber: String,
    val code: String,
    val isSupplier: Boolean = false,
    val userName: String = "Admin"
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val invoiceNumber: String,
    val date: String,
    val subtotal: Double,
    val discount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val customerName: String,
    val customerPhone: String,
    val isPurchase: Boolean = false,
    val customerAddress: String = "",
    val customerTaxNumber: String = "",
    val userName: String = "Admin"
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val price: Double,
    val unit: String = ""
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val accessLevel: String,
    val active: Boolean,
    val uid: String = "",
    val adminId: String = "",
    val idCardNumber: String = "",
    val iqamaId: String = "",
    val passportId: String = "",
    val phone: String = "",
    val address: String = "",
    val posNo: String = "",
    val nationality: String = "",
    val routeArea: String = "",
    val rank: String = "",
    val sectionName: String = "",
    val dateOfBirth: String = "",
    val bloodType: String = "",
    val gender: String = ""
)

@Entity(tableName = "payment_types")
data class PaymentTypeEntity(
    @PrimaryKey val name: String,
    val position: String,
    val enabled: Boolean,
    val quickPayment: Boolean,
    val customerRequired: Boolean,
    val changeAllowed: Boolean,
    val markAsPaid: Boolean,
    val printReceipt: Boolean,
    val shortcutKey: String
)

@Entity(tableName = "tax_rates")
data class TaxRateEntity(
    @PrimaryKey val code: String,
    val name: String,
    val rate: String,
    val enabled: Boolean,
    val fixed: Boolean
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val category: String,
    val amount: Double,
    val description: String,
    val userName: String,
    val paymentMethod: String = "CASH"
)

@Entity(tableName = "print_stations")
data class PrintStationEntity(
    @PrimaryKey val name: String,
    val type: String, // e.g., "Thermal", "A4", "Network"
    val connectionType: String, // e.g., "Bluetooth", "USB", "IP"
    val address: String, // MAC or IP address
    val enabled: Boolean = true,
    val isDefault: Boolean = false
)

@Dao
interface PosDao {
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE barcode = :barcode AND userName = :userName")
    suspend fun deleteProductByBarcode(barcode: String, userName: String)

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoices(): List<InvoiceEntity>

    @Query("SELECT * FROM invoice_items WHERE invoiceNumber = :invoiceNumber")
    suspend fun getItemsForInvoice(invoiceNumber: String): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItemEntity)

    @Query("DELETE FROM invoices WHERE invoiceNumber = :invoiceNumber")
    suspend fun deleteInvoice(invoiceNumber: String)

    @Query("DELETE FROM invoice_items WHERE invoiceNumber = :invoiceNumber")
    suspend fun deleteInvoiceItems(invoiceNumber: String)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM payment_types")
    suspend fun getAllPaymentTypes(): List<PaymentTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentType(paymentType: PaymentTypeEntity)

    @Delete
    suspend fun deletePaymentType(paymentType: PaymentTypeEntity)

    @Query("SELECT * FROM tax_rates")
    suspend fun getAllTaxRates(): List<TaxRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxRate(taxRate: TaxRateEntity)

    @Delete
    suspend fun deleteTaxRate(taxRate: TaxRateEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("SELECT * FROM print_stations")
    suspend fun getAllPrintStations(): List<PrintStationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrintStation(station: PrintStationEntity)

    @Delete
    suspend fun deletePrintStation(station: PrintStationEntity)
}

@Database(entities = [ProductEntity::class, CustomerEntity::class, InvoiceEntity::class, InvoiceItemEntity::class, UserEntity::class, PaymentTypeEntity::class, TaxRateEntity::class, ExpenseEntity::class, PrintStationEntity::class], version = 16)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
