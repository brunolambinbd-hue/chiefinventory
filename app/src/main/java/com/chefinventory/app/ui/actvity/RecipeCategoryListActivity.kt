package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.databinding.ActivityRecipeCategoryListBinding
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.ui.adapter.RecipeCategoryAdapter
import com.chefinventory.app.ui.viewmodel.RecipeCategoryViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory

class RecipeCategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeCategoryListBinding
    private lateinit var adapter: RecipeCategoryAdapter

    private val viewModel: RecipeCategoryViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Type de Recettes"

        setupRecyclerView()
        observeViewModel()

        binding.fabManageCategories.setOnClickListener {
            val intent = Intent(this, LocationManagementActivity::class.java).apply {
                putExtra(LocationManagementActivity.EXTRA_LOCATION_TYPE, LocationType.RECIPE.ordinal)
            }
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipeCategoryAdapter { categoryInfo ->
            val intent = Intent(this, RecipeListActivity::class.java).apply {
                putExtra(RecipeListActivity.EXTRA_CATEGORY_ID, categoryInfo.categoryId)
                putExtra(RecipeListActivity.EXTRA_CATEGORY_NAME, categoryInfo.categoryName)
            }
            startActivity(intent)
        }
        binding.rvRecipeCategories.layoutManager = LinearLayoutManager(this)
        binding.rvRecipeCategories.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.recipeCategories.observe(this) { categories ->
            val isEmpty = categories.isNullOrEmpty()
            binding.rvRecipeCategories.isVisible = !isEmpty
            binding.tvEmptyCategories.isVisible = isEmpty

            adapter.submitList(categories)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
