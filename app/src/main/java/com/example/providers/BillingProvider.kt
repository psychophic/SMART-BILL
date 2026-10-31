package com.example.providers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BillingRepository
import com.example.models.Product
import com.example.models.Expense
import com.example.models.AppSetting
import com.example.models.Customer
import com.example.models.BillRecord
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class BillingProvider(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BillingRepository(
        db.productDao(),
        db.expenseDao(),
        db.appSettingDao(),
        db.customerDao(),
        db.billRecordDao()
    )

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val billRecords: StateFlow<List<BillRecord>> = repository.allBillRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks quantity per product ID for Instant Counter / active editor
    private val _quantities = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quantities = _quantities.asStateFlow()

    // Screen selection: "home", "instant", "history", "products", "settings"
    private val _currentTab = MutableStateFlow("home")
    val currentTab = _currentTab.asStateFlow()

    // Currently selected customer for billing details (null = on Dashboard)
    private val _activeCustomer = MutableStateFlow<Customer?>(null)
    val activeCustomer = _activeCustomer.asStateFlow()

    // Customer specific active billing draft state
    private val _customerQuantities = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val customerQuantities = _customerQuantities.asStateFlow()

    private val _customerExpenses = MutableStateFlow<List<Double>>(emptyList())
    val customerExpenses = _customerExpenses.asStateFlow()

    // Show summary overlay
    private val _showSummary = MutableStateFlow(false)
    val showSummary = _showSummary.asStateFlow()

    // Dark Mode setting: "SYSTEM", "LIGHT", "DARK"
    private val _darkModeSetting = MutableStateFlow("DARK")
    val darkModeSetting = _darkModeSetting.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
            val dmValue = repository.getSettingValue("dark_mode") ?: "DARK"
            _darkModeSetting.value = dmValue
        }
    }

    // Customer Selection & Draft Management
    fun openCustomerBilling(customer: Customer) {
        _activeCustomer.value = customer
        _customerQuantities.value = parseQuantitiesJson(customer.billItemsJson)
        _customerExpenses.value = parseExpensesJson(customer.expensesJson)
    }

    fun closeCustomerBilling() {
        _activeCustomer.value = null
        _customerQuantities.value = emptyMap()
        _customerExpenses.value = emptyList()
    }

    fun incrementCustomerQuantity(productId: Int) {
        val current = _customerQuantities.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + 1
        _customerQuantities.value = current
    }

    fun decrementCustomerQuantity(productId: Int) {
        val current = _customerQuantities.value.toMutableMap()
        val count = current[productId] ?: 0
        if (count > 1) {
            current[productId] = count - 1
        } else {
            current.remove(productId)
        }
        _customerQuantities.value = current
    }

    fun setCustomerQuantity(productId: Int, qty: Int) {
        val current = _customerQuantities.value.toMutableMap()
        if (qty > 0) {
            current[productId] = qty
        } else {
            current.remove(productId)
        }
        _customerQuantities.value = current
    }

    fun addCustomerExpense(amount: Double) {
        if (amount > 0) {
            _customerExpenses.value = _customerExpenses.value + amount
        }
    }

    fun removeCustomerExpense(index: Int) {
        val list = _customerExpenses.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _customerExpenses.value = list
        }
    }

    fun buildCustomerBillSummary(): String {
        val qMap = _customerQuantities.value
        val expList = _customerExpenses.value
        val prods = products.value.associate { it.id to it.name }
        val parts = mutableListOf<String>()
        qMap.forEach { (pId, qty) ->
            if (qty > 0) {
                val name = prods[pId] ?: "পণ্য"
                parts.add("$name (${DateUtils.toBengaliDigits(qty)})")
            }
        }
        if (expList.isNotEmpty()) {
            val totalExp = expList.sum()
            parts.add("অন্যান্য খরচ (৳${DateUtils.toBengaliDigits(totalExp)})")
        }
        return if (parts.isEmpty()) "নগদ বিল" else parts.joinToString(", ")
    }

    fun saveCustomerDueBill() {
        val customer = _activeCustomer.value ?: return
        val currentProds = products.value
        val prodPriceMap = currentProds.associate { it.id to it.price }

        val qMap = _customerQuantities.value
        val expList = _customerExpenses.value

        var billAmount = 0.0
        qMap.forEach { (pId, qty) ->
            val price = prodPriceMap[pId] ?: 0.0
            billAmount += price * qty
        }
        expList.forEach { exp ->
            billAmount += exp
        }

        // Amount to add to due
        val amountToAdd = if (billAmount > 0) billAmount else 0.0
        val finalDue = customer.totalBill + amountToAdd
        val summary = if (billAmount > 0) buildCustomerBillSummary() else "পূর্বের বাকী"

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            if (amountToAdd > 0) {
                val record = BillRecord(
                    customerId = customer.id,
                    customerName = customer.name,
                    totalAmount = amountToAdd,
                    status = "DUE",
                    itemsSummary = summary,
                    timestamp = timestamp,
                    formattedDate = DateUtils.formatDate(timestamp),
                    formattedTime = DateUtils.formatTime(timestamp),
                    monthKey = DateUtils.getMonthKey(timestamp)
                )
                repository.recordBill(record)
            }

            // Update customer with new due total and clear active draft items
            repository.updateCustomerBill(
                customerId = customer.id,
                billItemsJson = "{}",
                expensesJson = "[]",
                totalBill = finalDue
            )

            _customerQuantities.value = emptyMap()
            _customerExpenses.value = emptyList()
            _activeCustomer.value = customer.copy(
                billItemsJson = "{}",
                expensesJson = "[]",
                totalBill = finalDue,
                lastUpdated = timestamp
            )
        }
    }

    fun settleCustomerPaidBill() {
        val customer = _activeCustomer.value ?: return
        val currentProds = products.value
        val prodPriceMap = currentProds.associate { it.id to it.price }

        val qMap = _customerQuantities.value
        val expList = _customerExpenses.value

        var billAmount = 0.0
        qMap.forEach { (pId, qty) ->
            val price = prodPriceMap[pId] ?: 0.0
            billAmount += price * qty
        }
        expList.forEach { exp ->
            billAmount += exp
        }

        val totalPaidAmount = if (billAmount > 0) {
            billAmount + customer.totalBill
        } else {
            customer.totalBill
        }

        val summary = if (billAmount > 0) {
            val cartSummary = buildCustomerBillSummary()
            if (customer.totalBill > 0) "$cartSummary + পূর্বের বকেয়া পরিশোধ" else cartSummary
        } else {
            "বকেয়া বিল সম্পূর্ণ পরিশোধ"
        }

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            if (totalPaidAmount > 0) {
                val record = BillRecord(
                    customerId = customer.id,
                    customerName = customer.name,
                    totalAmount = totalPaidAmount,
                    status = "PAID",
                    itemsSummary = summary,
                    timestamp = timestamp,
                    formattedDate = DateUtils.formatDate(timestamp),
                    formattedTime = DateUtils.formatTime(timestamp),
                    monthKey = DateUtils.getMonthKey(timestamp)
                )
                repository.recordBill(record)
            }

            // Set customer totalBill to 0 and clear draft
            repository.clearCustomerBill(customer.id)
            _customerQuantities.value = emptyMap()
            _customerExpenses.value = emptyList()
            _activeCustomer.value = customer.copy(
                billItemsJson = "{}",
                expensesJson = "[]",
                totalBill = 0.0,
                lastUpdated = timestamp
            )
        }
    }

    fun deleteBillRecord(record: BillRecord) {
        viewModelScope.launch {
            repository.deleteBillRecord(record)
        }
    }

    fun saveActiveCustomerBill() {
        val customer = _activeCustomer.value ?: return
        val currentProds = products.value
        val prodPriceMap = currentProds.associate { it.id to it.price }

        val qMap = _customerQuantities.value
        val expList = _customerExpenses.value

        var total = 0.0
        qMap.forEach { (pId, qty) ->
            val price = prodPriceMap[pId] ?: 0.0
            total += price * qty
        }
        expList.forEach { exp ->
            total += exp
        }

        val qJson = serializeQuantities(qMap)
        val expJson = serializeExpenses(expList)

        viewModelScope.launch {
            repository.updateCustomerBill(
                customerId = customer.id,
                billItemsJson = qJson,
                expensesJson = expJson,
                totalBill = total
            )
            // Update local active customer copy
            val updated = customer.copy(
                billItemsJson = qJson,
                expensesJson = expJson,
                totalBill = total,
                lastUpdated = System.currentTimeMillis()
            )
            _activeCustomer.value = updated
        }
    }

    fun clearActiveCustomerBill() {
        val customer = _activeCustomer.value ?: return
        viewModelScope.launch {
            repository.clearCustomerBill(customer.id)
            _customerQuantities.value = emptyMap()
            _customerExpenses.value = emptyList()
            _activeCustomer.value = customer.copy(
                billItemsJson = "{}",
                expensesJson = "[]",
                totalBill = 0.0,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun addCustomer(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.addCustomer(name.trim())
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            if (_activeCustomer.value?.id == customer.id) {
                closeCustomerBilling()
            }
            repository.deleteCustomer(customer)
        }
    }

    // Instant / General Counter Quantities
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

    fun recordInstantCounterSale(amount: Double, summary: String, isDue: Boolean = false, customerName: String = "কাউন্টার ক্রেতা") {
        if (amount <= 0) return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val record = BillRecord(
                customerId = 0L,
                customerName = customerName,
                totalAmount = amount,
                status = if (isDue) "DUE" else "PAID",
                itemsSummary = summary,
                timestamp = timestamp,
                formattedDate = DateUtils.formatDate(timestamp),
                formattedTime = DateUtils.formatTime(timestamp),
                monthKey = DateUtils.getMonthKey(timestamp)
            )
            repository.recordBill(record)
            resetQuantities()
            clearAllExpenses()
        }
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

    // Other Expenses Management (General Counter)
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

    // Theme Settings
    fun setDarkModeSetting(mode: String) {
        _darkModeSetting.value = mode
        viewModelScope.launch {
            repository.saveSetting("dark_mode", mode)
        }
    }

    // Data Reset
    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetAllData()
            _quantities.value = emptyMap()
            _activeCustomer.value = null
            _customerQuantities.value = emptyMap()
            _customerExpenses.value = emptyList()
        }
    }

    // JSON Helper Utilities
    companion object {
        fun parseQuantitiesJson(json: String?): Map<Int, Int> {
            if (json.isNullOrBlank() || json == "{}") return emptyMap()
            return try {
                val map = mutableMapOf<Int, Int>()
                val obj = JSONObject(json)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k.toInt()] = obj.getInt(k)
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }
        }

        fun serializeQuantities(map: Map<Int, Int>): String {
            val obj = JSONObject()
            map.forEach { (k, v) ->
                if (v > 0) obj.put(k.toString(), v)
            }
            return obj.toString()
        }

        fun parseExpensesJson(json: String?): List<Double> {
            if (json.isNullOrBlank() || json == "[]") return emptyList()
            return try {
                val list = mutableListOf<Double>()
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    list.add(arr.getDouble(i))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeExpenses(list: List<Double>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it) }
            return arr.toString()
        }
    }
}
