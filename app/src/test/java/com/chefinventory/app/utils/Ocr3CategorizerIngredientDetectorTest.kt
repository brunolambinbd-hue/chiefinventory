package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerIngredientDetector].
 */
class Ocr3CategorizerIngredientDetectorTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCountIngredientSequences_ExhaustiveBranches() {
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("")).isEqualTo(0)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("   ")).isEqualTo(0)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g 100")).isEqualTo(1)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("2 Carottes 1 Pomme")).isEqualTo(2)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("2 Carottes")).isEqualTo(1)

        // Inside and outside parentheses branches (L35)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g (100 g de sucre)")).isEqualTo(1)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g de farine 100 g de sucre")).isEqualTo(2)

        // Excluded next words branch (L38, L40)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("10 min")).isEqualTo(0)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("10 20")).isEqualTo(0)
        assertThat(Ocr3CategorizerIngredientDetector.countIngredientSequences("100 , ; .")).isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanIngredientSemantics_ExhaustiveBranches() {
        // Line length / no letters (L60)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("", emptyList())).isEmpty()
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("   ", emptyList())).isEmpty()
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("!", emptyList())).isEmpty()
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("!!!", emptyList())).isEmpty()
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("a", emptyList())).isEmpty()

        // Proper name detection (L61)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("MICHEL LAROCHE", emptyList())).isEmpty()

        // Excluded keywords (L63)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("PAGE", listOf("page"))).isEmpty()

        // Semantic exclusions (L64)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("BIO", emptyList(), listOf("bio"))).isEmpty()

        // Standalone articles (L67, L70)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("du", emptyList())).isEmpty()
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("le", emptyList(), emptyList(), listOf("le"))).isEmpty()

        // Valid ingredient line (single word and multi-word)
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("• Tomate", emptyList())).isEqualTo("Tomate")
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("- Oignon", emptyList())).isEqualTo("Oignon")
        assertThat(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("Tomate cerise", emptyList())).isEqualTo("Tomate cerise")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasExplicitWeight() {
        assertThat(Ocr3CategorizerIngredientDetector.hasExplicitWeight("200 g de farine")).isTrue()
        assertThat(Ocr3CategorizerIngredientDetector.hasExplicitWeight("5 cl d'huile")).isTrue()
        assertThat(Ocr3CategorizerIngredientDetector.hasExplicitWeight("Sel et poivre")).isFalse()
    }
}
