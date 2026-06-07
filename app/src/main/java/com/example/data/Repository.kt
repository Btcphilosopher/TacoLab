package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class TacoLabRepository(private val dao: TacoLabDao) {

    val menuItems: Flow<List<MenuItemEntity>> = dao.getAllMenuItems()
    val orders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val loyaltyProfile: Flow<LoyaltyEntity?> = dao.getLoyaltyProfile()
    val favoriteMeals: Flow<List<FavoriteMealEntity>> = dao.getAllFavoriteMeals()

    suspend fun checkAndPopulateMenuIfEmpty() {
        withContext(Dispatchers.IO) {
            val currentMenu = dao.getAllMenuItems().firstOrNull()
            if (currentMenu.isNullOrEmpty()) {
                TacoLabDatabase.populateInitialData(dao)
            }
        }
    }

    suspend fun placeOrder(order: OrderEntity): Int {
        return withContext(Dispatchers.IO) {
            val orderId = dao.insertOrder(order).toInt()
            
            // Points rate: $1 spent = 15 Loyalty points earned ($10 burrito = 150 points)
            val ptsEarned = (order.totalPrice * 15).toInt()
            val currentProfile = dao.getLoyaltyProfile().firstOrNull() ?: LoyaltyEntity(id = 1, pointsBalance = 0)
            val newPoints = currentProfile.pointsBalance + ptsEarned
            
            // Tiers:
            // Rookie Taco Fan (0 - 250)
            // Taco Regular (251 - 600)
            // Taco Elite (601 - 1200)
            // Taco Legend (1201+)
            val newTier = when {
                newPoints >= 1200 -> "Taco Legend"
                newPoints >= 600 -> "Taco Elite"
                newPoints >= 250 -> "Taco Regular"
                else -> "Rookie Taco Fan"
            }
            
            val nextTierBound = when (newTier) {
                "Rookie Taco Fan" -> 250
                "Taco Regular" -> 600
                "Taco Elite" -> 1200
                else -> 1200
            }
            
            val ptsToNext = if (newTier == "Taco Legend") 0 else (nextTierBound - newPoints).coerceAtLeast(0)
            
            dao.insertLoyaltyProfile(
                currentProfile.copy(
                    pointsBalance = newPoints,
                    tier = newTier,
                    pointsToNextTier = ptsToNext,
                    orderStreakCount = currentProfile.orderStreakCount + 1
                )
            )
            orderId
        }
    }

    suspend fun updateOrderStatus(id: Int, status: String) {
        withContext(Dispatchers.IO) {
            dao.updateOrderStatus(id, status)
        }
    }

    suspend fun updateOrderPrepTime(id: Int, remainingTime: Int) {
        withContext(Dispatchers.IO) {
            dao.updateOrderPrepTime(id, remainingTime)
        }
    }

    suspend fun deleteOrder(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteOrderById(id)
        }
    }

    suspend fun clearAllOrders() {
        withContext(Dispatchers.IO) {
            dao.clearAllOrders()
        }
    }

    suspend fun updateMenuItemAvailability(id: String, isAvailable: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateMenuItemAvailability(id, isAvailable)
        }
    }

    suspend fun addFavoriteMeal(meal: FavoriteMealEntity) {
        withContext(Dispatchers.IO) {
            dao.insertFavoriteMeal(meal)
        }
    }

    suspend fun removeFavoriteMeal(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteFavoriteMeal(id)
        }
    }

    suspend fun redeemLoyaltyPoints(ptsCost: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val currentProfile = dao.getLoyaltyProfile().firstOrNull() ?: return@withContext false
            if (currentProfile.pointsBalance >= ptsCost) {
                val newPoints = currentProfile.pointsBalance - ptsCost
                val newTier = when {
                    newPoints >= 1200 -> "Taco Legend"
                    newPoints >= 600 -> "Taco Elite"
                    newPoints >= 250 -> "Taco Regular"
                    else -> "Rookie Taco Fan"
                }
                val nextTierBound = when (newTier) {
                    "Rookie Taco Fan" -> 250
                    "Taco Regular" -> 600
                    "Taco Elite" -> 1200
                    else -> 1200
                }
                val ptsToNext = if (newTier == "Taco Legend") 0 else (nextTierBound - newPoints).coerceAtLeast(0)
                
                dao.insertLoyaltyProfile(
                    currentProfile.copy(
                        pointsBalance = newPoints,
                        tier = newTier,
                        pointsToNextTier = ptsToNext
                    )
                )
                true
            } else {
                false
            }
        }
    }
}
