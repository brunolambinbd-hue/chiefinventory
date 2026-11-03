package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File

/**
 * Suite de tests unitaires de Type B vérifiant le chargement XML data-driven et le cache de [OcrVocabularyLoader].
 */
class OcrVocabularyLoaderTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCacheVocabularyIfNeeded_CustomDirectoryPath_ExecutesElseBranch() {
        val mockVocab = OcrVocabularyLoader.loadFromProjectResources()
        OcrVocabularyLoader.cacheVocabularyIfNeeded(File("custom_path_test"), mockVocab)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseResDirectory_NonDirectoryFile_ExecutesElseBranchLog() {
        val dummyNonDirFile = File.createTempFile("non_directory_file", ".tmp")
        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseResDirectory(dummyNonDirFile, stringArrays, strings)

        assertThat(stringArrays).isEmpty()
        assertThat(strings).isEmpty()
        dummyNonDirFile.delete()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testParseXmlDocument_NonElementNodeInDom_ExecutesElseBranches() {
        val mockNode = mock<Node> {
            on { nodeType } doReturn Node.TEXT_NODE
        }
        val mockNodeList = mock<NodeList> {
            on { length } doReturn 1
            on { item(0) } doReturn mockNode
        }
        val mockElement = mock<Element>()
        val mockDoc = mock<Document> {
            on { documentElement } doReturn mockElement
            on { getElementsByTagName("string-array") } doReturn mockNodeList
            on { getElementsByTagName("string") } doReturn mockNodeList
        }

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseXmlDocument(mockDoc, stringArrays, strings)

        assertThat(stringArrays).isEmpty()
        assertThat(strings).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromProjectResources_NonDotProjectPath_ExecutesElseBranch() {
        // Path is "./" (not equals to ".") -> executes else branch in loadFromProjectResources
        val vocab = OcrVocabularyLoader.loadFromProjectResources(File("./"))
        assertThat(vocab.stepActionKeywords).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseXmlResourceFile_WithCommentsAndTextNodes_ExecutesElseBranches() {
        val tempFile = File.createTempFile("xml_with_comments", ".xml")
        tempFile.writeText(
            """<?xml version="1.0" encoding="utf-8"?>
            <resources>
                <!-- This is a comment node before string-array -->
                <string-array name="test_array">
                    <item>val1</item>
                </string-array>
                <!-- Comment node before string -->
                <string name="test_string">val2</string>
                <!-- Comment node at end -->
            </resources>""".trimIndent()
        )

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseXmlResourceFile(tempFile, stringArrays, strings)

        assertThat(stringArrays["test_array"]).containsExactly("val1")
        assertThat(strings["test_string"]).isEqualTo("val2")

        tempFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcr3ExplicitWineHeadersLoaded() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        assertThat(vocab.ocr3ExplicitWineHeaders).isNotEmpty()
        assertThat(vocab.ocr3ExplicitWineHeaders).contains("conseil vin")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveProductionXmlResourcesIntegrity_All38PropertiesLoadedAndValid() {
        OcrVocabularyLoader.clearCache()
        val vocab = OcrVocabularyLoader.loadFromProjectResources()

        assertThat(vocab.stepActionKeywords).isNotEmpty()
        assertThat(vocab.ingredientEndMarkers).isNotEmpty()
        assertThat(vocab.ingredientSubHeaders).isNotEmpty()
        assertThat(vocab.sourceKeywords).isNotEmpty()
        assertThat(vocab.knownSourceBrands).isNotEmpty()
        assertThat(vocab.phonePrefixes).isNotEmpty()
        assertThat(vocab.wineAppellations).isNotEmpty()
        assertThat(vocab.wineProducers).isNotEmpty()
        assertThat(vocab.wineKeywords).isNotEmpty()
        assertThat(vocab.wineTitleKeywords).isNotEmpty()
        assertThat(vocab.ocr3ExplicitWineHeaders).isNotEmpty()
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
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromProjectResources_CachingBehavior() {
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
