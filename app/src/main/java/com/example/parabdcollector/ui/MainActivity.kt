package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.databinding.ActivityMainBinding
import com.example.parabdcollector.model.CollectionItem

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: CollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // On utilise la version la plus simple de l'adaptateur
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

        // --- DÉSACTIVATION TEMPORAIRE DE LA BASE DE DONNÉES ---
        // Ce bloc sera réactivé une fois que l'application sera stable.
        /*
        viewModel.allItems.observe(this) { items ->
            adapter.submitList(items)
        }
        */
    }
}