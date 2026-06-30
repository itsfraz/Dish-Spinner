package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Dish
import com.example.data.DishDatabase
import com.example.data.DishRepository
import com.example.data.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DishRepository

    private val _selectedCategory = MutableStateFlow("Global Favorites")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _allDishes = MutableStateFlow<List<Dish>>(emptyList())
    val allDishes: StateFlow<List<Dish>> = _allDishes.asStateFlow()

    val recentHistory: StateFlow<List<HistoryEntry>>

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var dishesJob: kotlinx.coroutines.Job? = null

    init {
        val dishDao = DishDatabase.getDatabase(application).dishDao()
        repository = DishRepository(dishDao)
        recentHistory = repository.recentHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        // Pre-populate database with default appetizing dishes on first launch
        viewModelScope.launch {
            repository.prepopulateDefaultDishes()
            selectCategory("Global Favorites")
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        dishesJob?.cancel()
        dishesJob = viewModelScope.launch {
            repository.getDishesByCategory(category).collect { dishes ->
                _allDishes.value = dishes
            }
        }
    }

    fun addDish(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _errorMessage.value = "Dish name cannot be empty."
            return
        }
        if (trimmed.length > 50) {
            _errorMessage.value = "Dish name must be 50 characters or less."
            return
        }
        
        val count = allDishes.value.size
        if (count >= 12) {
            _errorMessage.value = "Maximum of 12 dishes reached to keep the wheel readable."
            return
        }

        _errorMessage.value = null
        viewModelScope.launch {
            repository.insertDish(Dish(name = trimmed, category = _selectedCategory.value))
        }
    }

    fun deleteDish(dish: Dish) {
        val count = allDishes.value.size
        if (count <= 2) {
            _errorMessage.value = "At least 2 dishes are required to spin."
            return
        }
        _errorMessage.value = null
        viewModelScope.launch {
            repository.deleteDish(dish)
        }
    }

    fun updateDish(dish: Dish, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            _errorMessage.value = "Dish name cannot be empty."
            return
        }
        if (trimmed.length > 50) {
            _errorMessage.value = "Dish name must be 50 characters or less."
            return
        }
        _errorMessage.value = null
        viewModelScope.launch {
            repository.insertDish(dish.copy(name = trimmed))
        }
    }

    fun resetDishToDefault(dish: Dish) {
        _errorMessage.value = null
        viewModelScope.launch {
            repository.insertDish(dish.copy(name = dish.originalName))
        }
    }

    fun addWinningDishToHistory(dishName: String) {
        viewModelScope.launch {
            repository.insertHistoryEntry(HistoryEntry(dishName = dishName))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
