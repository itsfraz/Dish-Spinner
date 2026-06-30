package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val originalName: String = name,
    val category: String = "Global Favorites",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
