package com.chefinventory.app.ui.actvity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.ActivityOpenFoodFactsProductDetailBinding
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.ui.viewmodel.OpenFoodFactsProductDetailViewModel

/**
 * Écran d'IHM de consultation et d'édition visuelle des fiches ingrédients Open Food Facts (Niveau 2).
 */
class OpenFoodFactsProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BARCODE = "EXTRA_BARCODE"
        const val EXTRA_TAXONOMY_ID = "EXTRA_TAXONOMY_ID"
        const val EXTRA_CLEAN_NAME = "EXTRA_CLEAN_NAME"
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }

    private lateinit var binding: ActivityOpenFoodFactsProductDetailBinding
    private lateinit var viewModel: OpenFoodFactsProductDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenFoodFactsProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        val offRepository = OpenFoodFactsRepository(database.openFoodFactsDao())
        val ingredientRepository = IngredientRepository(database.ingredientDao())
        viewModel = OpenFoodFactsProductDetailViewModel(offRepository, ingredientRepository)

        setupToolbar()
        setupListeners()
        observeViewModel()

        val barcode = intent.getStringExtra(EXTRA_BARCODE)
        val taxonomyId = intent.getLongExtra(EXTRA_TAXONOMY_ID, 0L)
        val cleanName = intent.getStringExtra(EXTRA_CLEAN_NAME)
        val category = intent.getStringExtra(EXTRA_CATEGORY)

        viewModel.loadProduct(barcode, cleanName, taxonomyId, category)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnImportToPantry.setOnClickListener {
            viewModel.importToPantry()
        }
    }

    private fun observeViewModel() {
        viewModel.taxonomy.observe(this) { taxonomy ->
            if (taxonomy != null) {
                val formattedName = taxonomy.cleanName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                binding.tvProductName.text = formattedName
                binding.tvProductName.isVisible = true

                val categoryText = when {
                    !taxonomy.ecobalyseCategory.isNullOrBlank() -> taxonomy.ecobalyseCategory
                    !taxonomy.category.isNullOrBlank() && taxonomy.category != "Alimentation" -> taxonomy.category
                    else -> "other_food_items"
                }
                binding.tvCategory.text = "Catégorie Écobalyse : $categoryText"
                binding.tvCategory.isVisible = true

                binding.tvCiqualName.text = if (!taxonomy.ciqualName.isNullOrBlank()) {
                    "Dénomination Ciqual / Anses : \"${taxonomy.ciqualName}\""
                } else {
                    "Dénomination Ciqual / Anses : Non renseignée"
                }
                binding.tvCiqualName.isVisible = true

                binding.tvParents.text = if (!taxonomy.parentsStr.isNullOrBlank()) {
                    "🌳 Parent Ontologique OFF : ${taxonomy.parentsStr}"
                } else {
                    "🌳 Parent Ontologique OFF : Non renseigné"
                }
                binding.tvParents.isVisible = true

                binding.tvChildren.text = if (!taxonomy.childrenStr.isNullOrBlank()) {
                    "🌿 Sous-déclinaisons Dérivées OFF : ${taxonomy.childrenStr}"
                } else {
                    "🌿 Sous-déclinaisons Dérivées OFF : Aucune"
                }
                binding.tvChildren.isVisible = true

                binding.tvAverageWeight.text = if (!taxonomy.averageWeightStr.isNullOrBlank()) {
                    "⚖️ Poids Moyen Unitaire : ${taxonomy.averageWeightStr}"
                } else {
                    "⚖️ Poids Moyen Unitaire : Non renseigné"
                }
                binding.tvAverageWeight.isVisible = true

                binding.tvSeason.text = if (!taxonomy.seasonStr.isNullOrBlank()) {
                    "☀️ Saisonnalité en France : ${taxonomy.seasonStr}"
                } else {
                    "☀️ Saisonnalité en France : Toute l'année"
                }
                binding.tvSeason.isVisible = true

                binding.tvEcobalyseCategory.text = if (!taxonomy.ecobalyseCategory.isNullOrBlank()) {
                    "🌿 Catégorie Ecobalyse : ${taxonomy.ecobalyseCategory}"
                } else {
                    "🌿 Catégorie Ecobalyse : Non classée"
                }
                binding.tvEcobalyseCategory.isVisible = true

                binding.tvCarbonFootprint.text = if (!taxonomy.carbonFootprintStr.isNullOrBlank()) {
                    "🌍 Empreinte Carbone : ${taxonomy.carbonFootprintStr}"
                } else {
                    "🌍 Empreinte Carbone : Non évaluée"
                }
                binding.tvCarbonFootprint.isVisible = true
            }
        }

        viewModel.importStatus.observe(this) { status ->
            if (!status.isNullOrBlank()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                viewModel.clearImportStatus()
            }
        }
    }
}
