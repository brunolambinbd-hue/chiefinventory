package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivitySearchBinding
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.ui.adapter.CollectionAdapter
import com.example.parabdcollector.ui.viewmodel.SearchResultState
import com.example.parabdcollector.ui.viewmodel.SearchViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import com.example.parabdcollector.utils.BitmapUtils
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.ImageCaptureUtil

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CollectionAdapter
    private lateinit var imageCaptureUtil: ImageCaptureUtil

    private var currentSearchDescription: String = ""
    private var searchImageBitmap: Bitmap? = null
    private var lastSearchWasSimple: Boolean = true
    private var lastSimpleQuery: String? = null
    private var lastAdvancedCriteria: SearchCriteria? = null

    private val viewModel: SearchViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_search_title)

        imageCaptureUtil = ImageCaptureUtil(this) { permanentUri ->
            if (permanentUri != null) {
                val bitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
                searchImageBitmap = bitmap
                binding.ivSearchThumbnail.setImageBitmap(bitmap)
                if (bitmap != null) {
                    binding.ivSearchThumbnail.visibility = View.VISIBLE
                    viewModel.calculateSignatureForPreview(bitmap)
                } else {
                    binding.ivSearchThumbnail.visibility = View.GONE
                    Toast.makeText(this, "Erreur de décodage de l'image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerView()
        setupCategorySpinners()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener { performSearch() }
        binding.btnSearchByImage.setOnClickListener { imageCaptureUtil.start() }
        binding.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }
        binding.btnRetrySearch.setOnClickListener { retryLastSearch() }
    }

    private fun observeViewModel() {
        viewModel.searchResultState.observe(this) { state ->
            // Hide views based on the new state
            binding.progressBar.isVisible = state is SearchResultState.Loading
            binding.resultsContainer.isVisible = state is SearchResultState.Success
            binding.errorContainer.isVisible = state is SearchResultState.Error
            // The search form is visible only if there is no other state being displayed
            binding.searchFormContainer.isVisible = state !is SearchResultState.Loading && state !is SearchResultState.Success && state !is SearchResultState.Error

            when (state) {
                is SearchResultState.Success -> {
                    adapter.submitList(state.results)
                    updateResultCount(state.results.size)
                }
                is SearchResultState.Error -> {
                    binding.tvErrorMessage.text = state.message
                }
                is SearchResultState.Loading -> {
                    // Nothing extra to do, progress bar visibility is handled above
                }
            }
        }

        viewModel.signaturePreview.observe(this) { preview ->
            if (preview.isNotBlank()) {
                binding.tvSearchThumbnailSignature.text = preview
                binding.tvSearchThumbnailSignature.visibility = View.VISIBLE
            } else {
                binding.tvSearchThumbnailSignature.visibility = View.GONE
            }
        }
    }

    private fun updateResultCount(count: Int) {
        binding.tvResultsCount.isVisible = true
        if (count == 0 && currentSearchDescription.isBlank()) {
             binding.tvResultsCount.text = getString(R.string.search_no_results)
        } else if (count == 0 && searchImageBitmap != null) {
            binding.tvResultsCount.text = getString(R.string.search_no_similar_results)
        } else if (currentSearchDescription.isNotBlank()) {
            binding.tvResultsCount.text = resources.getQuantityString(R.plurals.search_results_count_with_criteria, count, count, currentSearchDescription)
        } else {
            binding.tvResultsCount.text = resources.getQuantityString(R.plurals.search_results_count, count, count)
        }
    }

    private fun resetSearchState() {
        binding.etSearchSimple.setText("")
        binding.etSearchTitre.setText("")
        binding.etSearchEditor.setText("")
        searchImageBitmap = null
        binding.ivSearchThumbnail.visibility = View.GONE
        binding.tvSearchThumbnailSignature.visibility = View.GONE
        currentSearchDescription = ""
        lastSimpleQuery = null
        lastAdvancedCriteria = null
        viewModel.clearSearchResults()
        // Make the form visible again
        binding.searchFormContainer.isVisible = true
        binding.resultsContainer.isVisible = false
        binding.errorContainer.isVisible = false
        binding.progressBar.isVisible = false
    }

    private fun retryLastSearch() {
        if (lastSearchWasSimple && lastSimpleQuery != null) {
            viewModel.search(lastSimpleQuery!!)
        } else if (!lastSearchWasSimple && lastAdvancedCriteria != null) {
            viewModel.advancedSearch(lastAdvancedCriteria!!, searchImageBitmap)
        } else {
            resetSearchState()
        }
    }

    private fun toggleAdvancedSearch() {
        if (binding.advancedSearchContainer.isGone) {
            binding.advancedSearchContainer.isVisible = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
        } else {
            binding.advancedSearchContainer.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
        }
    }

    private fun setupCategorySpinners() {
        val superCategories = CategoryMapper.getSuperCategories()
        val superCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, superCategories)
        binding.etSearchSuperCategory.setAdapter(superCategoryAdapter)

        binding.categorySearchLayout.isEnabled = false

        binding.etSearchSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedSuperCategory = parent.getItemAtPosition(position) as String
            val categories = CategoryMapper.getCategoriesFor(selectedSuperCategory)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etSearchCategory.setAdapter(categoryAdapter)

            binding.categorySearchLayout.isEnabled = true
            binding.etSearchCategory.text = null
        }
    }

    private fun performSearch() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val simpleQuery = binding.etSearchSimple.text.toString().trim()
        if (simpleQuery.isNotBlank()) {
            currentSearchDescription = simpleQuery
            lastSearchWasSimple = true
            lastSimpleQuery = simpleQuery
            viewModel.search(simpleQuery)
        } else {
            val criteria = SearchCriteria(
                titre = binding.etSearchTitre.text.toString().trim().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditor.text.toString().trim().takeIf { it.isNotBlank() },
                annee = binding.etSearchYear.text.toString().trim().toIntOrNull(),
                mois = binding.etSearchedMonth.text.toString().trim().toIntOrNull(),
                superCategorie = binding.etSearchSuperCategory.text.toString().trim().takeIf { it.isNotBlank() },
                categorie = binding.etSearchCategory.text.toString().trim().takeIf { it.isNotBlank() },
                description = binding.etSearchDescription.text.toString().trim().takeIf { it.isNotBlank() },
                tirage = binding.etSearchTirage.text.toString().trim().takeIf { it.isNotBlank() },
                dimensions = binding.etSearchDimensions.text.toString().trim().takeIf { it.isNotBlank() }
            )
            lastAdvancedCriteria = criteria
            lastSearchWasSimple = false

            val descriptionParts = listOfNotNull(
                criteria.titre,
                criteria.editeur,
                criteria.annee?.toString(),
                criteria.superCategorie,
                criteria.categorie,
                if (searchImageBitmap != null) "Image" else null
            )
            currentSearchDescription = descriptionParts.joinToString(", ").ifBlank { "" }

            viewModel.advancedSearch(criteria, searchImageBitmap)
        }
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", searchResult.item.id)
            startActivity(intent)
        }
        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_refine_search -> {
                binding.searchFormContainer.isVisible = true
                binding.resultsContainer.isVisible = false
                if (binding.advancedSearchContainer.isGone) {
                    toggleAdvancedSearch()
                }
                return true
            }
            R.id.action_new_search -> {
                resetSearchState()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
