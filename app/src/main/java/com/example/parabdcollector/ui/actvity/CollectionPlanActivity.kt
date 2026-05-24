package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityCollectionPlanBinding
import com.example.parabdcollector.ui.adapter.CollectionPlanAdapter
import com.example.parabdcollector.ui.viewmodel.CollectionPlanViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

class CollectionPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollectionPlanBinding
    private val viewModel: CollectionPlanViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.importRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = CollectionPlanAdapter { superCat, cat ->
            val intent = Intent(this, ItemListActivity::class.java).apply {
                putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_SOUGHT)
                putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, superCat)
                putExtra(ItemListActivity.EXTRA_CATEGORY, cat)
            }
            startActivity(intent)
        }

        binding.rvCollectionPlan.layoutManager = LinearLayoutManager(this)
        binding.rvCollectionPlan.adapter = adapter

        viewModel.hierarchy.observe(this) { list ->
            adapter.submitList(list)
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
