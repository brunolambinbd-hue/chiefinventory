package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
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
        assertThat(title).ignoringCase().contains("Salade d'aubergines")
        assertThat(ing).ignoringCase().doesNotContain("Salade d'aubergines")
        assertThat(inst).ignoringCase().doesNotContain("Salade d'aubergines")
        assertThat(wine).ignoringCase().doesNotContain("Salade d'aubergines")
        assertThat(source).ignoringCase().doesNotContain("Salade d'aubergines")

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
            assertThat(ing).ignoringCase().contains(item)
            assertThat(inst).ignoringCase().doesNotContain(item)
            assertThat(title).ignoringCase().doesNotContain(item)
            assertThat(wine).ignoringCase().doesNotContain(item)
            assertThat(source).ignoringCase().doesNotContain(item)
        }

        assertThat(ing).ignoringCase().contains("persil haché")
        assertThat(ing).ignoringCase().contains("sel")
        assertThat(ing).ignoringCase().contains("poivre")

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
            assertThat(inst).ignoringCase().contains(line)
            assertThat(ing).ignoringCase().doesNotContain(line)
        }

        // ==========================================
        // 4. METADONNÉES (Portions, Vin, Source)
        // ==========================================
        assertThat(result.servings).isEqualTo("4")

        assertThat(wine).ignoringCase().contains("Rousse Région Merlot")
        assertThat(wine).ignoringCase().contains("Bulgarie")
        assertThat(ing).ignoringCase().doesNotContain("Merlot")
        assertThat(inst).ignoringCase().doesNotContain("Merlot")

        assertThat(source).ignoringCase().contains("Nahit YILMAZ")
        assertThat(source).contains("02/542.42.42")
        assertThat(ing).ignoringCase().doesNotContain("Nahit YILMAZ")
        assertThat(inst).ignoringCase().doesNotContain("Nahit YILMAZ")
    }
}
