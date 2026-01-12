package com.example.parabdcollector.ui.actvity

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.parabdcollector.*
import com.example.parabdcollector.databinding.ActivityBatchPossessionBinding
import com.example.parabdcollector.ui.viewmodel.*

class BatchPossessionActivity : AppCompatActivity() {
    private lateinit var b: ActivityBatchPossessionBinding
    private val vm: BatchPossessionViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBatchPossessionBinding.inflate(layoutInflater); setContentView(b.root)
        setSupportActionBar(b.toolbar); supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.batch_update_title)
        setupClickListeners(); observeViewModel()
    }

    private fun setupClickListeners() {
        b.btnAnalyze.setOnClickListener {
            val name = b.etSeriesName.text.toString().trim()
            val start = b.etStartNumber.text.toString().toIntOrNull()
            val end = b.etEndNumber.text.toString().toIntOrNull()
            if (name.isEmpty() || start == null || end == null) {
                Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (start > end) {
                Toast.makeText(this, "Début > Fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            vm.analyzeSeries(name, start, end)
        }
        b.btnConfirmUpdate.setOnClickListener { vm.applyUpdate() }
    }

    private fun observeViewModel() {
        vm.analysisResult.observe(this) { r ->
            if (r != null) {
                b.cvSummary.isVisible = true
                b.tvBatchSummary.text = getString(R.string.batch_summary_format, r.foundCount, r.rangeSize)
                b.btnConfirmUpdate.isEnabled = r.foundCount > 0
                if (r.foundCount == 0) Toast.makeText(this, R.string.batch_no_items_found, Toast.LENGTH_SHORT).show()
            } else b.cvSummary.isVisible = false
        }
        vm.updateStatus.observe(this) { c ->
            if (c != null) {
                Toast.makeText(this, getString(R.string.batch_update_success_format, c), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
