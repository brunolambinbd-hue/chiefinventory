package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivitySignatureReportBinding

class SignatureReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignatureReportBinding
    private lateinit var adapter: SignatureReportAdapter

    private val viewModel: SignatureReportViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Rapport des Signatures"

        setupRecyclerView()

        viewModel.signatureStats.observe(this) { stats ->
            binding.tvSignaturesOk.text = getString(R.string.report_signatures_ok, stats.validCount)
            binding.tvSignaturesEmpty.text = getString(R.string.report_signatures_empty, stats.emptyCount)
            binding.tvSignaturesMissing.text = getString(R.string.report_signatures_missing, stats.missingCount)
        }

        viewModel.filteredItems.observe(this) { items ->
            adapter.submitList(items)
        }
    }

    private fun setupRecyclerView() {
        adapter = SignatureReportAdapter { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", item.id)
            startActivity(intent)
        }
        binding.rvSignatureReport.adapter = adapter
        binding.rvSignatureReport.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}