package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityIngredientListBinding
import com.example.chiefinventory.ui.adapter.IngredientAdapter
import com.example.chiefinventory.ui.viewmodel.IngredientViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory

class IngredientListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIngredientListBinding
    private lateinit var adapter: IngredientAdapter
    private var locationId: Long = -1L

    private val viewModel: IngredientViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngredientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Ingrédients"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = locationName

        // Initialiser le ViewModel avec l'emplacement actuel
        viewModel.setLocation(locationId)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.fabAddIngredient.setOnClickListener {
            val intent = Intent(this, EditIngredientActivity::class.java).apply {
                putExtra(EditIngredientActivity.EXTRA_LOCATION_ID, locationId)
            }
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.etSearchIngredient.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupRecyclerView() {
        adapter = IngredientAdapter { ingredient ->
            val intent = Intent(this, EditIngredientActivity::class.java).apply {
                putExtra(EditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.id)
                putExtra(EditIngredientActivity.EXTRA_LOCATION_ID, locationId)
            }
            startActivity(intent)
        }
        binding.rvIngredientList.layoutManager = LinearLayoutManager(this)
        binding.rvIngredientList.adapter = adapter
    }

    private fun observeViewModel() {
        // Utiliser la nouvelle propriété 'ingredients' qui gère le filtrage/recherche
        viewModel.ingredients.observe(this) { ingredients ->
            adapter.submitList(ingredients)
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
        const val EXTRA_LOCATION_ID = "location_id"
        const val EXTRA_LOCATION_NAME = "location_name"
    }
}
