package com.example.data

import androidx.room.*
import com.example.models.Product
import com.example.models.Expense
import com.example.models.AppSetting
import com.example.models.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): AppSetting?

    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<AppSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY totalBill DESC, id ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int

    @Query("UPDATE customers SET totalBill = 0.0, billItemsJson = '{}', expensesJson = '[]'")
    suspend fun resetAllCustomerDues()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers")
    suspend fun clearAllCustomers()
}

@Dao
interface BillRecordDao {
    @Query("SELECT * FROM bill_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<com.example.models.BillRecord>>

    @Query("SELECT * FROM bill_records WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getRecordsForCustomer(customerId: Long): Flow<List<com.example.models.BillRecord>>

    @Query("SELECT * FROM bill_records WHERE monthKey = :monthKey ORDER BY timestamp DESC")
    fun getRecordsForMonth(monthKey: String): Flow<List<com.example.models.BillRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: com.example.models.BillRecord): Long

    @Update
    suspend fun updateRecord(record: com.example.models.BillRecord)

    @Delete
    suspend fun deleteRecord(record: com.example.models.BillRecord)

    @Query("DELETE FROM bill_records")
    suspend fun clearAllRecords()
}

