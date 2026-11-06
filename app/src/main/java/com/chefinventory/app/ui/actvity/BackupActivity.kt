package com.chefinventory.app.ui.actvity

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chefinventory.app.BuildConfig
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.R
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.ActivityBackupBinding
import com.chefinventory.app.ui.viewmodel.BackupViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * An activity for managing database backup and restore operations.
 */
class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    private val viewModel: BackupViewModel by viewModels {
        val app = application as ChefInventoryApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository)
    }

    // Launcher for the backup file creation intent.
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/x-sqlite3")) { uri: Uri? ->
        uri?.let { viewModel.backupDatabase(it) }
    }

    // Launcher for the restore file selection intent.
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_backup_title)

        setupVersionInfo()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupVersionInfo() {
        val gitHash = BuildConfig.GIT_COMMIT_HASH
        val buildDate = BuildConfig.BUILD_DATE
        val versionInfo = getString(
            R.string.version_info_format,
            BuildConfig.VERSION_NAME,
            AppDatabase.DATABASE_VERSION,
            BuildConfig.API_URL,
            gitHash,
            buildDate
        )
        binding.tvVersionInfo.text = versionInfo
    }

    private fun setupClickListeners() {
        binding.btnBackup.setOnClickListener {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val defaultFileName = "chef_inventory_backup_$timeStamp.db"
            createDocumentLauncher.launch(defaultFileName)
        }

        binding.btnRestore.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream", "*/*"))
        }

        binding.btnDiagnostic.setOnClickListener {
            ResourceDiagnosticActivity.start(this)
        }

        binding.btnTestCrash.setOnClickListener {
            throw RuntimeException("Test Crash triggered from BackupActivity")
        }
    }

    private fun observeViewModel() {
        viewModel.operationStatus.observe(this) { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
