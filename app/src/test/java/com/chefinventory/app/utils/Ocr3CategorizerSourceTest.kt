package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et la précision de [Ocr3CategorizerSource].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerSourceTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertThat(Ocr3CategorizerSource.toString()).contains("Ocr3CategorizerSource")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_ZipCodeOnlyAndLongLines() {
        assertThat(Ocr3CategorizerSource.isSourceLine("1050 BRUXELLES", vocabulary)).isTrue()

        val longLineWithDigits = "Mélanger la préparation au four pendant 1050 secondes très longue phrase de recette"
        assertThat(Ocr3CategorizerSource.isSourceLine(longLineWithDigits, vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadResources_LoadsFromAndroidResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val sourceRes = Ocr3CategorizerSource.loadResources(context.resources)
        assertThat(sourceRes.sourceKeywords).isNotEmpty()
        assertThat(sourceRes.phonePrefixes).isNotEmpty()

        val cleanedWithRes = Ocr3CategorizerSource.cleanSourceLine("Tel: 01 23 45 67 89", sourceRes.phonePrefixes)
        assertThat(cleanedWithRes).isEqualTo("01 23 45 67 89")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_ValidPhoneNumbers() {
        assertThat(Ocr3CategorizerSource.isSourceLine("02/542.42.42", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("+32 2 542 42 42", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("Tel: 01 45 67 89 10", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("06.12.34.56.78", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_SourceKeywords() {
        assertThat(Ocr3CategorizerSource.isSourceLine("Restaurant La Bonne Fourchette", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("12 rue des Fleurs", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("Hôtel de Ville", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_ShortZipCodes() {
        assertThat(Ocr3CategorizerSource.isSourceLine("75001 Paris", vocabulary)).isTrue()
        assertThat(Ocr3CategorizerSource.isSourceLine("CP: 1000", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_NonSourceLines() {
        assertThat(Ocr3CategorizerSource.isSourceLine("Couper les oignons", vocabulary)).isFalse()
        assertThat(Ocr3CategorizerSource.isSourceLine("Sel et poivre", vocabulary)).isFalse()
        assertThat(Ocr3CategorizerSource.isSourceLine("Voici une ligne longue de preparation avec un chiffre qui ne devrait pas etre consideree source.", vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanSourceLine_RemovesPhonePrefixes() {
        assertThat(Ocr3CategorizerSource.cleanSourceLine("Tel: 01 23 45 67 89", vocabulary)).isEqualTo("01 23 45 67 89")
        assertThat(Ocr3CategorizerSource.cleanSourceLine("Tél. 02 54 88 99", vocabulary)).isEqualTo("02 54 88 99")
        assertThat(Ocr3CategorizerSource.cleanSourceLine("Fax 04 55", vocabulary)).isEqualTo("04 55")
        assertThat(Ocr3CategorizerSource.cleanSourceLine("GSM: 0612345678", vocabulary)).isEqualTo("0612345678")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanSourceLine_DoesNotBreakWords() {
        assertThat(Ocr3CategorizerSource.cleanSourceLine("Hôtel 012345", vocabulary)).isEqualTo("Hôtel 012345")
        assertThat(Ocr3CategorizerSource.cleanSourceLine("Chantel 012345", vocabulary)).isEqualTo("Chantel 012345")
    }
}
