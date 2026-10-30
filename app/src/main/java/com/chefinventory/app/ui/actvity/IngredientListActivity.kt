package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.databinding.ActivityIngredientListBinding
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.ui.adapter.IngredientAdapter
import com.chefinventory.app.ui.viewmodel.IngredientViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIngredientListBinding
    private lateinit var adapter: IngredientAdapter

    private var allIngredientsList: List<Ingredient> = emptyList()
    private var locationNamesMap: Map<Long, String> = emptyMap()
    private var selectedLocationFilter: String = FILTER_ALL

    private val viewModel: IngredientViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngredientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mon Stock d'Ingrédients"

        viewModel.setLocation(-1L)

        setupRecyclerView()
        setupSearch()
        setupLocationTabs()
        observeViewModel()

        lifecycleScope.launch(Dispatchers.IO) {
            val db = com.chefinventory.app.data.AppDatabase.getDatabase(applicationContext)
            val locations = db.locationDao().getAllSync()
            locationNamesMap = locations.associate { it.id to it.name }
            withContext(Dispatchers.Main) {
                adapter.setLocationNamesMap(locationNamesMap)
                applyFilters()
            }
        }

        binding.fabAddIngredient.setOnClickListener {
            val intent = Intent(this, EditIngredientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLocationTabs() {
        binding.tabLocationFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedLocationFilter = when (tab?.position) {
                    1 -> FILTER_FRIGO
                    2 -> FILTER_PLACARD
                    3 -> FILTER_SURGELE
                    4 -> FILTER_CAVE
                    else -> FILTER_ALL
                }
                applyFilters()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearchIngredient.doAfterTextChanged { _ ->
            applyFilters()
        }
    }

    private fun setupRecyclerView() {
        val openEdit = { ingredient: Ingredient ->
            val intent = Intent(this, EditIngredientActivity::class.java).apply {
                putExtra(EditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.id)
            }
            startActivity(intent)
        }

        adapter = IngredientAdapter(
            onClick = openEdit,
            onEditClick = openEdit
        )
        binding.rvIngredientList.layoutManager = LinearLayoutManager(this)
        binding.rvIngredientList.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.ingredients.observe(this) { ingredients ->
            allIngredientsList = ingredients ?: emptyList()
            applyFilters()
        }
    }

    private fun updateTabCounts() {
        val totalCount = allIngredientsList.size
        var frigoCount = 0
        var placardCount = 0
        var surgelateurCount = 0
        var caveCount = 0

        for (ingredient in allIngredientsList) {
            val locName = IngredientAdapter.resolveLocationName(
                ingredient.locationId,
                locationNamesMap,
                ingredient.category
            )

            when {
                matchesLocationFilter(locName, FILTER_FRIGO) -> frigoCount++
                matchesLocationFilter(locName, FILTER_SURGELE) -> surgelateurCount++
                matchesLocationFilter(locName, FILTER_CAVE) -> caveCount++
                else -> placardCount++
            }
        }

        binding.tabLocationFilter.getTabAt(0)?.text = "TOUT ($totalCount)"
        binding.tabLocationFilter.getTabAt(1)?.text = "❄️ Frigo ($frigoCount)"
        binding.tabLocationFilter.getTabAt(2)?.text = "🧺 Placard ($placardCount)"
        binding.tabLocationFilter.getTabAt(3)?.text = "🧊 Surgélateur ($surgelateurCount)"
        binding.tabLocationFilter.getTabAt(4)?.text = "🍷 Cave ($caveCount)"
    }

    private fun applyFilters() {
        updateTabCounts()

        val cleanQuery = stripAccents(binding.etSearchIngredient.text?.toString())

        val filtered = allIngredientsList.filter { ingredient ->
            val cleanName = stripAccents(ingredient.name)
            val cleanCategory = stripAccents(ingredient.category)
            val cleanDescription = stripAccents(ingredient.description)

            val nameMatches = cleanName.contains(cleanQuery)
            val categoryMatches = cleanCategory.contains(cleanQuery)
            val descriptionMatches = cleanDescription.contains(cleanQuery)
            val matchesText = cleanQuery.isBlank() || nameMatches || categoryMatches || descriptionMatches

            val locName = IngredientAdapter.resolveLocationName(
                ingredient.locationId,
                locationNamesMap,
                ingredient.category
            )

            val matchesLocation = matchesLocationFilter(locName, selectedLocationFilter)

            matchesText && matchesLocation
        }

        val totalCount = filtered.size
        if (totalCount == 0) {
            binding.tvIngredientCountHeader.text = "Aucun ingrédient trouvé"
        } else {
            binding.tvIngredientCountHeader.text = "📊 $totalCount ingrédient(s) affiché(s)"
        }

        adapter.submitList(filtered)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_LOCATION_ID: String = "location_id"
        const val EXTRA_LOCATION_NAME: String = "location_name"
        const val EXTRA_INGREDIENT_ID: String = "ingredient_id"

        const val FILTER_ALL: String = "TOUT"
        const val FILTER_FRIGO: String = "Frigo"
        const val FILTER_PLACARD: String = "Placard"
        const val FILTER_SURGELE: String = "Surgélateur"
        const val FILTER_CAVE: String = "Cave"

        fun stripAccents(text: String?): String {
            if (text.isNullOrBlank()) return ""
            val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
            return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                .trim()
        }

        fun isFrigoLocation(locNameLower: String): Boolean {
            return "frigo" in locNameLower || "fridge" in locNameLower || "réfrigérateur" in locNameLower || "refrigerateur" in locNameLower
        }

        fun isSurgeleLocation(locNameLower: String): Boolean {
            return "surgélateur" in locNameLower || "surgelateur" in locNameLower || "congélateur" in locNameLower || "congelateur" in locNameLower || "freezer" in locNameLower || "surgelés" in locNameLower || "surgeles" in locNameLower
        }

        fun isCaveLocation(locNameLower: String): Boolean {
            return "cave" in locNameLower || "cellar" in locNameLower || "boisson" in locNameLower || "boissons" in locNameLower || "drink" in locNameLower || "vin" in locNameLower || "bière" in locNameLower || "biere" in locNameLower || "bieres" in locNameLower || "légume" in locNameLower || "legume" in locNameLower || "vegetable" in locNameLower || "potato" in locNameLower || "pomme de terre" in locNameLower || "oignon" in locNameLower || "onion" in locNameLower || "ail" in locNameLower || "garlic" in locNameLower || "échalote" in locNameLower || "echalote" in locNameLower
        }

        fun matchesLocationFilter(locName: String, filter: String): Boolean {
            val lower = locName.lowercase()
            return when (filter) {
                FILTER_FRIGO -> isFrigoLocation(lower)
                FILTER_SURGELE -> isSurgeleLocation(lower)
                FILTER_CAVE -> isCaveLocation(lower)
                FILTER_PLACARD -> !(isFrigoLocation(lower) || isSurgeleLocation(lower) || isCaveLocation(lower))
                else -> true
            }
        }
    }
}
