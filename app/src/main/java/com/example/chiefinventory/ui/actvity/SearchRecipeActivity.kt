package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivitySearchRecipeBinding
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.ui.adapter.IngredientSelectAdapter
import com.example.chiefinventory.ui.adapter.RecipeAdapter
import com.example.chiefinventory.ui.viewmodel.SearchRecipeViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.google.android.material.chip.Chip

class SearchRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchRecipeBinding
    private lateinit var recipeAdapter: RecipeAdapter
    
    private var allRecipesList: List<com.example.chiefinventory.dao.RecipeWithIngredients> = emptyList()

    private val viewModel: SearchRecipeViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🔍 Que cuisiner (par mes Recettes) ?"

        setupRecipeRecyclerView()
        setupSearchFilter()
        observeViewModel()
    }

    private fun setupRecipeRecyclerView() {
        recipeAdapter = RecipeAdapter { recipe ->
            val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra(EditRecipeActivity.EXTRA_RECIPE_ID, recipe.id)
            }
            startActivity(intent)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = recipeAdapter
    }

    private fun setupSearchFilter() {
        binding.etSearchStock.hint = "Rechercher une recette ou un ingrédient..."
        binding.etSearchStock.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    private fun filterRecipes(query: String) {
        val cleanQuery = stripAccents(query)
        val filtered = if (cleanQuery.isBlank()) {
            emptyList()
        } else {
            allRecipesList.filter { item ->
                val titleMatch = stripAccents(item.recipe.title).contains(cleanQuery)
                val ingrMatch = item.ingredients.any { stripAccents(it.ingredientName).contains(cleanQuery) }
                titleMatch || ingrMatch
            }.map { it.recipe }
        }

        recipeAdapter.submitList(filtered)
        if (cleanQuery.isBlank()) {
            binding.tvNoResults.visibility = View.GONE
        } else {
            binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.recipesWithIngredients.observe(this) { recipesWithIngr ->
            allRecipesList = recipesWithIngr ?: emptyList()
            filterRecipes(binding.etSearchStock.text.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
