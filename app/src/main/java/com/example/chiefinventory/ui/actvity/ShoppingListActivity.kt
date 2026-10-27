package com.example.chiefinventory.ui.actvity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chiefinventory.databinding.ActivityShoppingListBinding
import com.example.chiefinventory.ui.adapter.ShoppingListAdapter
import com.example.chiefinventory.ui.viewmodel.ShoppingListViewModel

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding
    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ShoppingListAdapter { ingredient, isBought ->
            viewModel.markItemBought(ingredient, isBought)
        }
        binding.rvShoppingList.adapter = adapter

        viewModel.outOfStockIngredients.observe(this) { ingredients ->
            if (ingredients.isNullOrEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvShoppingList.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvShoppingList.visibility = View.VISIBLE
                adapter.submitList(ingredients)
            }
        }
    }
}
