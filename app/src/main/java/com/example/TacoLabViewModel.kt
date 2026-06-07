package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TacoLabViewModel(application: Application) : AndroidViewModel(application) {

    private val database: TacoLabDatabase by lazy {
        TacoLabDatabase.getDatabase(application, viewModelScope)
    }
    
    val repository: TacoLabRepository by lazy {
        TacoLabRepository(database.tacoLabDao())
    }

    // State flows from Room
    val menuItems = repository.menuItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders = repository.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loyaltyProfile = repository.loyaltyProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val favoriteMeals = repository.favoriteMeals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart and order workflow state
    private val _cart = MutableStateFlow<List<CustomizedItem>>(emptyList())
    val cart: StateFlow<List<CustomizedItem>> = _cart.asStateFlow()

    // The item currently being customized in the Customization Dialog/Screen
    private val _customizingItem = MutableStateFlow<CustomizedItem?>(null)
    val customizingItem: StateFlow<CustomizedItem?> = _customizingItem.asStateFlow()

    // Customer settings
    private val _selectedDietType = MutableStateFlow("All (No Limit)") // Options: All (No Limit), Keto, Vegan, High Protein
    val selectedDietType: StateFlow<String> = _selectedDietType.asStateFlow()

    private val _simulatedWeather = MutableStateFlow("Sunny & Cool 18°C") // Options: Sunny & Cool 18°C, Thunderstorming 10°C, Hot Wave 32°C, Snowflake Chill 1°C
    val simulatedWeather: StateFlow<String> = _simulatedWeather.asStateFlow()

    // Active screen navigation role
    // Roles: CUSTOMER, KITCHEN_KDS, STAFF_ADMIN, ARCHITECT_SPECS
    private val _currentRole = MutableStateFlow("CUSTOMER")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // AI recommendation state
    private val _aiRecommendation = MutableStateFlow<AiRecommendObj?>(null)
    val aiRecommendation: StateFlow<AiRecommendObj?> = _aiRecommendation.asStateFlow()

    // Screen-level state notifications
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        // Ensure menu data is loaded on launch
        viewModelScope.launch {
            repository.checkAndPopulateMenuIfEmpty()
            updateAiRecommendation()
        }

        // Setup real-time order background simulation (decrements remaining prepare time, advances states)
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(4000) // tick every 4 seconds for immediate visual impact
                val currentOrders = repository.orders.firstOrNull() ?: emptyList()
                currentOrders.forEach { ord ->
                    if (ord.status == "PENDING") {
                        repository.updateOrderStatus(ord.id, "PREPARING")
                    } else if (ord.status == "PREPARING") {
                        if (ord.prepTimeRemaining <= 30) {
                            val nextStatus = when (ord.orderType) {
                                "DRIVE_THRU" -> "DRIVETHRU_QUEUE"
                                "DELIVERY" -> "OUT_FOR_DELIVERY"
                                else -> "COMPLETED"
                            }
                            repository.updateOrderStatus(ord.id, nextStatus)
                            repository.updateOrderPrepTime(ord.id, 0)
                        } else {
                            repository.updateOrderPrepTime(ord.id, ord.prepTimeRemaining - 30)
                        }
                    } else if (ord.status == "DRIVETHRU_QUEUE" || ord.status == "OUT_FOR_DELIVERY") {
                        // Slowly complete over time
                        if (ord.prepTimeRemaining <= 0) {
                            repository.updateOrderStatus(ord.id, "COMPLETED")
                        } else {
                            repository.updateOrderPrepTime(ord.id, 0)
                        }
                    }
                }
            }
        }

        // Re-calculate AI match when user changes diet or weather
        viewModelScope.launch {
            combine(_selectedDietType, _simulatedWeather) { diet, weather ->
                Pair(diet, weather)
            }.collect {
                updateAiRecommendation()
            }
        }
    }

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun setDietLimit(diet: String) {
        _selectedDietType.value = diet
    }

    fun setWeather(weather: String) {
        _simulatedWeather.value = weather
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_snackbarMessage.value == msg) {
                _snackbarMessage.value = null
            }
        }
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    // CART OPERATIONS
    fun applyDefaultCustomization(menuItem: MenuItemEntity) {
        val defaultCustom = createDefaultCustomization(menuItem)
        _customizingItem.value = defaultCustom
    }

    fun setCustomizingItem(item: CustomizedItem?) {
        _customizingItem.value = item
    }

    fun updateCustomizingProtein(protein: String) {
        _customizingItem.value?.let { current ->
            val updated = current.copy(selectedProtein = protein)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun updateCustomizingSpice(spice: String) {
        _customizingItem.value?.let { current ->
            val updated = current.copy(selectedSpice = spice)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun toggleCustomizingSalsa(salsa: String) {
        _customizingItem.value?.let { current ->
            val salsas = current.selectedSalsas.toMutableList()
            if (salsas.contains(salsa)) {
                salsas.remove(salsa)
            } else {
                salsas.add(salsa)
            }
            val updated = current.copy(selectedSalsas = salsas)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun updateCustomizingCheese(cheese: String) {
        _customizingItem.value?.let { current ->
            val updated = current.copy(selectedCheese = cheese)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun toggleCustomizingExtra(extra: String) {
        _customizingItem.value?.let { current ->
            val extras = current.selectedExtras.toMutableList()
            if (extras.contains(extra)) {
                extras.remove(extra)
            } else {
                extras.add(extra)
            }
            val updated = current.copy(selectedExtras = extras)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun updateCustomizingTortilla(tortilla: String) {
        _customizingItem.value?.let { current ->
            val updated = current.copy(selectedTortilla = tortilla)
            _customizingItem.value = recalcItem(updated)
        }
    }

    fun addCustomizedToCart() {
        _customizingItem.value?.let { item ->
            val currentCart = _cart.value.toMutableList()
            currentCart.add(item)
            _cart.value = currentCart
            _customizingItem.value = null
            showMessage("Added 1x ${item.baseItemName} to cart!")
        }
    }

    fun removeFromCart(index: Int) {
        val currentCart = _cart.value.toMutableList()
        if (index in currentCart.indices) {
            val removedItem = currentCart.removeAt(index)
            _cart.value = currentCart
            showMessage("Removed ${removedItem.baseItemName} from cart.")
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Instantly order or reorder a meal
    fun quickOrderMeal(item: CustomizedItem, type: String = "PICKUP") {
        viewModelScope.launch {
            val total = item.price
            val name = "Guest Customer"
            
            // Generate some random details
            val tableNum = if (type == "DINE_IN") "Table " + (1..24).random() else null
            val laneNum = if (type == "DRIVE_THRU") "Lane " + listOf("Alpha", "Beta", "Gamma").random() else null
            val deliveryPartner = if (type == "DELIVERY") listOf("DoorDash", "Uber Eats", "Just Eat").random() else null
            val deliveryEta = if (type == "DELIVERY") (15..30).random() else 0

            val itemsListJson = TacoLabConverters().toCustomizedItemList(listOf(item))

            val order = OrderEntity(
                customerName = name,
                itemsJson = itemsListJson,
                totalPrice = total,
                pointsEarned = (total * 15).toInt(),
                status = "PENDING",
                orderType = type,
                prepTimeRemaining = 120, // 2 minutes
                tableNumber = tableNum,
                laneAssignment = laneNum,
                deliveryPartner = deliveryPartner,
                deliveryEta = deliveryEta
            )

            val orderId = repository.placeOrder(order)
            showMessage("Order #$orderId ($type) submitted successfully!")
        }
    }

    fun checkoutCart(orderType: String, redeemPoints: Boolean = false, pointsCost: Int = 0) {
        val currentCartItems = _cart.value
        if (currentCartItems.isEmpty()) {
            showMessage("Cart is empty!")
            return
        }

        viewModelScope.launch {
            if (redeemPoints) {
                val success = repository.redeemLoyaltyPoints(pointsCost)
                if (!success) {
                    showMessage("Insufficient loyalty points!")
                    return@launch
                }
            }

            val total = if (redeemPoints) 0.0 else currentCartItems.sumOf { it.price }
            val itemsListJson = TacoLabConverters().toCustomizedItemList(currentCartItems)

            // Random details
            val tableNum = if (orderType == "DINE_IN") "Table " + (1..30).random() else null
            val laneNum = if (orderType == "DRIVE_THRU") "Lane " + listOf("A", "B", "C").random() else null
            val deliveryPartner = if (orderType == "DELIVERY") listOf("Uber Eats", "DoorDash", "Just Eat").random() else null
            val deliveryEta = if (orderType == "DELIVERY") (15..40).random() else 0

            val order = OrderEntity(
                customerName = "Alex Rivera",
                itemsJson = itemsListJson,
                totalPrice = total,
                pointsEarned = if (redeemPoints) 0 else (total * 15).toInt(),
                status = "PENDING",
                orderType = orderType,
                prepTimeRemaining = 180,
                tableNumber = tableNum,
                laneAssignment = laneNum,
                deliveryPartner = deliveryPartner,
                deliveryEta = deliveryEta
            )

            val id = repository.placeOrder(order)
            _cart.value = emptyList() // clear cart
            showMessage("Order #$id successfully created! Preparing now.")
        }
    }

    // FAVORITES
    fun saveFavoriteMeal(customItem: CustomizedItem, customName: String) {
        viewModelScope.launch {
            val json = TacoLabConverters().toCustomizedItemList(listOf(customItem))
            val favorite = FavoriteMealEntity(
                mealName = customName,
                baseItemId = customItem.baseItemId,
                baseItemName = customItem.baseItemName,
                customizationJson = json,
                price = customItem.price,
                calories = customItem.calories
            )
            repository.addFavoriteMeal(favorite)
            showMessage("Saved '$customName' to Favorite Orders!")
        }
    }

    fun deleteOrder(id: Int) {
        viewModelScope.launch {
            repository.deleteOrder(id)
            showMessage("Order #$id removed from queue.")
        }
    }

    fun deleteFavoriteMeal(id: Int) {
        viewModelScope.launch {
            repository.removeFavoriteMeal(id)
            showMessage("Favorite order deleted.")
        }
    }

    // STAFF / ADMIN CONTROLS
    fun toggleMenuAvailable(itemId: String, isNowAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateMenuItemAvailability(itemId, isNowAvailable)
            showMessage("Menu item updated: " + (if (isNowAvailable) "Available" else "OUT OF STOCK"))
        }
    }

    fun kitchenUpdateOrderStatus(id: Int, nextStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(id, nextStatus)
            if (nextStatus == "COMPLETED") {
                repository.updateOrderPrepTime(id, 0)
                showMessage("Order #$id marked COMPLETED and ready for handover!")
            } else {
                showMessage("Order #$id status set to $nextStatus")
            }
        }
    }

    // AI SUGGESTION CRUNCHER
    private fun updateAiRecommendation() {
        val weather = _simulatedWeather.value
        val diet = _selectedDietType.value
        
        // Let's check typical recommendation rules:
        val recommend = when {
            diet == "Vegan" -> {
                val baseItem = CustomizedItem(
                    baseItemId = "burrito_cantina",
                    baseItemName = "Organic Sofritas Burrito Wrap",
                    category = "Burritos",
                    selectedProtein = "Vegan", // Sofritas
                    selectedSpice = "Medium",
                    selectedSalsas = listOf("Pico", "Salsa Verde"),
                    selectedCheese = "None",
                    selectedExtras = listOf("Guac", "Rice", "Beans"),
                    selectedTortilla = "Flour Tortilla",
                    price = 8.99,
                    calories = 620
                )
                AiRecommendObj(
                    title = "Pure Organics Custom",
                    tagline = "Green Lab Recommendation",
                    description = "It's $weather outside — stay nourished with a spicy Sofritas burrito packed with plant-based proteins, hand-crushed guac, and direct fiber beans!",
                    item = baseItem
                )
            }
            diet == "Keto" -> {
                val baseItem = CustomizedItem(
                    baseItemId = "bowl_keto_tex",
                    baseItemName = "High-Voltage Keto Bowl",
                    category = "Bowls",
                    selectedProtein = "Chicken",
                    selectedSpice = "Hot",
                    selectedSalsas = listOf("Pico", "Fire Salsa"),
                    selectedCheese = "Cotija",
                    selectedExtras = listOf("Guac", "Crema"),
                    selectedTortilla = "Bowl (None)",
                    price = 9.39,
                    calories = 480
                )
                AiRecommendObj(
                    title = "Keto Peak Fit Bowl",
                    tagline = "Low Carb Lab Blend",
                    description = "Match your limits: high-protein chicken paired with Cotija crumble, clean fats from fresh avocado, and a dash of hot volcanic chili salsa.",
                    item = baseItem
                )
            }
            diet == "High Protein" -> {
                val baseItem = CustomizedItem(
                    baseItemId = "burrito_supreme",
                    baseItemName = "Double Stack Protein Bomber",
                    category = "Burritos",
                    selectedProtein = "Beef",
                    selectedSpice = "Inferno",
                    selectedSalsas = listOf("Pico", "Fire Salsa", "Corn Salsa"),
                    selectedCheese = "Queso Sauce",
                    selectedExtras = listOf("Beans", "Crema"),
                    selectedTortilla = "Flour Tortilla",
                    price = 9.38,
                    calories = 890
                )
                AiRecommendObj(
                    title = "Double Protein Lab Wrap",
                    tagline = "High Calorie Peak",
                    description = "Unleash performance: beef layers, high-density black beans, double queso cream, and a splash of wild fire pepper to ignite the thermogenic clock.",
                    item = baseItem
                )
            }
            weather.contains("Thunderstorming") || weather.contains("Chill") -> {
                // Cold weather recommends warm, high spice comfort foods!
                val baseItem = CustomizedItem(
                    baseItemId = "burrito_supreme",
                    baseItemName = "Extreme Inferno Fire Burrito",
                    category = "Burritos",
                    selectedProtein = "Beef",
                    selectedSpice = "Inferno",
                    selectedSalsas = listOf("Fire Salsa", "Pico"),
                    selectedCheese = "Queso Sauce",
                    selectedExtras = listOf("Jalapeños", "Beans"),
                    selectedTortilla = "Flour Tortilla",
                    price = 9.58,
                    calories = 810
                )
                AiRecommendObj(
                    title = "Volcanic Comfort Wrap",
                    tagline = "Thermogenic Storm Match",
                    description = "It's cold or storming outside ($weather) — warm your core with a high-intensity spice beef wrap with extra jalapeños, warm cheese queso, and hot habanero fire salsa!",
                    item = baseItem
                )
            }
            else -> {
                // Default recommendation (Baja Freeze + double taco)
                val baseItem = CustomizedItem(
                    baseItemId = "taco_volcano",
                    baseItemName = "Spicy Baja Chicken Lava Taco",
                    category = "Tacos",
                    selectedProtein = "Chicken",
                    selectedSpice = "Hot",
                    selectedSalsas = listOf("Salsa Verde"),
                    selectedCheese = "Cheddar",
                    selectedExtras = listOf("Jalapeños"),
                    selectedTortilla = "Corn Tortilla",
                    price = 3.19,
                    calories = 360
                )
                AiRecommendObj(
                    title = "Baja Sol Crimson Taco",
                    tagline = "Chef Special recommendation",
                    description = "Enjoying the sun! Swipe on our crunchy red lava corn taco filled with fiery lime juice grilled chicken, loaded salsa, and a cold drink bypass.",
                    item = baseItem
                )
            }
        }
        _aiRecommendation.value = recommend
    }

    // HELPER CREATORS & PRICERS
    private fun createDefaultCustomization(menuItem: MenuItemEntity): CustomizedItem {
        return CustomizedItem(
            baseItemId = menuItem.id,
            baseItemName = menuItem.name,
            category = menuItem.category,
            selectedProtein = when {
                menuItem.category == "Drinks" || menuItem.category == "Desserts" -> "None"
                menuItem.id == "bowl_keto_tex" || menuItem.id == "taco_volcano" || menuItem.id == "quesadilla_mega" -> "Chicken"
                menuItem.id == "burrito_cantina" -> "Pork"
                else -> "Beef"
            },
            selectedSpice = if (menuItem.id == "taco_volcano") "Hot" else "Medium",
            selectedSalsas = if (menuItem.category == "Drinks" || menuItem.category == "Desserts") emptyList() else listOf("Pico"),
            selectedCheese = if (menuItem.category == "Drinks" || menuItem.category == "Desserts") "None" else "Cheddar",
            selectedExtras = emptyList(),
            selectedTortilla = when (menuItem.category) {
                "Tacos" -> "Corn Tortilla"
                "Burritos" -> "Flour Tortilla"
                "Quesadillas" -> "Flour Tortilla"
                else -> "Bowl (None)"
            },
            price = menuItem.basePrice,
            calories = menuItem.calories
        )
    }

    private fun recalcItem(item: CustomizedItem): CustomizedItem {
        val (newPrice, newCalories) = calculatePriceAndCalories(item)
        return item.copy(price = newPrice, calories = newCalories)
    }

    private fun calculatePriceAndCalories(item: CustomizedItem): Pair<Double, Int> {
        val basePrice = when (item.baseItemId) {
            "taco_crunch" -> 3.49
            "taco_volcano" -> 2.89
            "burrito_cantina" -> 8.49
            "burrito_supreme" -> 7.99
            "quesadilla_mega" -> 5.99
            "nachos_bell_lab" -> 7.29
            "bowl_keto_tex" -> 8.99
            "fries_mexi_lab" -> 3.49
            "drink_freeze_baja" -> 2.79
            "dessert_churros" -> 3.19
            else -> 4.99
        }

        val baseCalories = when (item.baseItemId) {
            "taco_crunch" -> 320
            "taco_volcano" -> 290
            "burrito_cantina" -> 780
            "burrito_supreme" -> 650
            "quesadilla_mega" -> 610
            "nachos_bell_lab" -> 1100
            "bowl_keto_tex" -> 430
            "fries_mexi_lab" -> 450
            "drink_freeze_baja" -> 190
            "dessert_churros" -> 360
            else -> 400
        }

        var price = basePrice
        var calories = baseCalories

        // Protein modifications
        when (item.selectedProtein) {
            "Beef" -> {
                if (item.baseItemId != "taco_crunch" && item.baseItemId != "burrito_supreme" && item.baseItemId != "nachos_bell_lab") {
                    price += 1.25
                }
                calories += 140
            }
            "Chicken" -> {
                if (item.baseItemId == "burrito_cantina" || item.baseItemId == "taco_crunch") {
                    price += 0.50
                }
                calories += 110
            }
            "Pork" -> { // Braised carnitas
                if (item.baseItemId != "burrito_cantina") {
                    price += 1.75
                }
                calories += 160
            }
            "Vegan" -> { // Sofritas organic chunks
                price += 0.75
                calories += 90
            }
            "None" -> {
                price = (price - 0.75).coerceAtLeast(0.99)
                calories = (calories - 120).coerceAtLeast(10)
            }
        }

        // Cheese modifications
        when (item.selectedCheese) {
            "Cheddar" -> { calories += 90 }
            "Cotija" -> { price += 0.35; calories += 70 }
            "Queso Sauce" -> { price += 0.89; calories += 120 }
            "None" -> {
                price = (price - 0.30).coerceAtLeast(0.99)
                calories = (calories - 80).coerceAtLeast(10)
            }
        }

        // Extras additions
        item.selectedExtras.forEach { extra ->
            when (extra) {
                "Guac" -> { price += 1.99; calories += 140 }
                "Jalapeños" -> { price += 0.30; calories += 5 }
                "Beans" -> { price += 0.49; calories += 90 }
                "Rice" -> { price += 0.49; calories += 140 }
                "Crema" -> { price += 0.39; calories += 60 }
            }
        }

        // Tortilla modifications
        when (item.selectedTortilla) {
            "Flour Tortilla" -> { calories += 180 }
            "Corn Tortilla" -> { calories += 100 }
            "Bowl (None)" -> { /* standard unmodified */ }
        }

        return Pair(Math.round(price * 100.0) / 100.0, calories.coerceAtLeast(10))
    }
}

data class AiRecommendObj(
    val title: String,
    val tagline: String,
    val description: String,
    val item: CustomizedItem
)
