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
 * Suite de tests unitaires de Type B vérifiant le comportement et la précision de [SourceParser].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SourceParserTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadResources_LoadsFromAndroidResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val sourceRes = SourceParser.loadResources(context.resources)
        assertThat(sourceRes.sourceKeywords).isNotEmpty()
        assertThat(sourceRes.phonePrefixes).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_ValidPhoneNumbers() {
        assertThat(SourceParser.isSourceLine("02/542.42.42", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("+32 2 542 42 42", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("Tel: 01 45 67 89 10", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("06.12.34.56.78", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_SourceKeywords() {
        assertThat(SourceParser.isSourceLine("Restaurant La Bonne Fourchette", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("12 rue des Fleurs", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("Hôtel de Ville", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_ShortZipCodes() {
        assertThat(SourceParser.isSourceLine("75001 Paris", vocabulary)).isTrue()
        assertThat(SourceParser.isSourceLine("CP: 1000", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsSourceLine_NonSourceLines() {
        assertThat(SourceParser.isSourceLine("Couper les oignons", vocabulary)).isFalse()
        assertThat(SourceParser.isSourceLine("Sel et poivre", vocabulary)).isFalse()
        assertThat(SourceParser.isSourceLine("Voici une ligne longue de preparation avec un chiffre qui ne devrait pas etre consideree source.", vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsLikelyProperNameOrSource_TitlesWithFood() {
        assertThat(OcrHelperUtils.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary)).isFalse()
        assertThat(OcrHelperUtils.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary)).isFalse()

        assertThat(OcrHelperUtils.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary)).isTrue()
        assertThat(OcrHelperUtils.isLikelyProperNameOrSource("HÔTEL CONRAD", vocabulary)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanSourceLine_RemovesPhonePrefixes() {
        assertThat(SourceParser.cleanSourceLine("Tel: 01 23 45 67 89", vocabulary)).isEqualTo("01 23 45 67 89")
        assertThat(SourceParser.cleanSourceLine("Tél. 02 54 88 99", vocabulary)).isEqualTo("02 54 88 99")
        assertThat(SourceParser.cleanSourceLine("Fax 04 55", vocabulary)).isEqualTo("04 55")
        assertThat(SourceParser.cleanSourceLine("GSM: 0612345678", vocabulary)).isEqualTo("0612345678")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanSourceLine_DoesNotBreakWords() {
        assertThat(SourceParser.cleanSourceLine("Hôtel 012345", vocabulary)).isEqualTo("Hôtel 012345")
        assertThat(SourceParser.cleanSourceLine("Chantel 012345", vocabulary)).isEqualTo("Chantel 012345")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractSourceFromAdLine_ExtractsKnownBrandsAndHandlesBlank() {
        assertThat(SourceParser.extractSourceFromAdLine("Assiette de présentation disponible dans les Maxi GB", vocabulary)).isEqualTo("Maxi GB")
        assertThat(SourceParser.extractSourceFromAdLine("Recette proposée par les magasins Delhaize", vocabulary)).isEqualTo("Delhaize")
        assertThat(SourceParser.extractSourceFromAdLine("Extrait du magazine Cuisine Actuelle n°45", vocabulary)).isEqualTo("Cuisine Actuelle")

        assertThat(SourceParser.extractSourceFromAdLine("", vocabulary)).isNull()
        assertThat(SourceParser.extractSourceFromAdLine("   ", vocabulary)).isNull()
        assertThat(SourceParser.extractSourceFromAdLine("Assiette disponible dans les supermarchés", vocabulary)).isNull()
    }
}
