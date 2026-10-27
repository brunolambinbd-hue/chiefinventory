package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.Normalizer

/**
 * Suite de tests unitaires de Type B vérifiant la recherche textuelle en minuscules (lowercase),
 * insensible à la casse, aux accents et aux ligatures.
 *
 * @see REQ-4.9.1.1
 */
class SearchLowercaseFilterTypeBTest {

    private fun normalizeForSearch(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase()
            .trim()
    }

    private fun matchesSearch(targetText: String, searchQuery: String): Boolean {
        val cleanTarget = normalizeForSearch(targetText)
        val cleanQuery = normalizeForSearch(searchQuery)
        return cleanQuery.isBlank() || cleanTarget.contains(cleanQuery)
    }

    @Test
    fun `search should match uppercase query against lowercase target name`() {
        assertThat(matchesSearch("Lait demi-écrémé", "ÉCRÉMÉ")).isTrue()
        assertThat(matchesSearch("Lait demi-écrémé", "ecreme")).isTrue()
        assertThat(matchesSearch("Chicons / Endives", "CHICONS")).isTrue()
        assertThat(matchesSearch("Œufs", "OEUFS")).isTrue()
    }

    @Test
    fun `search should match lowercase query against uppercase or mixed case recipe title`() {
        assertThat(matchesSearch("Roulade de jambon de Parme", "jambon")).isTrue()
        assertThat(matchesSearch("Roulade de jambon de Parme", "PARME")).isTrue()
        assertThat(matchesSearch("TARTE AUX CHICONS", "chicons")).isTrue()
    }

    @Test
    fun `search should match query against recipe ingredient name regardless of case`() {
        val recipeIngredientName = "3 paquets de jambon de Parme en fines tranches (125 à 180 g)"
        assertThat(matchesSearch(recipeIngredientName, "JAMBON")).isTrue()
        assertThat(matchesSearch(recipeIngredientName, "parme")).isTrue()
        assertThat(matchesSearch(recipeIngredientName, "tranches")).isTrue()
    }
}
