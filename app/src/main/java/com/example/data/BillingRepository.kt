package com.example.data

import com.example.models.Product
import com.example.models.Expense
import com.example.models.AppSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BillingRepository(
    private val productDao: ProductDao,
    private val expenseDao: ExpenseDao,
    private val appSettingDao: AppSettingDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun seedDefaultProductsIfEmpty() {
        val current = productDao.getAllProducts().first()
        if (current.isEmpty()) {
            val defaults = getDefaultBengaliProducts()
            productDao.insertProducts(defaults)
        } else {
            // Auto-migrate any existing English default names to Bengali
            val englishToBengali = mapOf(
                "Carrom Bill" to "ক্যারম বিল",
                "Benson" to "বেনসন",
                "Lucky Strike Switch" to "লাকি স্ট্রাইক সুইচ",
                "Camel" to "ক্যামেল",
                "Royal" to "রয়েল",
                "Derby" to "ডার্বি",
                "Lal Tea" to "লাল চা",
                "Black Coffee" to "ব্ল্যাক কফি",
                "Milk Coffee" to "দুধ কফি"
            )
            current.forEach { prod ->
                val banglaName = englishToBengali[prod.name]
                if (banglaName != null) {
                    productDao.updateProduct(prod.copy(name = banglaName))
                }
            }
        }
    }

    private fun getDefaultBengaliProducts(): List<Product> = listOf(
        Product(name = "ক্যারম বিল", price = 20.0, iconType = "GAME", isDefault = true),
        Product(name = "বেনসন", price = 20.0, iconType = "SMOKE", isDefault = true),
        Product(name = "লাকি স্ট্রাইক সুইচ", price = 12.0, iconType = "SMOKE", isDefault = true),
        Product(name = "ক্যামেল", price = 10.0, iconType = "SMOKE", isDefault = true),
        Product(name = "রয়েল", price = 8.0, iconType = "SMOKE", isDefault = true),
        Product(name = "ডার্বি", price = 8.0, iconType = "SMOKE", isDefault = true),
        Product(name = "লাল চা", price = 5.0, iconType = "TEA", isDefault = true),
        Product(name = "ব্ল্যাক কফি", price = 10.0, iconType = "COFFEE", isDefault = true),
        Product(name = "দুধ কফি", price = 20.0, iconType = "COFFEE", isDefault = true)
    )

    suspend fun addProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun clearAllExpenses() = expenseDao.clearAllExpenses()

    fun getSettingFlow(key: String): Flow<AppSetting?> = appSettingDao.getSettingFlow(key)
    suspend fun getSettingValue(key: String): String? = appSettingDao.getSetting(key)?.value
    suspend fun saveSetting(key: String, value: String) {
        appSettingDao.insertSetting(AppSetting(key, value))
    }

    suspend fun resetAllData() {
        productDao.clearAllProducts()
        expenseDao.clearAllExpenses()
        val defaults = getDefaultBengaliProducts()
        productDao.insertProducts(defaults)
    }

    suspend fun importProducts(products: List<Product>) {
        productDao.clearAllProducts()
        productDao.insertProducts(products)
    }
}
