package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer0_Context].
 */
class Ocr3Categorizer0_ContextTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlock0_CategorizationContext_FullCoverageAndGeneratedDataClassMethods() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val ctx = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)

        assertThat(ctx.actionVerbs).isNotEmpty()
        assertThat(ctx.commonIngredients).isNotEmpty()
        assertThat(ctx.excludedKeywords).isNotEmpty()
        assertThat(ctx.preparationModifiers).isNotEmpty()
        assertThat(ctx.instructionTriggers).isNotEmpty()
        assertThat(ctx.semanticExclusions).isNotEmpty()
        assertThat(ctx.endMarkers).isNotEmpty()
        assertThat(ctx.subHeaders).isNotEmpty()
        assertThat(ctx.ingredientHeaders).isNotEmpty()
        assertThat(ctx.instructionHeaders).isNotEmpty()
        assertThat(ctx.hardQtyRegex).isNotNull()
        assertThat(ctx.qtyRegex).isNotNull()
        assertThat(ctx.containsActionRegex).isNotNull()
        assertThat(ctx.ingredientConnectors).isNotEmpty()

        // Test Data class generated methods (componentN, copy, equals, hashCode, toString)
        val copyCtx = ctx.copy()
        assertThat(copyCtx).isEqualTo(ctx)
        assertThat(copyCtx.hashCode()).isEqualTo(ctx.hashCode())
        assertThat(copyCtx.toString()).contains("CategorizationContext")

        val (action, common, excl, prep, instTrig, semExcl, endM, subH, ingrH, instH, hardQ, qty, containsA, ingrConn) = ctx
        assertThat(action).isEqualTo(ctx.actionVerbs)
        assertThat(common).isEqualTo(ctx.commonIngredients)
        assertThat(excl).isEqualTo(ctx.excludedKeywords)
        assertThat(prep).isEqualTo(ctx.preparationModifiers)
        assertThat(instTrig).isEqualTo(ctx.instructionTriggers)
        assertThat(semExcl).isEqualTo(ctx.semanticExclusions)
        assertThat(endM).isEqualTo(ctx.endMarkers)
        assertThat(subH).isEqualTo(ctx.subHeaders)
        assertThat(ingrH).isEqualTo(ctx.ingredientHeaders)
        assertThat(instH).isEqualTo(ctx.instructionHeaders)
        assertThat(hardQ).isEqualTo(ctx.hardQtyRegex)
        assertThat(qty).isEqualTo(ctx.qtyRegex)
        assertThat(containsA).isEqualTo(ctx.containsActionRegex)
        assertThat(ingrConn).isEqualTo(ctx.ingredientConnectors)
    }
}
