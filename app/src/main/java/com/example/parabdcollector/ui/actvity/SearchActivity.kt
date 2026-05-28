package com.example.parabdcollector.ui.actvity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.*
import com.example.parabdcollector.*
import com.example.parabdcollector.databinding.ActivitySearchBinding
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.ui.adapter.CollectionAdapter
import com.example.parabdcollector.ui.viewmodel.*
import com.example.parabdcollector.utils.*

class SearchActivity : AppCompatActivity() {
    private lateinit var b: ActivitySearchBinding; private lateinit var ad: CollectionAdapter; private lateinit var img: ImageCaptureUtil
    private var desc = ""; private var bmp: Bitmap? = null; private var simple = true; private var q: String? = null; private var crit: SearchCriteria? = null
    private var dWidth: Double? = null; private var dHeight: Double? = null
    private var currentImageUri: Uri? = null
    
    private val measureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val data = res.data
            // On vérifie intelligemment quelle activité a renvoyé les données
            val w = if (data?.hasExtra(MeasureActivity.EXTRA_WIDTH) == true) {
                data.getDoubleExtra(MeasureActivity.EXTRA_WIDTH, 0.0)
            } else {
                data?.getDoubleExtra(CoinMeasureActivity.EXTRA_RESULT_WIDTH, 0.0) ?: 0.0
            }
            
            val h = if (data?.hasExtra(MeasureActivity.EXTRA_HEIGHT) == true) {
                data.getDoubleExtra(MeasureActivity.EXTRA_HEIGHT, 0.0)
            } else {
                data?.getDoubleExtra(CoinMeasureActivity.EXTRA_RESULT_HEIGHT, 0.0) ?: 0.0
            }
            
            dWidth = w; dHeight = h
            if (dWidth != null && dWidth!! > 0.1) {
                val formatString = if (dHeight!! > 0.1) {
                    String.format("%.1f / %.1f", dWidth, dHeight)
                } else {
                    String.format("%.1f", dWidth)
                }
                
                if (b.advancedSearchFields.isGone) toggleAdvancedSearch()
                b.etSearchDimensions.setText(formatString)
                Toast.makeText(this, "Dimensions capturées : $formatString cm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val vm: SearchViewModel by viewModels { val a = application as CollectionApplication; ViewModelFactory(a, a.repository, a.locationRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySearchBinding.inflate(layoutInflater); setContentView(b.root)
        setSupportActionBar(b.toolbar); supportActionBar?.setDisplayHomeAsUpEnabled(true); supportActionBar?.title = getString(R.string.menu_search_title)
        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, i -> val s = i.getInsets(WindowInsetsCompat.Type.systemBars()); v.updatePadding(s.left, s.top, s.right, s.bottom); WindowInsetsCompat.CONSUMED }
        img = ImageCaptureUtil(this) { u ->
            if (u == null) return@ImageCaptureUtil
            currentImageUri = u
            val bit = BitmapUtils.getBitmapFromUri(this, u)
            if (bit != null) {
                bmp = bit; b.ivSearchImagePreview.setImageBitmap(bit)
                if (b.advancedSearchFields.isGone) toggleAdvancedSearch()
                b.ivSearchImagePreview.isVisible = true; vm.calculateSignatureForPreview(bit)
            } else b.ivSearchImagePreview.isGone = true
            validateSearchButton()
        }
        setupRecyclerView(); setupSpinners(); setupClickListeners(); setupTextWatchers(); observeViewModel(); validateSearchButton()
        
        // Le bouton de mesure est toujours visible, car on a le fallback 2€.
        b.btnMeasure.isVisible = true
    }

    private fun setupClickListeners() {
        b.btnSearch.setOnClickListener { performSearch() }
        b.ivSearchImagePreview.setOnClickListener { img.startCamera() }
        b.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }
        b.btnRetry.setOnClickListener { retryLastSearch() }
        b.btnSearchByCamera.setOnClickListener { img.startCamera() }
        b.btnSearchByGallery.setOnClickListener { img.startGallery() }
        b.btnMeasure.setOnClickListener { 
            if (ARCoreHelper.isARCoreSupported(this)) {
                measureLauncher.launch(Intent(this, MeasureActivity::class.java))
            } else {
                // Pour la pièce de 2€, on a besoin d'une photo déjà prise
                if (currentImageUri != null) {
                    val intent = Intent(this, CoinMeasureActivity::class.java)
                    intent.putExtra(CoinMeasureActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
                    measureLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Prenez d'abord une photo avec la pièce de 2€", Toast.LENGTH_LONG).show()
                    img.startCamera()
                }
            }
        }
        b.fabScrollToTop.setOnClickListener { b.searchScrollView.smoothScrollTo(0, 0) }
    }

    private fun setupTextWatchers() {
        val w = { _: Editable? -> validateSearchButton() }
        b.etSearchSimple.doAfterTextChanged(w); b.etSearchTitle.doAfterTextChanged(w); b.etSearchSuperCategory.doAfterTextChanged(w)
        b.etSearchCategory.doAfterTextChanged(w); b.etSearchEditor.doAfterTextChanged(w); b.etSearchYear.doAfterTextChanged(w)
        b.etSearchMonth.doAfterTextChanged(w); b.etSearchDescription.doAfterTextChanged(w); b.etSearchTirage.doAfterTextChanged(w)
        b.etSearchDimensions.doAfterTextChanged(w); b.etSearchStatus.doAfterTextChanged(w)
    }

    private fun validateSearchButton() {
        val isAdv = b.advancedSearchFields.isVisible
        val ok = if (isAdv) {
            val fs = listOf(b.etSearchTitle, b.etSearchSuperCategory, b.etSearchCategory, b.etSearchEditor, b.etSearchYear, b.etSearchMonth, b.etSearchDescription, b.etSearchTirage, b.etSearchDimensions)
            val anyF = fs.any { !it.text.isNullOrBlank() }
            val st = b.etSearchStatus.text.toString(); val stOk = st.isNotBlank() && st != resources.getStringArray(R.array.search_status_options)[0]
            anyF || bmp != null || stOk
        } else {
            !b.etSearchSimple.text.isNullOrBlank()
        }
        b.btnSearch.isEnabled = ok
    }

    private fun observeViewModel() {
        vm.searchResultState.observe(this) { s ->
            b.progressBar.isVisible = s is SearchResultState.Loading; b.resultsListContainer.isVisible = s is SearchResultState.Success
            b.errorContainer.isVisible = s is SearchResultState.Error
            // On cache "tvNoResults" si c'est une recherche image pour utiliser le bandeau tvResultsSummary à la place
            b.tvNoResults.isVisible = s is SearchResultState.Success && s.results.isEmpty() && bmp == null
            b.fabScrollToTop.isVisible = s is SearchResultState.Success && s.results.isNotEmpty()
            if (s is SearchResultState.Success) {
                ad.submitList(s.results); updateResultSummary(s.totalCount, s.results.size, s.isFallback)
            } else if (s is SearchResultState.Idle) { ad.submitList(emptyList()); updateResultSummary(0, 0, false) }
        }
        vm.signaturePreview.observe(this) { p -> b.tvSignaturePreview.text = p; b.tvSignaturePreview.isVisible = p.isNotBlank() }
        
        vm.detectedWords.observe(this) { words ->
            if (words.isNotEmpty()) {
                val text = words.joinToString(" ")
                // On informe l'utilisateur, mais on ne remplit pas le champ ici
                Toast.makeText(this, "Indices textuels détectés : $text", Toast.LENGTH_SHORT).show()
                if (b.advancedSearchFields.isGone) toggleAdvancedSearch()
            }
        }

        vm.matchedPublisher.observe(this) { publisher ->
            if (!publisher.isNullOrBlank()) {
                // On remplit le champ éditeur avec le nom officiel
                b.etSearchEditor.setText(publisher)
                Toast.makeText(this, "Éditeur reconnu : $publisher", Toast.LENGTH_SHORT).show()
                if (b.advancedSearchFields.isGone) toggleAdvancedSearch()
            }
        }
    }

    private fun updateResultSummary(total: Int, displayed: Int, isFallback: Boolean) {
        if (total > 0) {
            val base = resources.getQuantityString(R.plurals.search_results_count_with_criteria, total, total, desc)
            
            val info = when {
                bmp != null && isFallback -> getString(R.string.search_fallback_results)
                bmp != null -> getString(R.string.search_visual_matches, displayed)
                total > displayed -> getString(R.string.search_displayed_count, displayed)
                else -> ""
            }
            
            if (bmp != null && isFallback) {
                // En mode fallback, on remplace tout le texte par l'avertissement
                b.tvResultsSummary.text = info
            } else {
                b.tvResultsSummary.text = getString(R.string.search_results_summary_format, base, info)
            }
            b.tvResultsSummary.isVisible = true
        } else if (bmp != null) {
            b.tvResultsSummary.text = getString(R.string.search_no_visual_matches)
            b.tvResultsSummary.isVisible = true
        } else b.tvResultsSummary.isGone = true
    }

    private fun clearFields() {
        listOf(b.etSearchTitle, b.etSearchEditor, b.etSearchYear, b.etSearchMonth, b.etSearchDescription, b.etSearchTirage, b.etSearchDimensions).forEach { it.setText("") }
        b.etSearchSuperCategory.setText("", false); b.etSearchCategory.setText("", false)
        b.etSearchStatus.setText(resources.getStringArray(R.array.search_status_options)[0], false)
        bmp = null; b.ivSearchImagePreview.isGone = true; b.tvSignaturePreview.isGone = true
        dWidth = null; dHeight = null
    }

    private fun resetSearchState() {
        b.etSearchSimple.setText(""); b.etSearchSimple.isEnabled = true; b.etSearchSimple.alpha = 1.0f
        if (b.advancedSearchFields.isVisible) toggleAdvancedSearch()
        clearFields(); desc = ""; q = null; crit = null; vm.clearSearchResults(); validateSearchButton()
    }

    private fun retryLastSearch() {
        if (simple && q != null) vm.search(q!!) else if (!simple && crit != null) vm.advancedSearch(crit!!, bmp) else resetSearchState()
    }

    private fun toggleAdvancedSearch() {
        val open = b.advancedSearchFields.isGone
        if (open) {
            b.advancedSearchFields.isVisible = true; b.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
            b.etSearchSimple.setText(""); b.etSearchSimple.isEnabled = false; b.etSearchSimple.alpha = 0.5f
        } else {
            b.advancedSearchFields.isGone = true; b.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
            b.etSearchSimple.isEnabled = true; b.etSearchSimple.alpha = 1.0f; clearFields()
        }
        validateSearchButton()
    }

    private fun setupSpinners() {
        val opts = resources.getStringArray(R.array.search_status_options); b.etSearchStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, opts))
        b.etSearchStatus.setText(opts[0], false); val sc = CategoryMapper.getSuperCategories()
        b.etSearchSuperCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, sc))
        b.etSearchSuperCategory.setOnItemClickListener { p, _, pos, _ ->
            val sel = p.getItemAtPosition(pos) as String
            b.etSearchCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, CategoryMapper.getCategoriesFor(sel)))
            b.layoutSearchCategory.isEnabled = true; b.etSearchCategory.setText("", false); validateSearchButton()
        }
    }

    private fun performSearch() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        if (b.advancedSearchFields.isVisible) {
            val o = resources.getStringArray(R.array.search_status_options); val s = b.etSearchStatus.text.toString()
            val isP = when (s) { o[1] -> true; o[2] -> false; else -> null }
            val c = SearchCriteria(
                titre = b.etSearchTitle.text.toString().trim().takeIf { it.isNotBlank() }, 
                superCategorie = b.etSearchSuperCategory.text.toString().trim().takeIf { it.isNotBlank() }, 
                categorie = b.etSearchCategory.text.toString().trim().takeIf { it.isNotBlank() }, 
                editeur = b.etSearchEditor.text.toString().trim().takeIf { it.isNotBlank() }, 
                annee = b.etSearchYear.text.toString().trim().toIntOrNull(), 
                mois = b.etSearchMonth.text.toString().trim().toIntOrNull(), 
                description = b.etSearchDescription.text.toString().trim().takeIf { it.isNotBlank() }, 
                tirage = b.etSearchTirage.text.toString().trim().takeIf { it.isNotBlank() }, 
                dimensions = b.etSearchDimensions.text.toString().trim().takeIf { it.isNotBlank() }, 
                isPossessed = isP,
                detectedWidth = dWidth?.takeIf { it > 0.1 },
                detectedHeight = dHeight?.takeIf { it > 0.1 },
                queryAspectRatio = bmp?.let { it.width.toDouble() / it.height.toDouble() }
            )
            crit = c 
            simple = false
            val prefix = if (bmp != null) "Image + " else ""
            desc = prefix + listOfNotNull(c.titre, c.editeur, s, c.superCategorie, c.categorie).joinToString(", ").ifBlank { "Avancée" }
            vm.advancedSearch(c, bmp)
        } else {
            val query = b.etSearchSimple.text.toString().trim()
            if (query.isNotBlank()) { desc = query; simple = true; q = query; vm.search(query) }
        }
    }

    private fun setupRecyclerView() { ad = CollectionAdapter { r -> val i = Intent(this, EditItemActivity::class.java); i.putExtra("itemId", r.item.id); startActivity(i) }; b.rvSearchResults.adapter = ad; b.rvSearchResults.layoutManager = LinearLayoutManager(this) }
    override fun onCreateOptionsMenu(m: Menu?): Boolean { menuInflater.inflate(R.menu.search_menu, m); return true }
    override fun onOptionsItemSelected(i: MenuItem): Boolean {
        when (i.itemId) {
            android.R.id.home -> { finish(); return true }
            R.id.action_refine_search -> { b.resultsListContainer.isGone = true; b.errorContainer.isGone = true; b.tvNoResults.isGone = true; b.progressBar.isGone = true; return true }
            R.id.action_new_search -> { resetSearchState(); return true }
        }
        return super.onOptionsItemSelected(i)
    }
}
