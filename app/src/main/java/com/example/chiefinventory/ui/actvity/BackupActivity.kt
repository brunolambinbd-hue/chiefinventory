package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chiefinventory.BuildConfig
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.databinding.ActivityBackupBinding
import com.example.chiefinventory.ui.viewmodel.BackupViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
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
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
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
        supportActionBar?.title = "Maintenance / Sauvegarde"
    }

    /**
     * Sets up the click listeners for the backup and restore buttons.
     */
    private fun setupClickListeners() {
        binding.btnBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "chiefinventory_backup_$timestamp.db"
            createDocumentLauncher.launch(fileName)
        }

        binding.btnRestore.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream"))
        }

        binding.btnTestCrash.setOnClickListener {
            throw RuntimeException("Test Crash triggered from BackupActivity")
        }

        binding.btnUnlocatedItems.setOnClickListener { 
            val intent = Intent(this, ItemListActivity::class.java).apply {
                putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_UNLOCATED)
            }
            startActivity(intent)
        }

        binding.btnLocatedNotPossessed.setOnClickListener { 
            val intent = Intent(this, ItemListActivity::class.java).apply {
                putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_LOCATED_NOT_POSSESSED)
            }
            startActivity(intent)
        }

        binding.btnBatchUpdate.setOnClickListener {
            startActivity(Intent(this, BatchPossessionActivity::class.java))
        }

        binding.btnCategoryAudit.setOnClickListener {
            startActivity(Intent(this, CategoryAuditActivity::class.java))
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
     * Retrieves and displays the application version, database version, API URL, and Git commit hash in the UI.
     */
    private fun displayVersionInfo() {
        // Retrieve values from BuildConfig and database
        val appVersion = BuildConfig.VERSION_NAME
        val dbVersion = AppDatabase.DATABASE_VERSION
        val apiUrl = BuildConfig.API_URL
        val commitHash = BuildConfig.GIT_COMMIT_HASH

        // Format the string using the resource and set it to the TextView
        binding.tvVersionInfo.text = getString(R.string.version_info_format, appVersion, dbVersion, apiUrl, commitHash)
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
