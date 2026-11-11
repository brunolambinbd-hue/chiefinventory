package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class RecipeOcrAubergineRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `exhaustive reference test for recipe 01 salade aubergines`() {
        val ocrText = """
            CONRAD|
            BR
            Nahit YILMAZ
            Hôtel Conrad International
            avenue Louise 7I
            1050 BRUXELLES
            Tél. : 02/542.42.42
            saveur de
            alade d'aubergines
            1 Plongez les aubergines dans l'eau bouillante et retirez la peau. Hachez la chair et faites-la suer dans
            un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et
            le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.
            23
            20
            Ingrédients pour 4 personnes
            6 aubergines moyennes (t 1 kg)
            1 ou 2 citrons
            150 g de beurre
            I piment rouge
            persil haché
            2 gousses d'ail
            sel, poivre
            Pour la décoration: tomates
            Notre vin conseillé
            Rousse Région Merlot
            (vìn rouge de Bulgarie)
            M.O.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val title = result.title ?: ""
        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""
        val wine = result.wine ?: ""
        val source = result.source ?: ""

        // ==========================================
        // 1. VALIDATION ET EXCLUSIVITÉ DU TITRE
        // ==========================================
        assertWithMessage("Le titre extrait doit contenir 'Salade d'aubergines'")
            .that(title)
            .ignoringCase()
            .contains("Salade d'aubergines")

        assertWithMessage("Le titre ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Salade d'aubergines")

        assertWithMessage("Le titre ne doit pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Salade d'aubergines")

        assertWithMessage("Le titre ne doit pas fuiter dans le vin")
            .that(wine)
            .ignoringCase()
            .doesNotContain("Salade d'aubergines")

        assertWithMessage("Le titre ne doit pas fuiter dans la source")
            .that(source)
            .ignoringCase()
            .doesNotContain("Salade d'aubergines")

        // ==========================================
        // 2. VALIDATION ET EXCLUSIVITÉ DES INGRÉDIENTS (Lignes avec quantité)
        // ==========================================
        val expectedIngredientsWithQty = listOf(
            "6 aubergines moyennes",
            "1 ou 2 citrons",
            "150 g de beurre",
            "1 piment rouge",
            "2 gousses d'ail"
        )

        for (item in expectedIngredientsWithQty) {
            assertWithMessage("L'ingrédient '$item' doit être extrait dans la liste des ingrédients")
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

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans la source")
                .that(source)
                .ignoringCase()
                .doesNotContain(item)
        }

        assertWithMessage("L'ingrédient 'persil haché' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("persil haché")

        assertWithMessage("L'assaisonnement 'sel' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("sel")

        assertWithMessage("L'assaisonnement 'poivre' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("poivre")

        // ==========================================
        // 3. VALIDATION EXACTE DES INSTRUCTIONS
        // ==========================================
        val expectedInstructionLines = listOf(
            "Plongez les aubergines dans l'eau bouillante et retirez la peau.",
            "Hachez la chair et faites-la suer dans un peu de beurre avec l'ail haché et le piment émincé fin.",
            "Assaisonnez.",
            "Ajoutez le jus de citron et le persil haché.",
            "Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.",
            "Pour la décoration: tomates."
        )

        for (line in expectedInstructionLines) {
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
        // 4. METADONNÉES (Portions, Vin, Source)
        // ==========================================
        assertWithMessage("Le nombre de portions extrait doit être de 4")
            .that(result.servings)
            .isEqualTo("4")

        assertWithMessage("Le vin doit contenir 'Rousse Région Merlot'")
            .that(wine)
            .ignoringCase()
            .contains("Rousse Région Merlot")

        assertWithMessage("Le vin doit contenir 'Bulgarie'")
            .that(wine)
            .ignoringCase()
            .contains("Bulgarie")

        assertWithMessage("Le nom de vin 'Merlot' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Merlot")

        assertWithMessage("Le nom de vin 'Merlot' ne doit pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Merlot")

        assertWithMessage("La source doit contenir le chef 'Nahit YILMAZ'")
            .that(source)
            .ignoringCase()
            .contains("Nahit YILMAZ")

        assertWithMessage("La source doit contenir le téléphone '02/542.42.42'")
            .that(source)
            .contains("02/542.42.42")

        assertWithMessage("La source 'Nahit YILMAZ' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Nahit YILMAZ")

        assertWithMessage("La source 'Nahit YILMAZ' ne doit pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Nahit YILMAZ")
    }
}
