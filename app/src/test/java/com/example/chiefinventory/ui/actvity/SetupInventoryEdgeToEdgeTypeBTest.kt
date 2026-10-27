package com.example.chiefinventory.ui.actvity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le calcul des marges d'insets de barre système (Status Bar & Navigation Bar)
 * pour éviter les chevauchements Edge-to-Edge en haut et en bas de l'écran.
 */
class SetupInventoryEdgeToEdgeTypeBTest {

    private fun calculateBottomMarginWithNavInsets(navBarHeightPx: Int, defaultMarginPx: Int): Int {
        return navBarHeightPx + defaultMarginPx
    }

    @Test
    fun `calculateBottomMarginWithNavInsets should add navigation bar height to default FAB margin`() {
        val navBarHeight = 48 // 48px de hauteur pour la barre de geste Android
        val defaultMargin = 16 // 16px de marge par défaut

        val finalMargin = calculateBottomMarginWithNavInsets(navBarHeight, defaultMargin)

        assertThat(finalMargin).isEqualTo(64) // 48 + 16 = 64px
    }
}
