package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.databinding.ActivityRecipeListBinding
import com.chefinventory.app.ui.adapter.RecipeAdapter
import com.chefinventory.app.ui.viewmodel.RecipeViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory

class RecipeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListBinding
    private lateinit var adapter: RecipeAdapter

    private val viewModel: RecipeViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)

        if (categoryId != -1L && !categoryName.isNullOrBlank()) {
            supportActionBar?.title = categoryName
            viewModel.setCategoryId(categoryId)
        } else {
            supportActionBar?.title = "Toutes mes Recettes"
            viewModel.setCategoryId(-1L)
        }

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.fabAddRecipe.setOnClickListener {
            startActivity(Intent(this, EditRecipeActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearchRecipe.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter { recipe ->
            val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra(EditRecipeActivity.EXTRA_RECIPE_ID, recipe.id)
            }
            startActivity(intent)
        }
        binding.rvRecipeList.layoutManager = LinearLayoutManager(this)
        binding.rvRecipeList.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
        // Constantes conservées pour la compatibilité avec le reste du projet
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_NAME = "category_name"
    }
}
