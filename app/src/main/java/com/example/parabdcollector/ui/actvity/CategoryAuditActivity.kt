package com.example.parabdcollector.ui.actvity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityCategoryAuditBinding
import com.example.parabdcollector.ui.viewmodel.CategoryAuditViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

class CategoryAuditActivity : AppCompatActivity() {
    private lateinit var b: ActivityCategoryAuditBinding
    private val vm: CategoryAuditViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCategoryAuditBinding.inflate(layoutInflater); setContentView(b.root)
        setSupportActionBar(b.toolbar); supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.category_audit_title)
        setupClickListeners(); observeViewModel()
    }

    private fun setupClickListeners() {
        b.btnAnalyzeAudit.setOnClickListener { vm.performAudit() }
        b.btnFixAudit.setOnClickListener { vm.fixInconsistencies() }
    }

    private fun observeViewModel() {
        vm.auditResult.observe(this) { count ->
            if (count != null) {
                if (count > 0) {
                    b.cvAuditSummary.isVisible = true; b.tvNoIssues.isVisible = false
                    b.tvAuditSummary.text = getString(R.string.category_audit_summary_format, count)
                } else {
                    b.cvAuditSummary.isVisible = false; b.tvNoIssues.isVisible = true
                }
            }
        }
        vm.updateStatus.observe(this) { count ->
            if (count != null) {
                val message = resources.getQuantityString(R.plurals.category_audit_success_format, count, count)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
