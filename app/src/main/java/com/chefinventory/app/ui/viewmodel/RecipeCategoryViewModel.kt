package com.chefinventory.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.chefinventory.app.dao.RecipeCategoryInfo
import com.chefinventory.app.repo.RecipeRepository

class RecipeCategoryViewModel(private val repository: RecipeRepository) : ViewModel() {
    val recipeCategories: LiveData<List<RecipeCategoryInfo>> = repository.getRecipeCategoriesWithCount()
}
