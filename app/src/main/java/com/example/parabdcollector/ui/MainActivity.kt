package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.databinding.ActivityMainBinding
import com.example.parabdcollector.db.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // On utilise 'applicationContext' pour être sûr d'avoir le bon contexte.
    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CollectionRepository(database.collectionDao())
        ViewModelFactory(repository)
    }

    private lateinit var adapter: CollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CollectionAdapter { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", item.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditItemActivity::class.java))
        }

        // La base de données est toujours active
        viewModel.allItems.observe(this) { items ->
            adapter.submitList(items)
        }
    }
}