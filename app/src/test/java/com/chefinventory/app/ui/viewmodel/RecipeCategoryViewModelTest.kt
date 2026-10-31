package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.dao.RecipeCategoryInfo
import com.chefinventory.app.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RecipeCategoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRecipeRepo: RecipeRepository

    @Before
    fun setUp() {
        mockRecipeRepo = mock()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipeCategoryViewModel_ExposesLiveData() {
        val catList = listOf(RecipeCategoryInfo(categoryId = 10L, categoryName = "Dessert", recipeCount = 5))
        whenever(mockRecipeRepo.getRecipeCategoriesWithCount()).thenReturn(MutableLiveData(catList))

        val viewModel = RecipeCategoryViewModel(mockRecipeRepo)
        assertThat(viewModel.recipeCategories.value).isEqualTo(catList)
    }
}
