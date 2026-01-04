package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
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

        handleWindowInsets()

        imageCaptureUtil = ImageCaptureUtil(this) { permanentUri ->
            if (permanentUri == null) {
                return@ImageCaptureUtil
            }

            val bitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
            if (bitmap != null) {
                searchImageBitmap = bitmap
                binding.ivSearchImagePreview.setImageBitmap(bitmap)
                if (binding.advancedSearchFields.isGone) {
                    toggleAdvancedSearch()
                }
                binding.ivSearchImagePreview.visibility = View.VISIBLE
                viewModel.calculateSignatureForPreview(bitmap)
            } else {
                binding.ivSearchImagePreview.visibility = View.GONE
                Toast.makeText(this, "Erreur de décodage de l\'image", Toast.LENGTH_SHORT).show()
            }
            validateSearchButton()
        }

        setupRecyclerView()
        setupSpinners()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        validateSearchButton() // Initial check
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = insets.left, top = insets.top, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener { performSearch() }
        binding.ivSearchImagePreview.setOnClickListener { imageCaptureUtil.start() }
        binding.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }
        binding.btnRetry.setOnClickListener { retryLastSearch() }
        binding.btnSearchByImage.setOnClickListener { imageCaptureUtil.start() }
        binding.fabScrollToTop.setOnClickListener { 
            binding.searchScrollView.smoothScrollTo(0, 0) 
        }
    }

    private fun setupTextWatchers() {
        val afterTextChanged = { _: Editable? -> validateSearchButton() }

        binding.etSearchSimple.doAfterTextChanged(afterTextChanged)
        binding.etSearchTitle.doAfterTextChanged(afterTextChanged)
        binding.etSearchSuperCategory.doAfterTextChanged(afterTextChanged)
        binding.etSearchCategory.doAfterTextChanged(afterTextChanged)
        binding.etSearchEditor.doAfterTextChanged(afterTextChanged)
        binding.etSearchYear.doAfterTextChanged(afterTextChanged)
        binding.etSearchMonth.doAfterTextChanged(afterTextChanged)
        binding.etSearchDescription.doAfterTextChanged(afterTextChanged)
        binding.etSearchTirage.doAfterTextChanged(afterTextChanged)
        binding.etSearchDimensions.doAfterTextChanged(afterTextChanged)
        binding.etSearchStatus.doAfterTextChanged(afterTextChanged)
    }

    private fun validateSearchButton() {
        val isAdvancedVisible = binding.advancedSearchFields.isVisible
        var isSearchPossible = false

        if (isAdvancedVisible) {
            val advancedFields: List<EditText> = listOf(
                binding.etSearchTitle,
                binding.etSearchSuperCategory,
                binding.etSearchCategory,
                binding.etSearchEditor,
                binding.etSearchYear,
                binding.etSearchMonth,
                binding.etSearchDescription,
                binding.etSearchTirage,
                binding.etSearchDimensions
            )
            val isAnyAdvancedFieldFilled = advancedFields.any { !it.text.isNullOrBlank() }
            val isImageSelected = searchImageBitmap != null
            val statusOptions = resources.getStringArray(R.array.search_status_options)
            val isStatusSelected = !binding.etSearchStatus.text.isNullOrBlank() && binding.etSearchStatus.text.toString() != statusOptions.firstOrNull()
            isSearchPossible = isAnyAdvancedFieldFilled || isImageSelected || isStatusSelected
        } else {
            isSearchPossible = !binding.etSearchSimple.text.isNullOrBlank()
        }
        
        binding.btnSearch.isEnabled = isSearchPossible
    }

    private fun observeViewModel() {
        viewModel.searchResultState.observe(this) { state ->
            binding.progressBar.isVisible = state is SearchResultState.Loading
            binding.resultsListContainer.isVisible = state is SearchResultState.Success
            binding.errorContainer.isVisible = state is SearchResultState.Error
            binding.tvNoResults.isVisible = state is SearchResultState.Success && state.results.isEmpty() && state !is SearchResultState.Idle
            binding.fabScrollToTop.isVisible = state is SearchResultState.Success && state.results.isNotEmpty()

            if (state is SearchResultState.Success) {
                if (state.totalCount > state.results.size) {
                    Toast.makeText(this, "${state.totalCount} résultats trouvés. Seuls les 200 premiers sont affichés.", Toast.LENGTH_LONG).show()
                }
                adapter.submitList(state.results)
                updateResultSummary(state.totalCount) // Use totalCount for the summary
            } else if (state is SearchResultState.Idle) {
                adapter.submitList(emptyList())
                updateResultSummary(0)
            }
        }

        viewModel.signaturePreview.observe(this) { preview ->
            if (preview.isNotBlank()) {
                binding.tvSignaturePreview.text = preview
                binding.tvSignaturePreview.visibility = View.VISIBLE
            } else {
                binding.tvSignaturePreview.visibility = View.GONE
            }
        }
    }

    private fun updateResultSummary(count: Int) {
        if (count > 0) {
            binding.tvResultsSummary.text = resources.getQuantityString(R.plurals.search_results_count_with_criteria, count, count, currentSearchDescription)
            binding.tvResultsSummary.isVisible = true
        } else {
            binding.tvResultsSummary.isVisible = false
        }
    }

    private fun clearAdvancedSearchFields() {
        binding.etSearchTitle.setText("")
        val statusOptions = resources.getStringArray(R.array.search_status_options)
        binding.etSearchStatus.setText(statusOptions.firstOrNull() ?: "", false)
        binding.etSearchSuperCategory.setText("", false)
        binding.etSearchCategory.setText("", false)
        binding.etSearchEditor.setText("")
        binding.etSearchYear.setText("")
        binding.etSearchMonth.setText("")
        binding.etSearchDescription.setText("")
        binding.etSearchTirage.setText("")
        binding.etSearchDimensions.setText("")
        searchImageBitmap = null
        binding.ivSearchImagePreview.visibility = View.GONE
        binding.tvSignaturePreview.visibility = View.GONE
    }

    private fun resetSearchState() {
        // Reset to the default state: simple search enabled, advanced search hidden and cleared.
        binding.etSearchSimple.setText("")
        binding.etSearchSimple.isEnabled = true
        binding.etSearchSimple.alpha = 1.0f

        if (binding.advancedSearchFields.isVisible) {
            binding.advancedSearchFields.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
        }
        clearAdvancedSearchFields()
        
        currentSearchDescription = ""
        lastSimpleQuery = null
        lastAdvancedCriteria = null
        viewModel.clearSearchResults()
        validateSearchButton() // Re-validate after clearing
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
        val isOpening = binding.advancedSearchFields.isGone
        if (isOpening) {
            // Open Advanced Search: disable and clear simple search
            binding.advancedSearchFields.isVisible = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
            binding.etSearchSimple.setText("")
            binding.etSearchSimple.isEnabled = false
            binding.etSearchSimple.alpha = 0.5f
        } else {
            // Close Advanced Search: re-enable simple search and clear advanced fields
            binding.advancedSearchFields.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
            binding.etSearchSimple.isEnabled = true
            binding.etSearchSimple.alpha = 1.0f
            clearAdvancedSearchFields()
        }
        validateSearchButton()
    }

    private fun setupSpinners() {
        // Status Spinner
        val statusOptions = resources.getStringArray(R.array.search_status_options)
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusOptions)
        binding.etSearchStatus.setAdapter(statusAdapter)
        binding.etSearchStatus.setText(statusOptions.firstOrNull() ?: "", false) // Default to first item ("Tous")

        // Category Spinners
        val superCategories = CategoryMapper.getSuperCategories()
        val superCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, superCategories)
        binding.etSearchSuperCategory.setAdapter(superCategoryAdapter)

        binding.layoutSearchCategory.isEnabled = false

        binding.etSearchSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedSuperCategory = parent.getItemAtPosition(position) as String
            val categories = CategoryMapper.getCategoriesFor(selectedSuperCategory)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etSearchCategory.setAdapter(categoryAdapter)

            binding.layoutSearchCategory.isEnabled = true
            binding.etSearchCategory.setText("", false)
            validateSearchButton()
        }
    }

    private fun performSearch() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val isAdvancedSearch = binding.advancedSearchFields.isVisible

        if (isAdvancedSearch) {
            val statusOptions = resources.getStringArray(R.array.search_status_options)
            val selectedStatus = binding.etSearchStatus.text.toString()

            val isPossessed: Boolean? = when (selectedStatus) {
                statusOptions.getOrNull(1) -> true // "Possédés"
                statusOptions.getOrNull(2) -> false // "Recherchés"
                else -> null // "Tous"
            }

            val criteria = SearchCriteria(
                titre = binding.etSearchTitle.text.toString().trim().takeIf { it.isNotBlank() },
                superCategorie = binding.etSearchSuperCategory.text.toString().trim().takeIf { it.isNotBlank() },
                categorie = binding.etSearchCategory.text.toString().trim().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditor.text.toString().trim().takeIf { it.isNotBlank() },
                annee = binding.etSearchYear.text.toString().trim().toIntOrNull(),
                mois = binding.etSearchMonth.text.toString().trim().toIntOrNull(),
                description = binding.etSearchDescription.text.toString().trim().takeIf { it.isNotBlank() },
                tirage = binding.etSearchTirage.text.toString().trim().takeIf { it.isNotBlank() },
                dimensions = binding.etSearchDimensions.text.toString().trim().takeIf { it.isNotBlank() },
                isPossessed = isPossessed
            )
            lastAdvancedCriteria = criteria
            lastSearchWasSimple = false

            val descriptionParts = listOfNotNull(
                criteria.titre,
                selectedStatus,
                criteria.superCategorie,
                criteria.categorie,
                criteria.editeur,
                criteria.annee?.toString(),
                criteria.mois?.toString(),
                criteria.description,
                criteria.tirage,
                criteria.dimensions,
                if (searchImageBitmap != null) "Image" else null
            )
            currentSearchDescription = descriptionParts.joinToString(", ").ifBlank { "Recherche avancée" }

            viewModel.advancedSearch(criteria, searchImageBitmap)

        } else {
            val simpleQuery = binding.etSearchSimple.text.toString().trim()
            if (simpleQuery.isNotBlank()) {
                currentSearchDescription = simpleQuery
                lastSearchWasSimple = true
                lastSimpleQuery = simpleQuery
                viewModel.search(simpleQuery)
            }
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
                binding.resultsListContainer.visibility = View.GONE
                binding.errorContainer.visibility = View.GONE
                binding.tvNoResults.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
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
