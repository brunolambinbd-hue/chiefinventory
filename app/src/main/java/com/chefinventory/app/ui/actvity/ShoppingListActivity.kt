package com.chefinventory.app.ui.actvity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.databinding.ActivityShoppingListBinding
import com.chefinventory.app.ui.adapter.ShoppingListAdapter
import com.chefinventory.app.ui.viewmodel.ShoppingListViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding
    private val viewModel: ShoppingListViewModel by viewModels {
        val app = application as ChefInventoryApplication
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository)
    }
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
