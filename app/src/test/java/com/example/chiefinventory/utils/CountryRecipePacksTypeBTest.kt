package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le chargement et le parsing des packs de recettes par pays
 * (ex: Belgique BE, France FR, Italie IT).
 */
class CountryRecipePacksTypeBTest {

    @Test
    fun `parseSeedRecipeJsonObject should parse country pack recipe attributes`() {
        val jsonIt = """
            {
              "title": "Osso bucco",
              "category": "Plat principal",
              "cuisineOrigin": "Cuisine Italienne",
              "prepTimeMinutes": 20,
              "cookTimeMinutes": 90,
              "servings": 4,
              "ingredients": [
                "1.5 kg de jarret de veau",
                "2 carottes",
                "1 verre de vin blanc"
              ],
              "instructions": [
                "Faire dorer la viande dans une cocotte.",
                "Ajouter les carottes et le vin blanc."
              ],
              "sourceUrl": "https://fr.wikibooks.org/wiki/Osso_bucco"
            }
        """.trimIndent()

        val obj = JSONObject(jsonIt)
        val seed = StarterRecipeSeedLoader.parseSeedRecipeJsonObject(obj)

        assertThat(seed).isNotNull()
        assertThat(seed!!.title).isEqualTo("Osso bucco")
        assertThat(seed.cuisineOrigin).isEqualTo("Cuisine Italienne")
        assertThat(seed.category).isEqualTo("Plat principal")
        assertThat(seed.ingredients).hasSize(3)
        assertThat(seed.instructions).hasSize(2)
    }
}
