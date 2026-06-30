package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes ORDER BY createdAt DESC")
    fun getAllDishes(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE category = :category ORDER BY createdAt DESC")
    fun getDishesByCategory(category: String): Flow<List<Dish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<Dish>)

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun deleteDishById(id: Int)

    // History queries
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT 5")
    fun getRecentHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: HistoryEntry)

    @Query("DELETE FROM history_entries")
    suspend fun clearHistory()
}
