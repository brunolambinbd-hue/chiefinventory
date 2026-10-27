package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val ingredientDao = db.ingredientDao()

    val outOfStockIngredients: LiveData<List<Ingredient>> = ingredientDao.getOutOfStockIngredients()

    fun markItemBought(ingredient: Ingredient, isBought: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val pack = if (ingredient.packSize > 0.0) ingredient.packSize else 1.0
            val current = ingredient.quantity ?: 0.0
            val newQty = if (isBought) (current + pack) else 0.0
            ingredientDao.update(ingredient.copy(quantity = newQty))
        }
    }
}
