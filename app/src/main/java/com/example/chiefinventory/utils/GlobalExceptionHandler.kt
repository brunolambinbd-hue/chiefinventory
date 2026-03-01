package com.example.chiefinventory.utils

import android.content.Context
import android.content.Intent
import android.os.Process
import com.example.chiefinventory.ui.actvity.CrashActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Custom uncaught exception handler that logs the crash to a file and launches a CrashActivity.
 */
class GlobalExceptionHandler(private val applicationContext: Context) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        val stackTrace = e.stackTraceToString()
        val userFriendlyMessage = analyzeException(e)
        
        val fullReport = "$userFriendlyMessage\n\n--- DÉTAILS TECHNIQUES ---\n$stackTrace"
        
        writeErrorToLog(fullReport)

        val intent = Intent(applicationContext, CrashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(CrashActivity.EXTRA_CRASH_INFO, fullReport)
        }
        applicationContext.startActivity(intent)

        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    private fun analyzeException(e: Throwable): String {
        val message = e.message ?: ""
        val stackTrace = e.stackTraceToString()

        return when {
            stackTrace.contains("RoomOpenHelper") || stackTrace.contains("migration") -> 
                "🏠 PROBLÈME DE BASE DE DONNÉES :\nLa structure des données a changé. Si le crash persiste, essayez de 'Vider les données' de l'application dans les paramètres Android."
            
            e is android.content.ActivityNotFoundException -> 
                "🧭 ACTIVITÉ INTROUVABLE :\nL'application a essayé d'ouvrir un écran qui n'est pas déclaré dans le Manifest. Vérifiez le fichier AndroidManifest.xml."
            
            e is OutOfMemoryError -> 
                "💾 MÉMOIRE SATURÉE :\nL'application n'a plus assez de mémoire vive. Cela arrive souvent lors du traitement d'images très lourdes."
            
            stackTrace.contains("NullPointerException") -> 
                "🎯 ERREUR DE RÉFÉRENCE VIDE :\nL'application a tenté d'utiliser un objet qui n'existe pas encore ou qui a été supprimé."
            
            stackTrace.contains("IllegalArgumentException") && stackTrace.contains("addSource") ->
                "🔄 CONFLIT DE DONNÉES (LiveData) :\nUn problème est survenu lors de la mise à jour des informations à l'écran (souvent lié aux filtres de recherche)."

            else -> "❌ ERREUR INCONNUE :\nL'application a rencontré un problème inattendu."
        }
    }

    private fun writeErrorToLog(report: String) {
        val logFile = File(applicationContext.filesDir, "crash_log.txt")
        try {
            FileWriter(logFile, true).use { writer ->
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                writer.append("\n--- CRASH LE $timestamp ---\n")
                writer.append(report)
            }
        } catch (_: Exception) {}
    }
}
