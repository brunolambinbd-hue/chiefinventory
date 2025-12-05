package com.example.parabdcollector.ui.actvity

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.parabdcollector.databinding.ActivityBackupBinding
import com.example.parabdcollector.ui.viewmodel.BackupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity for handling database backup and restore operations.
 */
class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding
    private val viewModel: BackupViewModel by viewModels()

    // Activity result launcher for creating a backup file.
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let { viewModel.backupDatabase(it) }
    }

    // Activity result launcher for selecting a backup file to restore.
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sauvegarde et Restauration"

        binding.btnBackup.setOnClickListener { launchBackupFilePicker() }
        binding.btnRestore.setOnClickListener { launchRestoreFilePicker() }

        viewModel.operationStatus.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            binding.tvBackupStatus.text = it
        }
    }

    private fun launchBackupFilePicker() {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val fileName = "parabd_backup_${formatter.format(Date())}.db"
        createDocumentLauncher.launch(fileName)
    }

    private fun launchRestoreFilePicker() {
        openDocumentLauncher.launch(arrayOf("application/octet-stream"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
