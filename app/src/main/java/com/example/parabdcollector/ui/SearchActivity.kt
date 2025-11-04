package com.example.parabdcollector.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CollectionAdapter

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
        supportActionBar?.title = "Rechercher un objet"

        setupRecyclerView()

        binding.btnSearch.setOnClickListener { performSearch() }

        binding.tvToggleAdvancedSearch.setOnClickListener {
            if (binding.advancedSearchContainer.visibility == View.GONE) {
                binding.advancedSearchContainer.visibility = View.VISIBLE
                binding.tvToggleAdvancedSearch.text = "Masquer la recherche avancée"
            } else {
                binding.advancedSearchContainer.visibility = View.GONE
                binding.tvToggleAdvancedSearch.text = "Recherche avancée"
            }
        }
    }

    private fun performSearch() {
        // On cache le clavier
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val simpleQuery = binding.etSearchSimple.text.toString()

        if (simpleQuery.isNotBlank()) {
            viewModel.search(simpleQuery).observe(this) { results ->
                adapter.submitList(results)
            }
        } else {
            val criteria = SearchCriteria(
                titre = binding.etSearchTitre.text.toString().takeIf { it.isNotBlank() },
                univers = binding.etSearchUnivers.text.toString().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditeur.text.toString().takeIf { it.isNotBlank() },
                annee = binding.etSearchAnnee.text.toString().toIntOrNull(),
                categorie = binding.etSearchCategorie.text.toString().takeIf { it.isNotBlank() }
            )

            viewModel.advancedSearch(criteria).observe(this) { results ->
                adapter.submitList(results)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", item.id)
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