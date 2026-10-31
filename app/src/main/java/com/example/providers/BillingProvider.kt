package com.example.providers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BillingRepository
import com.example.models.Product
import com.example.models.Expense
import com.example.models.AppSetting
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillingProvider(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BillingRepository(db.productDao(), db.expenseDao(), db.appSettingDao())

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks quantity per product ID
    private val _quantities = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quantities = _quantities.asStateFlow()

    // Screen selection: "home", "products", "settings"
    private val _currentTab = MutableStateFlow("home")
    val currentTab = _currentTab.asStateFlow()

    // Show summary overlay
    private val _showSummary = MutableStateFlow(false)
    val showSummary = _showSummary.asStateFlow()

    // Dark Mode setting: "SYSTEM", "LIGHT", "DARK"
    private val _darkModeSetting = MutableStateFlow("SYSTEM")
    val darkModeSetting = _darkModeSetting.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultProductsIfEmpty()
            val dmValue = repository.getSettingValue("dark_mode") ?: "SYSTEM"
            _darkModeSetting.value = dmValue
        }
    }

    // Adjust quantity
    fun incrementQuantity(productId: Int) {
        val currentMap = _quantities.value.toMutableMap()
        val currentQty = currentMap[productId] ?: 0
        currentMap[productId] = currentQty + 1
        _quantities.value = currentMap
    }

    fun decrementQuantity(productId: Int) {
        val currentMap = _quantities.value.toMutableMap()
        val currentQty = currentMap[productId] ?: 0
        if (currentQty > 0) {
            currentMap[productId] = currentQty - 1
            _quantities.value = currentMap
        }
    }

    fun setQuantity(productId: Int, qty: Int) {
        if (qty >= 0) {
            val currentMap = _quantities.value.toMutableMap()
            currentMap[productId] = qty
            _quantities.value = currentMap
        }
    }

    fun resetQuantities() {
        _quantities.value = emptyMap()
    }

    fun resetAllBill() {
        _quantities.value = emptyMap()
        clearAllExpenses()
        _showSummary.value = false
    }

    // Navigation
    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun setShowSummary(show: Boolean) {
        _showSummary.value = show
    }

    // Products Management
    fun addProduct(name: String, price: Double, iconType: String) {
        viewModelScope.launch {
            val p = Product(name = name, price = price, iconType = iconType)
            repository.addProduct(p)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            val currentMap = _quantities.value.toMutableMap()
            if (currentMap.containsKey(product.id)) {
                currentMap.remove(product.id)
                _quantities.value = currentMap
            }
        }
    }

    // Other Expenses Management
    fun addExpense(amount: Double) {
        viewModelScope.launch {
            val exp = Expense(amount = amount)
            repository.addExpense(exp)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            repository.clearAllExpenses()
        }
    }

    // Settings Management
    fun setDarkMode(mode: String) {
        _darkModeSetting.value = mode
        viewModelScope.launch {
            repository.saveSetting("dark_mode", mode)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _quantities.value = emptyMap()
            _showSummary.value = false
        }
    }

    // Backup & Restore
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listProductType = Types.newParameterizedType(List::class.java, Product::class.java)

    fun exportBackupJson(): String {
        return try {
            val pList = products.value
            val adapter = moshi.adapter<List<Product>>(listProductType)
            adapter.toJson(pList)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun importBackupJson(json: String): Boolean {
        return try {
            val adapter = moshi.adapter<List<Product>>(listProductType)
            val list = adapter.fromJson(json)
            if (list != null && list.isNotEmpty()) {
                viewModelScope.launch {
                    repository.importProducts(list)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
