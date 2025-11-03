package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
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

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchAndHideKeyboard(v)
                return@setOnEditorActionListener true
            }
            false
        }

        binding.etSearch.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearchAndHideKeyboard(v)
                return@setOnKeyListener true
            }
            false
        }

        // On demande le focus et on ouvre le clavier automatiquement
        binding.etSearch.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun performSearchAndHideKeyboard(view: View) {
        performSearch()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString()
        if (query.isNotBlank()) {
            viewModel.search(query).observe(this) { results ->
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