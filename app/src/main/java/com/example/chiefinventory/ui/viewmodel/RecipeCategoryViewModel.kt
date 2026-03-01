package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.repo.RecipeRepository

class RecipeCategoryViewModel(private val repository: RecipeRepository) : ViewModel() {
    val recipeCategories: LiveData<List<RecipeCategoryInfo>> = repository.getRecipeCategoriesWithCount()
}
