package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'affichage du compteur de recettes filtrées
 * (ex: "34 recettes trouvées (12 faisables à 100%)").
 */
class MatchRecipeCountHeaderTypeBTest {

    private fun formatMatchCountHeader(totalCount: Int, readyCount: Int): String {
        if (totalCount == 0) return "Aucune recette trouvée"
        val totalStr = "$totalCount recette${if (totalCount > 1) "s" else ""} trouvée${if (totalCount > 1) "s" else ""}"
        val readyStr = "$readyCount faisable${if (readyCount > 1) "s" else ""} à 100%"
        return "$totalStr ($readyStr)"
    }

    @Test
    fun `formatMatchCountHeader should format plural counts correctly`() {
        assertThat(formatMatchCountHeader(34, 12)).isEqualTo("34 recettes trouvées (12 faisables à 100%)")
        assertThat(formatMatchCountHeader(1, 1)).isEqualTo("1 recette trouvée (1 faisable à 100%)")
        assertThat(formatMatchCountHeader(0, 0)).isEqualTo("Aucune recette trouvée")
    }
}
