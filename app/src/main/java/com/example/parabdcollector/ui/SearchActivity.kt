@file:Suppress("UnusedImport", "UnusedImport")

package com.example.parabdcollector.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.parabdcollector.utils.CategoryMapper

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CollectionAdapter // On utilise le bon adaptateur détaillé

    private val viewModel: SearchViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_search_title)

        setupRecyclerView()
        setupCategorySpinners()

        binding.btnSearch.setOnClickListener { performSearch() }

        binding.tvToggleAdvancedSearch.setOnClickListener {
            if (binding.advancedSearchContainer.isGone) {
                binding.advancedSearchContainer.isVisible = true
                binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
                // On vide la recherche simple quand on ouvre la recherche avancée.
                binding.etSearchSimple.setText("")
            } else {
                binding.advancedSearchContainer.isGone = true
                binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
            }
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
        // On cache le clavier
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val simpleQuery = binding.etSearchSimple.text.toString()

        if (simpleQuery.isNotBlank()) {
            viewModel.search(simpleQuery).observe(this) { results ->
                adapter.submitList(results.map { SearchResultItem(it) })
            }
        } else {
            val criteria = SearchCriteria(
                titre = binding.etSearchTitre.text.toString().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditor.text.toString().takeIf { it.isNotBlank() },
                annee = binding.etSearchYear.text.toString().toIntOrNull(),
                mois = binding.etSearchedMonth.text.toString().toIntOrNull(),
                superCategorie = binding.etSearchSuperCategory.text.toString().takeIf { it.isNotBlank() },
                categorie = binding.etSearchCategory.text.toString().takeIf { it.isNotBlank() },
                description = binding.etSearchDescription.text.toString().takeIf { it.isNotBlank() }
            )

            viewModel.advancedSearch(criteria).observe(this) { results ->
                adapter.submitList(results.map { SearchResultItem(it) })
            }
        }

        // On referme la recherche avancée pour donner de la place aux résultats.
        if (binding.advancedSearchContainer.isVisible) {
            binding.advancedSearchContainer.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}