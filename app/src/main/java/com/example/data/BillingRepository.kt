package com.example.data

import com.example.models.Product
import com.example.models.Expense
import com.example.models.AppSetting
import com.example.models.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BillingRepository(
    private val productDao: ProductDao,
    private val expenseDao: ExpenseDao,
    private val appSettingDao: AppSettingDao,
    private val customerDao: CustomerDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    suspend fun seedDefaultsIfEmpty() {
        seedProducts()
        seedCustomers()
    }

    private suspend fun seedProducts() {
        val current = productDao.getAllProducts().first()
        if (current.isEmpty()) {
            val defaults = getDefaultBengaliProducts()
            productDao.insertProducts(defaults)
        } else {
            // Auto-migrate and fix product names as requested by user
            val renameMap = mapOf(
                "ক্যারম বিল" to "ক্যারাম",
                "Carrom Bill" to "ক্যারাম",
                "লাকি স্ট্রাইক সুইচ" to "লাকি স্ট্রাইক",
                "Lucky Strike Switch" to "লাকি স্ট্রাইক",
                "Benson" to "বেনসন",
                "Camel" to "ক্যামেল",
                "Royal" to "রয়েল",
                "Derby" to "ডার্বি",
                "Lal Tea" to "লাল চা",
                "Black Coffee" to "ব্ল্যাক কফি",
                "Milk Coffee" to "দুধ কফি"
            )
            current.forEach { prod ->
                val newName = renameMap[prod.name]
                if (newName != null) {
                    productDao.updateProduct(prod.copy(name = newName))
                }
            }

            // Ensure requested products 'বিস্কুট', 'চানাচুর', 'কোল্ড ড্রিংকস' are present
            val existingNames = current.map { it.name }.toSet()
            val extraProducts = listOf(
                Product(name = "বিস্কুট", price = 10.0, iconType = "FOOD", isDefault = true),
                Product(name = "চানাচুর", price = 10.0, iconType = "FOOD", isDefault = true),
                Product(name = "কোল্ড ড্রিংকস", price = 30.0, iconType = "FOOD", isDefault = true)
            )
            extraProducts.forEach { extra ->
                if (!existingNames.contains(extra.name)) {
                    productDao.insertProduct(extra)
                }
            }
        }
    }

    private suspend fun seedCustomers() {
        val count = customerDao.getCustomerCount()
        if (count == 0) {
            val defaultNames = listOf(
                "শান", "ইমন", "বাপ্পি", "মুকুট",
                "হালিম", "গরিব", "সাজ্জাদ", "পলাশ",
                "আঙ্গুর", "ভুবন", "সুপ্ত সোহেল", "জনি খালু",
                "জনি", "তন্ময়", "মানিক", "জিতু"
            )
            val customers = defaultNames.map { name ->
                Customer(name = name)
            }
            customerDao.insertCustomers(customers)
        }
    }

    private fun getDefaultBengaliProducts(): List<Product> = listOf(
        Product(name = "ক্যারাম", price = 20.0, iconType = "GAME", isDefault = true),
        Product(name = "বেনসন", price = 20.0, iconType = "SMOKE", isDefault = true),
        Product(name = "লাকি স্ট্রাইক", price = 12.0, iconType = "SMOKE", isDefault = true),
        Product(name = "ক্যামেল", price = 10.0, iconType = "SMOKE", isDefault = true),
        Product(name = "রয়েল", price = 8.0, iconType = "SMOKE", isDefault = true),
        Product(name = "ডার্বি", price = 8.0, iconType = "SMOKE", isDefault = true),
        Product(name = "লাল চা", price = 5.0, iconType = "TEA", isDefault = true),
        Product(name = "ব্ল্যাক কফি", price = 10.0, iconType = "COFFEE", isDefault = true),
        Product(name = "দুধ কফি", price = 20.0, iconType = "COFFEE", isDefault = true),
        Product(name = "বিস্কুট", price = 10.0, iconType = "FOOD", isDefault = true),
        Product(name = "চানাচুর", price = 10.0, iconType = "FOOD", isDefault = true),
        Product(name = "কোল্ড ড্রিংকস", price = 30.0, iconType = "FOOD", isDefault = true)
    )

    // Product methods
    suspend fun addProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Expense methods
    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun clearAllExpenses() = expenseDao.clearAllExpenses()

    // Customer methods
    suspend fun addCustomer(name: String): Long {
        return customerDao.insertCustomer(Customer(name = name.trim()))
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)
    }

    suspend fun updateCustomerBill(
        customerId: Long,
        billItemsJson: String,
        expensesJson: String,
        totalBill: Double
    ) {
        val existing = customerDao.getCustomerById(customerId)
        if (existing != null) {
            customerDao.updateCustomer(
                existing.copy(
                    billItemsJson = billItemsJson,
                    expensesJson = expensesJson,
                    totalBill = totalBill,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun clearCustomerBill(customerId: Long) {
        val existing = customerDao.getCustomerById(customerId)
        if (existing != null) {
            customerDao.updateCustomer(
                existing.copy(
                    billItemsJson = "{}",
                    expensesJson = "[]",
                    totalBill = 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    // App Settings
    fun getSettingFlow(key: String): Flow<AppSetting?> = appSettingDao.getSettingFlow(key)
    suspend fun getSettingValue(key: String): String? = appSettingDao.getSetting(key)?.value
    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.insertSetting(AppSetting(key, value))
    }

    suspend fun resetAllData() {
        productDao.clearAllProducts()
        expenseDao.clearAllExpenses()
        customerDao.clearAllCustomers()
        val defaults = getDefaultBengaliProducts()
        productDao.insertProducts(defaults)
        seedCustomers()
    }

    suspend fun importProducts(products: List<Product>) {
        productDao.clearAllProducts()
        productDao.insertProducts(products)
    }
}
