package com.example.parabdcollector.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityLocationManagementBinding

class LocationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationManagementBinding
    private lateinit var locationAdapter: LocationAdapter

    private val viewModel: LocationViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.locations_management_title)

        setupRecyclerView()

        viewModel.allLocations.observe(this) {
            locationAdapter.submitList(it)
        }
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter {
            // Gérer le clic sur un emplacement (pour l'édition, etc.)
        }
        binding.rvLocations.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(this@LocationManagementActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
