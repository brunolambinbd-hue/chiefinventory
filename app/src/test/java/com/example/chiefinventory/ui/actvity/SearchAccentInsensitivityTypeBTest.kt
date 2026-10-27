package com.example.chiefinventory.ui.actvity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.Normalizer

/**
 * Suite de tests unitaires de Type B vérifiant l'insensibilité aux accents et ligatures dans la recherche d'ingrédients.
 */
class SearchAccentInsensitivityTypeBTest {

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    @Test
    fun `stripAccents should match query with accent against target without accent and vice versa`() {
        // Taper "creme" retrouve "Crème fraîche"
        assertThat(stripAccents("Crème fraîche").contains(stripAccents("creme"))).isTrue()

        // Taper "crème" retrouve "Crème fraîche"
        assertThat(stripAccents("Crème fraîche").contains(stripAccents("crème"))).isTrue()

        // Taper "oeufs" retrouve "Œufs"
        assertThat(stripAccents("Œufs").contains(stripAccents("oeufs"))).isTrue()

        // Taper "echalote" retrouve "Échalotes"
        assertThat(stripAccents("Échalotes").contains(stripAccents("echalote"))).isTrue()
    }
}
