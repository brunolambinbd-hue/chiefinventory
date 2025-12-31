package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            if (permanentUri != null) {
                val bitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
                searchImageBitmap = bitmap
                binding.ivSearchImagePreview.setImageBitmap(bitmap)
                if (bitmap != null) {
                    binding.advancedSearchFields.isVisible = true
                    binding.ivSearchImagePreview.visibility = View.VISIBLE
                    viewModel.calculateSignatureForPreview(bitmap)
                } else {
                    binding.ivSearchImagePreview.visibility = View.GONE
                    Toast.makeText(this, "Erreur de décodage de l\'image", Toast.LENGTH_SHORT).show()
                }
                validateSearchButton()
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l\'image", Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerView()
        setupCategorySpinners()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        validateSearchButton() // Initial check
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply insets as padding to the root view
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
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateSearchButton()
            }
        }

        binding.etSearchSimple.addTextChangedListener(textWatcher)
        binding.etSearchTitle.addTextChangedListener(textWatcher)
        binding.etSearchSuperCategory.addTextChangedListener(textWatcher)
        binding.etSearchCategory.addTextChangedListener(textWatcher)
        binding.etSearchEditor.addTextChangedListener(textWatcher)
        binding.etSearchYear.addTextChangedListener(textWatcher)
        binding.etSearchMonth.addTextChangedListener(textWatcher)
        binding.etSearchDescription.addTextChangedListener(textWatcher)
        binding.etSearchTirage.addTextChangedListener(textWatcher)
        binding.etSearchDimensions.addTextChangedListener(textWatcher)
    }

    private fun validateSearchButton() {
        var isAnyFieldFilled = false
        val fields: List<EditText> = listOf(
            binding.etSearchSimple,
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

        for (field in fields) {
            if (field.text.toString().isNotBlank()) {
                isAnyFieldFilled = true
                break
            }
        }

        val isImageSelected = searchImageBitmap != null
        binding.btnSearch.isEnabled = isAnyFieldFilled || isImageSelected
    }

    private fun observeViewModel() {
        viewModel.searchResultState.observe(this) { state ->
            binding.progressBar.isVisible = state is SearchResultState.Loading
            binding.resultsListContainer.isVisible = state is SearchResultState.Success
            binding.errorContainer.isVisible = state is SearchResultState.Error
            binding.tvNoResults.isVisible = state is SearchResultState.Success && state.results.isEmpty()
            binding.fabScrollToTop.isVisible = state is SearchResultState.Success && state.results.isNotEmpty()

            if (state is SearchResultState.Success) {
                adapter.submitList(state.results)
                updateResultSummary(state.results.size)
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

    private fun resetSearchState() {
        binding.etSearchSimple.setText("")
        binding.etSearchTitle.setText("")
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
        if (binding.advancedSearchFields.isGone) {
            binding.advancedSearchFields.isVisible = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
        } else {
            binding.advancedSearchFields.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
        }
        validateSearchButton()
    }

    private fun setupCategorySpinners() {
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

        val simpleQuery = binding.etSearchSimple.text.toString().trim()
        if (simpleQuery.isNotBlank()) {
            currentSearchDescription = simpleQuery
            lastSearchWasSimple = true
            lastSimpleQuery = simpleQuery
            viewModel.search(simpleQuery)
            return
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
            dimensions = binding.etSearchDimensions.text.toString().trim().takeIf { it.isNotBlank() }
        )
        lastAdvancedCriteria = criteria
        lastSearchWasSimple = false

        val descriptionParts = listOfNotNull(
            criteria.titre,
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
        
        if (binding.advancedSearchFields.isVisible) {
            toggleAdvancedSearch()
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
