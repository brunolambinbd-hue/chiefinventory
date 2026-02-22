package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityImportHistoryBinding
import com.example.parabdcollector.model.ImportSession
import com.example.parabdcollector.ui.adapter.ImportSessionAdapter
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

class ImportHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportHistoryBinding
    private val viewModel: com.example.parabdcollector.ui.viewmodel.ImportViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.importRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ImportSessionAdapter(
            onSessionClicked = { session ->
                val intent = Intent(this, ItemListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_SESSION)
                    putExtra(ItemListActivity.EXTRA_SESSION_ID, session.id)
                    putExtra(ItemListActivity.EXTRA_SESSION_NAME, session.fileName)
                }
                startActivity(intent)
            },
            onSessionLongClicked = { session ->
                showDeleteConfirmation(session)
            }
        )

        binding.rvImportHistory.layoutManager = LinearLayoutManager(this)
        binding.rvImportHistory.adapter = adapter

        viewModel.allSessions.observe(this) { sessions ->
            adapter.submitList(sessions)
        }
    }

    private fun showDeleteConfirmation(session: ImportSession) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Supprimer cette session ?")
            .setMessage("Voulez-vous supprimer l'entrée de l'historique pour \"${session.fileName}\" ?\n(Cela ne supprimera pas les BD importées)")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteSession(session)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
