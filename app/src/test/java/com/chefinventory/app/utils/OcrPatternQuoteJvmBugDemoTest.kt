package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.regex.Pattern

/**
 * Test unitaire d'isolation TDD démontrant la résolution du bogue JDK-8032926
 * (Incapacité de (?i) à matcher en majuscules à l'intérieur d'un bloc Pattern.quote \Q...\E sur JVM).
 */
class OcrPatternQuoteJvmBugDemoTest {

    @Test
    fun testRegexEscape_SucceedsOnJvmForUppercaseMatch() {
        val word = "salade"
        val uppercaseInput = "SALADE DE LANGOUSTINES AU CURRY"

        // Construction avec Regex.escape (produit (?i)(?<!\p{L})salade(?!\p{L}))
        val regexEscaped = Regex.escape(word.lowercase())
        val patternStrWithEscape = "(?i)(?<!\\p{L})$regexEscaped(?!\\p{L})"
        val patternWithEscape = Pattern.compile(patternStrWithEscape, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)

        val matcherEscape = patternWithEscape.matcher(uppercaseInput)

        // Avec Regex.escape, (?i) matche 100% des majuscules sur la JVM !
        assertThat(matcherEscape.find()).isTrue()
    }
}
