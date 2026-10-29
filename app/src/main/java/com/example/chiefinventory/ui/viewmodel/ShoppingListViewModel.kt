package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.Ingredient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShoppingListViewModel @JvmOverloads constructor(
    application: Application,
    private val ingredientDao: IngredientDao = AppDatabase.getDatabase(application).ingredientDao(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    val outOfStockIngredients: LiveData<List<Ingredient>> = ingredientDao.getOutOfStockIngredients()

    fun markItemBought(ingredient: Ingredient, isBought: Boolean): Job =
        viewModelScope.launch(ioDispatcher) {
            val newQty = calculateBoughtQuantity(ingredient.quantity, ingredient.packSize, isBought)
            ingredientDao.update(ingredient.copy(quantity = newQty))
        }

    companion object {
        fun calculateBoughtQuantity(currentQty: Double?, packSize: Double, isBought: Boolean): Double {
            val pack = if (packSize > 0.0) {
                packSize
            } else {
                1.0
            }

            val current = if (currentQty != null) {
                currentQty
            } else {
                0.0
            }

            if (isBought) {
                return current + pack
            } else {
                return 0.0
            }
        }
    }
}
