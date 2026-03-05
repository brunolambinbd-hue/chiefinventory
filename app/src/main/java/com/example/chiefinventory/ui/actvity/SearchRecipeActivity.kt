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
    private lateinit var ingredientAdapter: IngredientSelectAdapter
    
    private var allIngredientsList: List<Ingredient> = emptyList()

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
        supportActionBar?.title = "Que cuisiner ?"

        setupIngredientRecyclerView()
        setupRecipeRecyclerView()
        setupSearchFilter()
        observeViewModel()
        
        // On s'assure que la sélection est vide au démarrage
        viewModel.clearSelection()
    }

    private fun setupIngredientRecyclerView() {
        ingredientAdapter = IngredientSelectAdapter { ingredientName ->
            viewModel.toggleIngredient(ingredientName)
        }
        binding.rvAvailableIngredients.layoutManager = LinearLayoutManager(this)
        binding.rvAvailableIngredients.adapter = ingredientAdapter
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
        binding.etSearchStock.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterIngredients(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterIngredients(query: String) {
        val selected = viewModel.selectedIngredients.value ?: emptySet()
        
        // Vide par défaut : on ne montre les ingrédients du stock que si on cherche quelque chose
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            allIngredientsList
                .filter { it.name.contains(query, ignoreCase = true) }
                .map { IngredientSelectAdapter.IngredientItem(it.name, selected.contains(it.name)) }
        }
        ingredientAdapter.submitList(filtered)
    }

    private fun observeViewModel() {
        viewModel.allIngredients.observe(this) { ingredients ->
            allIngredientsList = ingredients ?: emptyList()
            filterIngredients(binding.etSearchStock.text.toString())
        }

        viewModel.selectedIngredients.observe(this) { selected ->
            binding.cgSelectedIngredients.removeAllViews()
            selected.forEach { name ->
                val chip = Chip(this).apply {
                    text = name
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        viewModel.toggleIngredient(name)
                    }
                }
                binding.cgSelectedIngredients.addView(chip)
            }
            filterIngredients(binding.etSearchStock.text.toString())
        }

        viewModel.searchResults.observe(this) { results ->
            val recipes = results?.map { it.first } ?: emptyList()
            recipeAdapter.submitList(recipes)
            
            val hasSelection = viewModel.selectedIngredients.value?.isNotEmpty() == true
            binding.tvNoResults.visibility = if (recipes.isEmpty() && hasSelection) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
