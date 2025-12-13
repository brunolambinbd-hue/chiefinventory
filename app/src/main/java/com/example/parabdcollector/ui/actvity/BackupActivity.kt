package com.example.parabdcollector.ui.actvity

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.parabdcollector.BuildConfig
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.databinding.ActivityBackupBinding
import com.example.parabdcollector.ui.viewmodel.BackupViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * An activity for managing database backup and restore operations.
 */
class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    private val viewModel: BackupViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    // Launcher for the backup file creation intent.
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/x-sqlite3")) { uri: Uri? ->
        uri?.let { viewModel.backupDatabase(it) }
    }

    // Launcher for the restore file selection intent.
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    /**
     * Initializes the activity, sets up the toolbar, click listeners, observers, and version info display.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        displayVersionInfo()
    }

    /**
     * Sets up the activity's toolbar and enables the Up button.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sauvegarde / Restauration"
    }

    /**
     * Sets up the click listeners for the backup and restore buttons.
     */
    private fun setupClickListeners() {
        binding.btnBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "parabd_backup_$timestamp.db"
            createDocumentLauncher.launch(fileName)
        }

        binding.btnRestore.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream"))
        }
    }

    /**
     * Observes the operation status LiveData from the ViewModel and shows a Toast on update.
     */
    private fun observeViewModel() {
        viewModel.operationStatus.observe(this) { status ->
            Toast.makeText(this, status, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Retrieves and displays the application and database version numbers in the UI.
     */
    private fun displayVersionInfo() {
        // Retrieve app version from BuildConfig
        val appVersion = BuildConfig.VERSION_NAME
        // Retrieve database version from our AppDatabase constant
        val dbVersion = AppDatabase.DATABASE_VERSION
        // Format the string and set it to the TextView
        binding.tvVersionInfo.text = "App v$appVersion (DB v$dbVersion)"
    }

    /**
     * Handles action bar item selections. Specifically, the "Up" button.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
