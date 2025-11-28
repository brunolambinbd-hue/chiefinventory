package com.example.parabdcollector.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivitySearchBinding
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.utils.BitmapUtils
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.ImageCaptureUtil

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CollectionAdapter
    private lateinit var imageCaptureUtil: ImageCaptureUtil

    private var currentSearchDescription: String = ""
    private var searchImageBitmap: Bitmap? = null
    private var searchHasBeenPerformed: Boolean = false

    private val viewModel: SearchViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_search_title)

        imageCaptureUtil = ImageCaptureUtil(this) { croppedUri ->
            val bitmap = BitmapUtils.getBitmapFromUri(this, croppedUri)
            
            searchImageBitmap = bitmap
            binding.ivSearchThumbnail.setImageBitmap(bitmap)
            binding.ivSearchThumbnail.visibility = View.VISIBLE
            viewModel.calculateSignatureForPreview(bitmap)
        }

        setupRecyclerView()
        setupCategorySpinners()

        binding.btnSearch.setOnClickListener { performSearch() }
        binding.btnSearchByImage.setOnClickListener { imageCaptureUtil.start() }

        binding.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }

        viewModel.searchResults.observe(this) { results ->
            adapter.submitList(results)
            if (!searchHasBeenPerformed) {
                binding.tvResultsCount.isVisible = false
                return@observe
            }

            val count = results.size
            binding.tvResultsCount.isVisible = true

            if (count == 0 && searchImageBitmap != null) {
                binding.tvResultsCount.text = getString(R.string.search_no_similar_results)
            } else if (currentSearchDescription.isNotBlank()) {
                binding.tvResultsCount.text = resources.getQuantityString(R.plurals.search_results_count_with_criteria, count, count, currentSearchDescription)
            } else {
                binding.tvResultsCount.text = resources.getQuantityString(R.plurals.search_results_count, count, count)
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

    private fun resetSearchState() {
        binding.etSearchSimple.setText("")
        binding.etSearchTitre.setText("")
        binding.etSearchEditor.setText("")
        // ... (le reste des resets)
        searchImageBitmap = null
        binding.ivSearchThumbnail.visibility = View.GONE
        binding.tvSearchThumbnailSignature.visibility = View.GONE
        currentSearchDescription = ""
        searchHasBeenPerformed = false
        viewModel.clearSearchResults()
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
        searchHasBeenPerformed = true

        val simpleQuery = binding.etSearchSimple.text.toString()
        if (simpleQuery.isNotBlank()) {
            currentSearchDescription = simpleQuery
            viewModel.search(simpleQuery)
        } else {
            val criteria = SearchCriteria(
                titre = binding.etSearchTitre.text.toString().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditor.text.toString().takeIf { it.isNotBlank() },
                annee = binding.etSearchYear.text.toString().toIntOrNull(),
                mois = binding.etSearchedMonth.text.toString().toIntOrNull(),
                superCategorie = binding.etSearchSuperCategory.text.toString().takeIf { it.isNotBlank() },
                categorie = binding.etSearchCategory.text.toString().takeIf { it.isNotBlank() },
                description = binding.etSearchDescription.text.toString().takeIf { it.isNotBlank() },
                tirage = binding.etSearchTirage.text.toString().takeIf { it.isNotBlank() },
                dimensions = binding.etSearchDimensions.text.toString().takeIf { it.isNotBlank() }
            )

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
