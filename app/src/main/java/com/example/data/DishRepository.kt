package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DishRepository(private val dishDao: DishDao) {
    
    val allDishes: Flow<List<Dish>> = dishDao.getAllDishes()
    val recentHistory: Flow<List<HistoryEntry>> = dishDao.getRecentHistory()

    fun getDishesByCategory(category: String): Flow<List<Dish>> {
        return dishDao.getDishesByCategory(category)
    }

    suspend fun insertDish(dish: Dish) {
        dishDao.insertDish(dish)
    }

    suspend fun insertHistoryEntry(entry: HistoryEntry) {
        dishDao.insertHistoryEntry(entry)
    }

    suspend fun clearHistory() {
        dishDao.clearHistory()
    }

    suspend fun deleteDish(dish: Dish) {
        dishDao.deleteDish(dish)
    }

    suspend fun deleteDishById(id: Int) {
        dishDao.deleteDishById(id)
    }

    suspend fun prepopulateDefaultDishes() {
        val categoriesWithDefaults = mapOf(
            "Global Favorites" to listOf(
                "Pizza 🍕", "Burger 🍔", "Sushi 🍣", "Tacos 🌮", 
                "Pasta 🍝", "Salad 🥗", "Ramen 🍜", "Curry 🍛"
            ),
            "South Indian" to listOf(
                "Masala Dosa 🥞", "Idli Sambar 🫓", "Medu Vada 🍩", "Uttapam 🥞", 
                "Pongal 🥣", "Lemon Rice 🍋", "Appam 🥞", "Filter Coffee ☕"
            ),
            "North Indian" to listOf(
                "Butter Chicken 🍗", "Paneer Tikka 🧀", "Chole Bhature 🫓", "Biryani 🍲", 
                "Dal Makhani 🥣", "Samosa 🥟", "Naan 🫓", "Tandoori Chicken 🍗"
            ),
            "Breakfast" to listOf(
                "Pancakes 🥞", "Waffles 🧇", "Omelette 🍳", "Oatmeal 🥣", 
                "Fruit Bowl 🍓", "Avocado Toast 🥑", "French Toast 🍞", "Bagel with Lox 🥯"
            ),
            "Beverages" to listOf(
                "Mango Lassi 🥭", "Masala Chai ☕", "Smoothie 🥤", "Fresh Juice 🍊", 
                "Iced Tea 🍹", "Lemonade 🍋", "Milkshake 🥛", "Hot Chocolate ☕"
            )
        )

        for ((category, dishes) in categoriesWithDefaults) {
            val existing = dishDao.getDishesByCategory(category).first()
            if (existing.isEmpty()) {
                val list = dishes.map { name ->
                    Dish(name = name, originalName = name, category = category)
                }
                dishDao.insertDishes(list)
            }
        }
    }
}
