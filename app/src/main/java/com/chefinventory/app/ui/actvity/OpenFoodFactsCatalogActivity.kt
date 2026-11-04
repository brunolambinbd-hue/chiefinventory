package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.chefinventory.app.R
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.ActivityOpenFoodFactsCatalogBinding
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.ui.adapter.OpenFoodFactsCatalogAdapter
import com.chefinventory.app.ui.viewmodel.OpenFoodFactsCatalogViewModel

/**
 * Écran d'IHM de consultation et recherche dans le Catalogue Open Food Facts (Niveau 2).
 */
class OpenFoodFactsCatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenFoodFactsCatalogBinding
    private lateinit var viewModel: OpenFoodFactsCatalogViewModel
    private lateinit var adapter: OpenFoodFactsCatalogAdapter
    private var selectedLetterTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenFoodFactsCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        val offRepository = OpenFoodFactsRepository(database.openFoodFactsDao())
        val ingredientRepository = IngredientRepository(database.ingredientDao())
        viewModel = OpenFoodFactsCatalogViewModel(offRepository, ingredientRepository)

        setupToolbar()
        setupRecyclerView()
        setupCategoryChips()
        setupAlphabetSidebar()
        setupSearchView()
        observeViewModel()
        viewModel.loadInitialData(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = OpenFoodFactsCatalogAdapter(
            onItemClick = { taxonomyItem ->
                val intent = Intent(this, OpenFoodFactsProductDetailActivity::class.java).apply {
                    putExtra(OpenFoodFactsProductDetailActivity.EXTRA_TAXONOMY_ID, taxonomyItem.id)
                    putExtra(OpenFoodFactsProductDetailActivity.EXTRA_CLEAN_NAME, taxonomyItem.cleanName)
                    putExtra(OpenFoodFactsProductDetailActivity.EXTRA_CATEGORY, taxonomyItem.category)
                }
                startActivity(intent)
            },
            onAddToPantryClick = { taxonomyItem ->
                viewModel.importToPantry(taxonomyItem)
            }
        )
        binding.recyclerViewCatalog.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCatalog.adapter = adapter
    }

    private fun setupCategoryChips() {
        val chipMap = mapOf(
            binding.chipCatAll to 0,
            binding.chipCatLegumes to 1,
            binding.chipCatCremerie to 2,
            binding.chipCatHuiles to 3,
            binding.chipCatViandes to 4,
            binding.chipCatVolailles to 5,
            binding.chipCatPoissons to 6,
            binding.chipCatEpices to 7,
            binding.chipCatCereales to 8,
            binding.chipCatBoissons to 9,
            binding.chipCatEpicerie to 10
        )

        for ((chip, code) in chipMap) {
            chip.setOnClickListener {
                viewModel.filterByCategoryCode(code)
            }
        }
    }

    private fun setupAlphabetSidebar() {
        binding.layoutAlphabetSidebar.removeAllViews()

        val activeColor = ContextCompat.getColor(this, R.color.colorPrimary)
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)

        val letters = listOf("★") + ('A'..'Z').map { it.toString() }
        for (letter in letters) {
            val tv = TextView(this).apply {
                text = letter
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(4, 2, 4, 2)
                setTextColor(if (letter == "★") activeColor else inactiveColor)
                setOnClickListener {
                    selectedLetterTextView?.setTextColor(inactiveColor)
                    selectedLetterTextView = this
                    this.setTextColor(activeColor)

                    val filter = if (letter == "★") null else letter
                    viewModel.filterByLetter(filter)
                }
            }
            if (letter == "★") {
                selectedLetterTextView = tv
            }
            binding.layoutAlphabetSidebar.addView(tv)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.taxonomyList.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.importStatus.observe(this) { status ->
            if (!status.isNull_Or_Blank()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                viewModel.clearImportStatus()
            }
        }
    }

    private fun String?.isNull_Or_Blank(): Boolean {
        return this == null || this.trim().isEmpty() || this == "null"
    }
}
