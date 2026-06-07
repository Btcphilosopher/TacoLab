package com.example.data

import androidx.room.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class CustomizedItem(
    val baseItemId: String,
    val baseItemName: String,
    val category: String,
    val selectedProtein: String,     // Beef, Chicken, Pork, Vegan, None
    val selectedSpice: String,       // Mild, Medium, Hot, Inferno
    val selectedSalsas: List<String>,// Pico, Salsa Verde, Fire Salsa, Corn Salsa
    val selectedCheese: String,      // Cheddar, Cotija, Queso Sauce, None
    val selectedExtras: List<String>,// Guac, Jalapeños, Beans, Rice, Crema
    val selectedTortilla: String,    // Flour Tortilla, Corn Tortilla, Bowl (None)
    val price: Double,
    val calories: Int
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // Tacos, Burritos, Quesadillas, Nachos, Bowls, Fries, Drinks, Desserts
    val basePrice: Double,
    val calories: Int,
    val isAvailable: Boolean = true,
    val description: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val itemsJson: String, // JSON serialization of List<CustomizedItem>
    val totalPrice: Double,
    val pointsEarned: Int,
    val status: String,    // PENDING, PREPARING, COMPLETED, OUT_FOR_DELIVERY, DRIVETHRU_QUEUE
    val orderType: String, // PICKUP, DINE_IN, DRIVE_THRU, DELIVERY
    val timestamp: Long = System.currentTimeMillis(),
    val prepTimeRemaining: Int = 180, // seconds
    val laneAssignment: String? = null, // for drive-thru (e.g. "Lane B")
    val tableNumber: String? = null,    // for dine-in QR (e.g. "Table 12")
    val deliveryPartner: String? = null,// Uber Eats, DoorDash, Just Eat
    val deliveryEta: Int = 0           // minutes
)

@Entity(tableName = "loyalty_profile")
data class LoyaltyEntity(
    @PrimaryKey val id: Int = 1,
    val pointsBalance: Int = 180,
    val tier: String = "Rookie Taco Fan", // Rookie Taco Fan, Taco Regular, Taco Elite, Taco Legend
    val pointsToNextTier: Int = 120,
    val orderStreakCount: Int = 4,
    val referralsCount: Int = 2
)

@Entity(tableName = "favorite_meals")
data class FavoriteMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealName: String,
    val baseItemId: String,
    val baseItemName: String,
    val customizationJson: String, // JSON serialization of CustomizedItem representation
    val price: Double,
    val calories: Int
)

class TacoLabConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromCustomizedItemList(value: String): List<CustomizedItem> {
        val type = Types.newParameterizedType(List::class.java, CustomizedItem::class.java)
        val adapter = moshi.adapter<List<CustomizedItem>>(type)
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toCustomizedItemList(list: List<CustomizedItem>): String {
        val type = Types.newParameterizedType(List::class.java, CustomizedItem::class.java)
        val adapter = moshi.adapter<List<CustomizedItem>>(type)
        return adapter.toJson(list)
    }
}
