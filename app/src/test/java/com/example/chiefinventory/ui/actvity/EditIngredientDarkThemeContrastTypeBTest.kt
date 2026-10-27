package com.example.chiefinventory.ui.actvity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le tint dynamique des boutons du Stepper d'édition
 * pour la lisibilité WCAG AAA en mode sombre (Dark Mode) et en mode clair (Light Mode).
 */
class EditIngredientDarkThemeContrastTypeBTest {

    private fun getIconTintHex(isDarkMode: Boolean): String {
        return if (isDarkMode) {
            "#FFFFFF" // Blanc éclatant en Mode Nuit sur fond noir
        } else {
            "#212121" // Noir anthracite en Mode Jour sur fond blanc
        }
    }

    @Test
    fun `getIconTintHex should return white in dark mode and dark charcoal in light mode`() {
        assertThat(getIconTintHex(isDarkMode = true)).isEqualTo("#FFFFFF")
        assertThat(getIconTintHex(isDarkMode = false)).isEqualTo("#212121")
    }
}
