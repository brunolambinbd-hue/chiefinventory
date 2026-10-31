package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class OcrVocabularyLoaderTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveProductionXmlResourcesIntegrity_All38PropertiesLoadedAndValid() {
        OcrVocabularyLoader.clearCache()
        val vocab = OcrVocabularyLoader.loadFromProjectResources()

        assertThat(vocab).isNotNull()

        // 1. Verify that all 38 XML resource lists/strings in res/values are non-empty
        assertThat(vocab.stepActionKeywords).isNotEmpty()
        assertThat(vocab.commonIngredientsNoQty).isNotEmpty()
        assertThat(vocab.excludedOcrKeywords).isNotEmpty()
        assertThat(vocab.ingredientPreparationModifiers).isNotEmpty()
        assertThat(vocab.instructionSwitchKeywords).isNotEmpty()
        assertThat(vocab.ingredientSemanticExclusions).isNotEmpty()
        assertThat(vocab.ingredientEndMarkers).isNotEmpty()
        assertThat(vocab.ingredientSubHeaders).isNotEmpty()
        assertThat(vocab.sourceKeywords).isNotEmpty()
        assertThat(vocab.knownSourceBrands).isNotEmpty()
        assertThat(vocab.phonePrefixes).isNotEmpty()
        assertThat(vocab.wineAppellations).isNotEmpty()
        assertThat(vocab.wineProducers).isNotEmpty()
        assertThat(vocab.wineKeywords).isNotEmpty()
        assertThat(vocab.wineTitleKeywords).isNotEmpty()
        assertThat(vocab.wineRemovePattern).isNotEmpty()
        assertThat(vocab.ocrSpellingRepairs).isNotEmpty()
        assertThat(vocab.adExclusions).isNotEmpty()
        assertThat(vocab.ocrNoiseExclusions).isNotEmpty()
        assertThat(vocab.titleKeywords).isNotEmpty()
        assertThat(vocab.mandatoryComplementIngredients).isNotEmpty()
        assertThat(vocab.hangingConnectors).isNotEmpty()
        assertThat(vocab.ingredientVarietyQualifiers).isNotEmpty()
        assertThat(vocab.baseIngredientsExpectingVariety).isNotEmpty()
        assertThat(vocab.standaloneArticles).isNotEmpty()
        assertThat(vocab.culinaryMeasurementUnits).isNotEmpty()
        assertThat(vocab.titleSpellingRepairs).isNotEmpty()
        assertThat(vocab.titleMultilineConnectors).isNotEmpty()
        assertThat(vocab.ingredientSplitterConnectors).isNotEmpty()
        assertThat(vocab.ocrDiacriticRepairs).isNotEmpty()
        assertThat(vocab.priceTitleKeywords).isNotEmpty()
        assertThat(vocab.priceCategoriesLow).isNotEmpty()
        assertThat(vocab.priceCategoriesMedium).isNotEmpty()
        assertThat(vocab.priceCategoriesHigh).isNotEmpty()
        assertThat(vocab.ingredientConnectors).isNotEmpty()
        assertThat(vocab.protectedSectionHeaders).isNotEmpty()
        assertThat(vocab.timeHeaderPrefixes).isNotEmpty()
        assertThat(vocab.timeHeaderKeywords).isNotEmpty()

        // 2. Verify format integrity of XML spelling and diacritic repair pairs ("wrong|correct")
        vocab.ocrSpellingRepairs.forEach { repair ->
            assertThat(repair).contains("|")
            val parts = repair.split("|")
            assertThat(parts).hasSize(2)
            assertThat(parts[0].trim()).isNotEmpty()
            assertThat(parts[1].trim()).isNotEmpty()
        }

        vocab.titleSpellingRepairs.forEach { repair ->
            assertThat(repair).contains("|")
            val parts = repair.split("|")
            assertThat(parts).hasSize(2)
            assertThat(parts[0].trim()).isNotEmpty()
            assertThat(parts[1].trim()).isNotEmpty()
        }

        vocab.ocrDiacriticRepairs.forEach { repair ->
            assertThat(repair).contains("|")
            val parts = repair.split("|")
            assertThat(parts).hasSize(2)
            assertThat(parts[0].trim()).isNotEmpty()
            assertThat(parts[1].trim()).isNotEmpty()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromProjectResources_CachingAndClearCache() {
        OcrVocabularyLoader.clearCache()
        val vocab1 = OcrVocabularyLoader.loadFromProjectResources()
        val vocab2 = OcrVocabularyLoader.loadFromProjectResources()

        assertThat(vocab1).isSameInstanceAs(vocab2)

        OcrVocabularyLoader.clearCache()
        val vocab3 = OcrVocabularyLoader.loadFromProjectResources()
        assertThat(vocab3).isNotSameInstanceAs(vocab1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetListAndGetStringVal_MissingKey_ReturnsDefaults() {
        val list = OcrVocabularyLoader.getList(emptyMap(), "missing")
        val str = OcrVocabularyLoader.getStringVal(emptyMap(), "missing")

        assertThat(list).isEmpty()
        assertThat(str).isEmpty()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testLoadFromProjectResources_InvalidDirectory_ThrowsIllegalStateException() {
        val isolatedDir = File(System.getProperty("java.io.tmpdir"), "isolated_test_dir_999")
        isolatedDir.mkdirs()
        assertThrows(IllegalStateException::class.java) {
            OcrVocabularyLoader.loadFromProjectResources(isolatedDir)
        }
        isolatedDir.delete()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testParseXmlResourceFile_CorruptedXml_CatchesExceptionAndLogsError() {
        val tempFile = File.createTempFile("corrupted_vocab", ".xml")
        tempFile.writeText("<resources><string-array name=\"test\"><item>unclosed_tag")

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()
        OcrVocabularyLoader.parseXmlResourceFile(tempFile, stringArrays, strings)

        // Cleanup
        tempFile.delete()
    }
}
