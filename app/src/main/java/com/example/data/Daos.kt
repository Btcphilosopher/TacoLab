package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TacoLabDao {
    // Menu Services
    @Query("SELECT * FROM menu_items ORDER BY id ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateMenuItemAvailability(id: String, isAvailable: Boolean)

    // Order Services (KDS & Customers)
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE orders SET prepTimeRemaining = :remainingTime WHERE id = :id")
    suspend fun updateOrderPrepTime(id: Int, remainingTime: Int)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("DELETE FROM orders")
    suspend fun clearAllOrders()

    // Loyalty Platform Services
    @Query("SELECT * FROM loyalty_profile WHERE id = 1")
    fun getLoyaltyProfile(): Flow<LoyaltyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyProfile(profile: LoyaltyEntity)

    @Query("UPDATE loyalty_profile SET pointsBalance = :points, tier = :tier, pointsToNextTier = :next",)
    suspend fun updateLoyaltyPoints(points: Int, tier: String, next: Int)

    // Favorites & Reorders
    @Query("SELECT * FROM favorite_meals ORDER BY id DESC")
    fun getAllFavoriteMeals(): Flow<List<FavoriteMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMeal(meal: FavoriteMealEntity)

    @Query("DELETE FROM favorite_meals WHERE id = :id")
    suspend fun deleteFavoriteMeal(id: Int)
}
