package com.example.parabdcollector.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivitySearchBinding
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.utils.CategoryMapper
import java.io.File

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: CollectionAdapter
    private var currentSearchDescription: String = ""
    private var searchImageBitmap: Bitmap? = null
    private var photoUri: Uri? = null // Pour stocker l'URI de la photo originale

    private val viewModel: SearchViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    // Lanceur pour le recadrage
    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, croppedUri)
                searchImageBitmap = bitmap
                binding.ivSearchThumbnail.setImageBitmap(bitmap)
                binding.ivSearchThumbnail.visibility = View.VISIBLE
                viewModel.calculateSignatureForPreview(bitmap)
            }
        } else {
            val exception = result.error
            Toast.makeText(this, "Erreur de recadrage: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanceur pour la prise de photo
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                val cropOptions = CropImageOptions().apply {
                    allowRotation = true
                    allowFlipping = true
                }
                val cropContractOptions = CropImageContractOptions(uri, cropOptions)
                cropImageLauncher.launch(cropContractOptions)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.toast_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_search_title)

        setupRecyclerView()
        setupCategorySpinners()

        binding.btnSearch.setOnClickListener { performSearch() }
        binding.btnSearchByImage.setOnClickListener { onSearchByImageClicked() }

        binding.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }

        viewModel.searchResults.observe(this) { results ->
            adapter.submitList(results)
            val count = results.size
            if (count > 0 || currentSearchDescription.isNotBlank()) {
                binding.tvResultsCount.text = resources.getQuantityString(R.plurals.search_results_count_with_criteria, count, count, currentSearchDescription)
                binding.tvResultsCount.isVisible = true
            } else {
                binding.tvResultsCount.isVisible = false
            }
        }

        viewModel.signaturePreview.observe(this) { preview ->
            if (preview.isNotBlank()) {
                binding.tvSearchThumbnailSignature.text = preview
                binding.tvSearchThumbnailSignature.visibility = View.VISIBLE
            } else {
                binding.tvSearchThumbnailSignature.visibility = View.GONE
            }
        }
    }

    private fun resetSearchState() {
        binding.etSearchSimple.setText("")
        binding.etSearchTitre.setText("")
        binding.etSearchEditor.setText("")
        // ... (le reste des resets)
        searchImageBitmap = null
        binding.ivSearchThumbnail.visibility = View.GONE
        binding.tvSearchThumbnailSignature.visibility = View.GONE
        viewModel.clearSearchResults()
    }

    private fun toggleAdvancedSearch() {
        if (binding.advancedSearchContainer.isGone) {
            binding.advancedSearchContainer.isVisible = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
        } else {
            binding.advancedSearchContainer.isGone = true
            binding.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
        }
    }

    private fun onSearchByImageClicked() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val imageFile = File(filesDir, "images/search_${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)
        photoUri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    private fun setupCategorySpinners() {
        val superCategories = CategoryMapper.getSuperCategories()
        val superCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, superCategories)
        binding.etSearchSuperCategory.setAdapter(superCategoryAdapter)

        binding.categorySearchLayout.isEnabled = false

        binding.etSearchSuperCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedSuperCategory = parent.getItemAtPosition(position) as String
            val categories = CategoryMapper.getCategoriesFor(selectedSuperCategory)
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
            binding.etSearchCategory.setAdapter(categoryAdapter)

            binding.categorySearchLayout.isEnabled = true
            binding.etSearchCategory.text = null
        }
    }

    private fun performSearch() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val simpleQuery = binding.etSearchSimple.text.toString()
        if (simpleQuery.isNotBlank()) {
            currentSearchDescription = simpleQuery
            viewModel.search(simpleQuery)
        } else {
            val criteria = SearchCriteria(
                titre = binding.etSearchTitre.text.toString().takeIf { it.isNotBlank() },
                editeur = binding.etSearchEditor.text.toString().takeIf { it.isNotBlank() },
                annee = binding.etSearchYear.text.toString().toIntOrNull(),
                mois = binding.etSearchedMonth.text.toString().toIntOrNull(),
                superCategorie = binding.etSearchSuperCategory.text.toString().takeIf { it.isNotBlank() },
                categorie = binding.etSearchCategory.text.toString().takeIf { it.isNotBlank() },
                description = binding.etSearchDescription.text.toString().takeIf { it.isNotBlank() },
                tirage = binding.etSearchTirage.text.toString().takeIf { it.isNotBlank() },
                dimensions = binding.etSearchDimensions.text.toString().takeIf { it.isNotBlank() }
            )
            viewModel.advancedSearch(criteria, searchImageBitmap)
        }
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", searchResult.item.id)
            startActivity(intent)
        }
        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_refine_search -> {
                if (binding.advancedSearchContainer.isGone) {
                    toggleAdvancedSearch()
                }
                return true
            }
            R.id.action_new_search -> {
                resetSearchState()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
