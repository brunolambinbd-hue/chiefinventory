package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityRecipeListBinding
import com.example.chiefinventory.ui.adapter.RecipeAdapter
import com.example.chiefinventory.ui.viewmodel.RecipeViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory

class RecipeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListBinding
    private lateinit var adapter: RecipeAdapter

    private val viewModel: RecipeViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // On affiche toujours le même titre pour cette page globale
        supportActionBar?.title = "Toutes mes Recettes"

        // On force le chargement de TOUTES les recettes au démarrage
        viewModel.setCategoryId(-1L)

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
