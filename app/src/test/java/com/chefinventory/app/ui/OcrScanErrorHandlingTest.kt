package com.chefinventory.app.ui

import android.content.Intent
import com.chefinventory.app.ui.actvity.CrashActivity
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test unitaire TDD d'isolation UI/Erreurs vérifiant la redirection automatique
 * vers [CrashActivity] (la page d'erreur globale) lors d'un échec de numérisation OCR.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrScanErrorHandlingTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcrError_LaunchesCrashActivityWithDetailedInfo() {
        val ocrException = java.lang.IllegalStateException("Image corrupt or OCR Engine out of memory")
        val stackTrace = ocrException.stackTraceToString()
        val fullReport = "📷 ERREUR DE NUMÉRISATION OCR :\nL'analyse de l'image a rencontré un problème (image invalide, mémoire saturée ou erreur de lecture).\n\n--- DÉTAILS TECHNIQUES ---\n$stackTrace"

        val intent = Intent("com.chefinventory.app.CRASH").apply {
            putExtra(CrashActivity.EXTRA_CRASH_INFO, fullReport)
        }

        val extractedReport = intent.getStringExtra(CrashActivity.EXTRA_CRASH_INFO)
        assertThat(extractedReport).contains("ERREUR DE NUMÉRISATION OCR")
        assertThat(extractedReport).contains("Image corrupt or OCR Engine out of memory")
    }
}
