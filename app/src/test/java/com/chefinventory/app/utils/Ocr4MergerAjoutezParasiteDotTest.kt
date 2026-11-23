package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 4 (Merger), un point parasite après un verbe transitif comme 'ajoutez.' devant une suite de phrase est éliminé et la phrase fusionnée.
 */
class Ocr4MergerAjoutezParasiteDotTest {

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
    fun testMergeInstructions_AjoutezWithParasiteDot_MergesCorrectlyInPhase4() {
        val rawInstructions = listOf(
            "Délayez le curry avec la mayonnaise, puis ajoutez. le yaourt et",
            "un trait de jus de citron. Salez et poivrez; réservez."
        )

        val mergedInstructions = Ocr4Merger.mergeInstructions(rawInstructions)

        assertWithMessage("Le point parasite après 'ajoutez.' doit être éliminé en Phase 4 et la phrase fusionnée sans coupure")
            .that(mergedInstructions)
            .containsExactly(
                "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et un trait de jus de citron.",
                "Salez et poivrez; réservez."
            )
    }
}
