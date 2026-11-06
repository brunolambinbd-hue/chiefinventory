package com.chefinventory.app.ui.actvity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chefinventory.app.databinding.ActivityDiagnosticBinding
import com.chefinventory.app.databinding.ItemResourceIssueBinding
import com.chefinventory.app.utils.DiagnosticReport
import com.chefinventory.app.utils.ResourceHealthChecker
import com.chefinventory.app.utils.ResourceIssue

/**
 * Activité dédiée au diagnostic complet de santé et d'intégrité des ressources XML de l'application.
 */
class ResourceDiagnosticActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnosticBinding
    private lateinit var adapter: IssuesAdapter
    private var latestReport: DiagnosticReport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarDiagnostic.setNavigationOnClickListener {
            finish()
        }

        binding.rvDiagnosticIssues.layoutManager = LinearLayoutManager(this)
        adapter = IssuesAdapter()
        binding.rvDiagnosticIssues.adapter = adapter

        binding.btnCopyReport.setOnClickListener {
            copyReportToClipboard()
        }

        runDiagnostic()
    }

    private fun runDiagnostic() {
        val report = ResourceHealthChecker.check(this)
        latestReport = report
        displayReport(report)
    }

    private fun displayReport(report: DiagnosticReport) {
        if (report.isHealthy) {
            binding.tvDiagnosticSummaryTitle.text = "🟢 Toutes les Ressources OK"
            binding.tvDiagnosticSummaryDetail.text = "Les ${report.checkedCount} contrôles de ressources se sont déroulés avec succès. Le scanner OCR est totalement opérationnel."
            adapter.submitList(emptyList())
        } else {
            binding.tvDiagnosticSummaryTitle.text = "🔴 Anomalies de Ressources Détectées"
            binding.tvDiagnosticSummaryDetail.text = "${report.issues.size} anomalie(s) détectée(s) sur ${report.checkedCount} contrôles. Le scan OCR est bloqué tant que ces ressources ne sont pas restaurées."
            adapter.submitList(report.issues)
        }
    }

    private fun copyReportToClipboard() {
        val report = latestReport ?: return
        val sb = StringBuilder()
        sb.appendLine("=== BILAN DE DIAGNOSTIC DES RESSOURCES OCR ===")
        sb.appendLine("Statut : ${if (report.isHealthy) "🟢 OK" else "🔴 ANOMALIES DÉTECTÉES"}")
        sb.appendLine("Contrôles : ${report.checkedCount} exécutés, ${report.issues.size} anomalie(s)")
        sb.appendLine("----------------------------------------------")

        if (report.issues.isEmpty()) {
            sb.appendLine("Toutes les ressources XML sont présentes et valides.")
        } else {
            report.issues.forEach { issue ->
                sb.appendLine("[${issue.domain}] ${issue.resourceName} : ${issue.description}")
            }
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Rapport Diagnostic ChefInventory", sb.toString())
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Rapport copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
    }

    private class IssuesAdapter : RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {
        private var issues = listOf<ResourceIssue>()

        fun submitList(newList: List<ResourceIssue>) {
            issues = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
            val binding = ItemResourceIssueBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return IssueViewHolder(binding)
        }

        override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
            holder.bind(issues[position])
        }

        override fun getItemCount(): Int = issues.size

        class IssueViewHolder(private val binding: ItemResourceIssueBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(issue: ResourceIssue) {
                binding.tvIssueIcon.text = if (issue.isCritical) "🔴" else "⚠️"
                binding.tvResourceName.text = issue.resourceName
                binding.tvResourceDomain.text = issue.domain
                binding.tvIssueDescription.text = issue.description
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ResourceDiagnosticActivity::class.java)
            context.startActivity(intent)
        }
    }
}
