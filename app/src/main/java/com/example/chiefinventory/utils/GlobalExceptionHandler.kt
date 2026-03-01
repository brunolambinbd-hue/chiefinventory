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
        // Écrire l'exception dans un fichier de log
        val stackTrace = e.stackTraceToString()
        writeErrorToLog(stackTrace)

        // Lancer une activité qui affiche un message d'erreur et permet de redémarrer.
        val intent = Intent(applicationContext, CrashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(CrashActivity.EXTRA_CRASH_INFO, stackTrace)
        }
        applicationContext.startActivity(intent)

        // Tuer le processus de l'application pour forcer un redémarrage propre.
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    private fun writeErrorToLog(stackTrace: String) {
        val logFile = File(applicationContext.filesDir, "crash_log.txt")
        try {
            FileWriter(logFile, true).use { writer ->
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                writer.append("\n--- $timestamp ---\n")
                writer.append(stackTrace)
            }
        } catch (_: Exception) {
            // Si l'écriture du log échoue, on ne peut pas faire grand-chose de plus.
        }
    }
}
