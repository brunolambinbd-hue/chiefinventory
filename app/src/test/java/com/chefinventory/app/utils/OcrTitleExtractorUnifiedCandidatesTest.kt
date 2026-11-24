package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 2.5, Ocr3CategorizerTitleExtractor
 * évalue de manière unifiée les candidats bannières (Source A) et intra-colonnes (Source B) avec les mots d'ingrédients,
 * élit le vrai titre et restitue les bannières rejetées dans les lignes restantes (remainingLines).
 */
class OcrTitleExtractorUnifiedCandidatesTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitle_UnifiedCandidatesEvaluation_ElectsBestTitleAndRestoresRejectedBanners() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val lines = listOf(
            "Büchettes de melon au jambon italien et au porto",
            "1 melon Galia",
            "8 tranches de jambon",
            "4 c. à soupe de porto",
            "• Pelez le melon, épépinez-le et coupez la chair.",
            "• Mixez le porto, le vinaigre et le yoghourt."
        )

        val offTaxonomy = listOf("melon", "jambon", "porto", "vinaigre", "yoghourt")
        val titleResult = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocab, emptyList(), offTaxonomy)

        assertWithMessage("Le vrai titre 'Bûchettes de melon au jambon italien et au porto' doit être élu avec le score maximal")
            .that(titleResult.title)
            .ignoringCase()
            .contains("Bûchettes de melon")

        assertWithMessage("Les lignes restantes (cleanedLines) doivent conserver les ingrédients et consignes")
            .that(titleResult.cleanedLines)
            .contains("1 melon Galia")

        assertWithMessage("Le titre élu ne doit plus figurer dans les lignes restantes")
            .that(titleResult.cleanedLines)
            .doesNotContain("Büchettes de melon au jambon italien et au porto")
    }
}
