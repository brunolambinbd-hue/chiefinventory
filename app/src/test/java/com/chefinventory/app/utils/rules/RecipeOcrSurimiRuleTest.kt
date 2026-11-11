package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class RecipeOcrSurimiRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `exhaustive reference test for recipe 02 salade de surimi`() {
        val ocrText = """
            ingrédients
            POUR
            4 PERSONNES
            120 g de surimi (poisson
            au goût de crabe) ou
            de véritable crabe
            2 c. à soupe
            de mayonnaise
            1 c. à soupe de câpres
            2 c. à soupe
            de persil haché
            2 œufs durs
            4 toasts
            salade de blé
            le jus de 1/2 citron
            sel et poivre
            Salade de surimi
            • Hachez finement le surimi ou
            le crabe ainsi que les œufs
            durs.
            • Mélangez avec la
            mayonnaise, le persil, le jus de
            citron, les câpres, du sel et du
            poivre.
            • Disposez un peu de salade de
            blé en forme de cercle sur une
            assiette et déposez-y la salade
            de surimi.
            Assiette de présentation disponible dans les Maxi GB
            • Servez avec les toasts.
            VIN CONSEILLÉ
            Atinum, Bordeaux blanc
            MO, 1996, 75 cl
            N
            4 tinlin
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val title = result.title ?: ""
        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""
        val wine = result.wine ?: ""

        // ==========================================
        // 1. VALIDATION ET EXCLUSIVITÉ DU TITRE
        // ==========================================
        assertWithMessage("Le titre extrait doit contenir 'Salade de surimi'")
            .that(title)
            .ignoringCase()
            .contains("Salade de surimi")

        assertWithMessage("Le titre ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Salade de surimi")

        assertWithMessage("Le titre ne doit pas fuiter dans le vin")
            .that(wine)
            .ignoringCase()
            .doesNotContain("Salade de surimi")

        // ==========================================
        // 2. VALIDATION ET EXCLUSIVITÉ DES INGRÉDIENTS (Lignes avec quantité)
        // ==========================================
        val expectedIngredientsWithQty = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de câpres",
            "2 c. à soupe de persil haché",
            "2 œufs durs",
            "4 toasts"
        )

        for (item in expectedIngredientsWithQty) {
            assertWithMessage("L'ingrédient '$item' doit figurer dans les ingrédients")
                .that(ing)
                .ignoringCase()
                .contains(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans les instructions")
                .that(inst)
                .ignoringCase()
                .doesNotContain(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans le titre")
                .that(title)
                .ignoringCase()
                .doesNotContain(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans le vin")
                .that(wine)
                .ignoringCase()
                .doesNotContain(item)
        }

        assertWithMessage("L'ingrédient 'Salade de blé' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("Salade de blé")

        assertWithMessage("L'ingrédient 'jus de 1/2 citron' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("jus de 1/2 citron")

        assertWithMessage("L'assaisonnement 'sel et poivre' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("sel et poivre")

        // ==========================================
        // 3. VALIDATION ET EXCLUSIVITÉ DES INSTRUCTIONS
        // ==========================================
        val expectedInstructions = listOf(
            "Hachez finement le surimi ou le crabe ainsi que les œufs durs.",
            "Mélangez avec la mayonnaise, le persil, le jus de citron, les câpres, du sel et du poivre.",
            "Disposez un peu de salade de blé en forme de cercle sur une assiette et déposez-y la salade de surimi.",
            "Servez avec les toasts."
        )

        for (line in expectedInstructions) {
            assertWithMessage("La consigne '$line' doit figurer dans les instructions")
                .that(inst)
                .ignoringCase()
                .contains(line)

            assertWithMessage("La consigne '$line' ne doit pas fuiter dans les ingrédients")
                .that(ing)
                .ignoringCase()
                .doesNotContain(line)
        }

        // ==========================================
        // 4. METADONNÉES (Portions, Vin)
        // ==========================================
        assertWithMessage("Le nombre de portions doit valoir '4'")
            .that(result.servings)
            .isEqualTo("4")

        assertWithMessage("Le vin doit contenir la marque 'Atinium'")
            .that(wine)
            .ignoringCase()
            .contains("Atinium")

        assertWithMessage("Le vin doit contenir l'appellation 'Bordeaux blanc'")
            .that(wine)
            .ignoringCase()
            .contains("Bordeaux blanc")

        assertWithMessage("Le nom de vin 'Bordeaux' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Bordeaux")

        assertWithMessage("Le nom de vin 'Bordeaux' ne doit pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Bordeaux")
    }
}
