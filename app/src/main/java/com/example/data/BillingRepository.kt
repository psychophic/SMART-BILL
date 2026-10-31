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
            val defaults = listOf(
                Product(name = "Carrom Bill", price = 20.0, iconType = "GAME", isDefault = true),
                Product(name = "Benson", price = 20.0, iconType = "SMOKE", isDefault = true),
                Product(name = "Lucky Strike Switch", price = 12.0, iconType = "SMOKE", isDefault = true),
                Product(name = "Camel", price = 10.0, iconType = "SMOKE", isDefault = true),
                Product(name = "Royal", price = 8.0, iconType = "SMOKE", isDefault = true),
                Product(name = "Derby", price = 8.0, iconType = "SMOKE", isDefault = true),
                Product(name = "Lal Tea", price = 5.0, iconType = "TEA", isDefault = true),
                Product(name = "Black Coffee", price = 10.0, iconType = "COFFEE", isDefault = true),
                Product(name = "Milk Coffee", price = 20.0, iconType = "COFFEE", isDefault = true)
            )
            productDao.insertProducts(defaults)
        }
    }

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
        val defaults = listOf(
            Product(name = "Carrom Bill", price = 20.0, iconType = "GAME", isDefault = true),
            Product(name = "Benson", price = 20.0, iconType = "SMOKE", isDefault = true),
            Product(name = "Lucky Strike Switch", price = 12.0, iconType = "SMOKE", isDefault = true),
            Product(name = "Camel", price = 10.0, iconType = "SMOKE", isDefault = true),
            Product(name = "Royal", price = 8.0, iconType = "SMOKE", isDefault = true),
            Product(name = "Derby", price = 8.0, iconType = "SMOKE", isDefault = true),
            Product(name = "Lal Tea", price = 5.0, iconType = "TEA", isDefault = true),
            Product(name = "Black Coffee", price = 10.0, iconType = "COFFEE", isDefault = true),
            Product(name = "Milk Coffee", price = 20.0, iconType = "COFFEE", isDefault = true)
        )
        productDao.insertProducts(defaults)
    }

    suspend fun importProducts(products: List<Product>) {
        productDao.clearAllProducts()
        productDao.insertProducts(products)
    }
}
