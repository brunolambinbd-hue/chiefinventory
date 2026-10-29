package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le parsing et l'association des images de recettes
 * hébergées localement dans les assets de l'application (file:///android_asset/recipe_images/...).
 */
class LocalAssetRecipeImageLoadingTypeBTest {

    @Test
    fun `parseSeedRecipeJsonObject should extract local asset imageUri`() {
        val jsonString = """
            {
              "title": "Chicons au gratin",
              "category": "Plat principal",
              "cuisineOrigin": "Cuisine Belge",
              "imageUrl": "file:///android_asset/recipe_images/chicons_au_gratin.jpg",
              "ingredients": ["4 chicons", "4 tranches de jambon"],
              "instructions": ["Cuire au four"]
            }
        """.trimIndent()

        val jsonObject = JSONObject(jsonString)
        val seed = StarterRecipeSeedLoader.parseSeedRecipeJsonObject(jsonObject)

        assertThat(seed).isNotNull()
        assertThat(seed!!.title).isEqualTo("Chicons au gratin")
        assertThat(seed.sourceUrl).isNull()
    }
}
