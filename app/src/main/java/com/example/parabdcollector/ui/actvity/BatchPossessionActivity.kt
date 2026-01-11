package com.example.parabdcollector.ui.actvity

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityBatchPossessionBinding
import com.example.parabdcollector.ui.model.DisplayLocation
import com.example.parabdcollector.ui.viewmodel.BatchPossessionViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

class BatchPossessionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBatchPossessionBinding
    private var displayLocs = emptyList<DisplayLocation>()

    private val viewModel: BatchPossessionViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatchPossessionBinding.inflate(layoutInflater); setContentView(binding.root)
        setSupportActionBar(binding.toolbar); supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.batch_update_title)
        setupLocationDropdown()
        setupClickListeners(); observeViewModel()
    }

    private fun setupLocationDropdown() {
        // Force l'affichage immédiat lors du clic
        binding.etLocation.threshold = 0
        binding.etLocation.setOnClickListener { binding.etLocation.showDropDown() }

        viewModel.displayLocations.observe(this) { locs ->
            displayLocs = locs
            val names = locs.map { "    ".repeat(it.depth) + it.location.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
            binding.etLocation.setAdapter(adapter)
        }

        binding.etLocation.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedLocationId = displayLocs.getOrNull(position)?.location?.id
        }
    }

    private fun setupClickListeners() {
        binding.btnAnalyze.setOnClickListener {
            val seriesName = binding.etSeriesName.text.toString().trim()
            val startNum = binding.etStartNumber.text.toString().toIntOrNull()
            val endNum = binding.etEndNumber.text.toString().toIntOrNull()
            if (seriesName.isEmpty() || startNum == null || endNum == null) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startNum > endNum) {
                Toast.makeText(this, "Le numéro de début doit être inférieur au numéro de fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            viewModel.analyzeSeries(seriesName, startNum, endNum)
        }
        binding.btnConfirmUpdate.setOnClickListener { viewModel.applyUpdate() }
    }

    private fun observeViewModel() {
        viewModel.analysisResult.observe(this) { res ->
            if (res != null) {
                binding.cvSummary.isVisible = true
                binding.tvBatchSummary.text = getString(R.string.batch_summary_format, res.foundCount, res.rangeSize)
                binding.btnConfirmUpdate.isEnabled = res.foundCount > 0
                if (res.foundCount == 0) Toast.makeText(this, R.string.batch_no_items_found, Toast.LENGTH_SHORT).show()
            } else binding.cvSummary.isVisible = false
        }
        viewModel.updateStatus.observe(this) { count ->
            if (count != null) {
                Toast.makeText(this, getString(R.string.batch_update_success_format, count), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
