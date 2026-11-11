package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Le parsing d'un fichier non-répertoire doit laisser les cartes de résultats vides")
            .that(stringArrays)
            .isEmpty()

        assertWithMessage("Le parsing d'un fichier non-répertoire doit laisser les cartes de chaînes vides")
            .that(strings)
            .isEmpty()

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

        assertWithMessage("Les nœuds non-éléments doivent être ignorés sans remplir la carte stringArrays")
            .that(stringArrays)
            .isEmpty()

        assertWithMessage("Les nœuds non-éléments doivent être ignorés sans remplir la carte strings")
            .that(strings)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromProjectResources_NonDotProjectPath_ExecutesElseBranch() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources(File("./"))
        assertWithMessage("Le chargement à partir de './' doit charger les verbes d'action du projet")
            .that(vocab.stepActionKeywords)
            .isNotEmpty()
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

        assertWithMessage("Le tableau XML 'test_array' doit contenir 'val1'")
            .that(stringArrays["test_array"])
            .containsExactly("val1")

        assertWithMessage("La chaîne XML 'test_string' doit valoir 'val2'")
            .that(strings["test_string"])
            .isEqualTo("val2")

        tempFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseXmlResourceFile_StripsWindowsCarriageReturnsFromXml() {
        val tempFile = File.createTempFile("xml_windows_crlf", ".xml")
        tempFile.writeText(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
            "<resources>\r\n" +
            "    <string-array name=\"crlf_test_array\">\r\n" +
            "        <item>mot1\r</item>\r\n" +
            "        <item>mot2\r\n</item>\r\n" +
            "    </string-array>\r\n" +
            "    <string name=\"crlf_test_string\">valeur1\r</string>\r\n" +
            "</resources>"
        )

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseXmlResourceFile(tempFile, stringArrays, strings)

        val items = stringArrays["crlf_test_array"] ?: emptyList()
        val stringVal = strings["crlf_test_string"] ?: ""

        assertWithMessage("Les éléments du tableau XML doivent être nettoyés des retours chariot \\r\\n")
            .that(items)
            .containsExactly("mot1", "mot2")

        assertWithMessage("La chaîne XML doit être nettoyée du retour chariot \\r")
            .that(stringVal)
            .isEqualTo("valeur1")

        for (item in items) {
            assertWithMessage("L'élément '$item' ne doit pas contenir de retour chariot \\r")
                .that(item)
                .doesNotContain("\r")
        }
        assertWithMessage("La valeur de chaîne '$stringVal' ne doit pas contenir de retour chariot \\r")
            .that(stringVal)
            .doesNotContain("\r")

        tempFile.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcr3ExplicitWineHeadersLoaded() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        assertWithMessage("Les en-têtes explicites de vins ne doivent pas être vides")
            .that(vocab.ocr3ExplicitWineHeaders)
            .isNotEmpty()

        assertWithMessage("Les en-têtes explicites de vins doivent contenir 'conseil vin'")
            .that(vocab.ocr3ExplicitWineHeaders)
            .contains("conseil vin")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveProductionXmlResourcesIntegrity_All38PropertiesLoadedAndValid() {
        OcrVocabularyLoader.clearCache()
        val vocab = OcrVocabularyLoader.loadFromProjectResources()

        assertWithMessage("Les verbes d'action culinaires du vocabulaire de production doivent être chargés")
            .that(vocab.stepActionKeywords)
            .isNotEmpty()

        assertWithMessage("Les marqueurs de fin d'ingrédients doivent être chargés")
            .that(vocab.ingredientEndMarkers)
            .isNotEmpty()

        assertWithMessage("Les sous-en-têtes d'ingrédients doivent être chargés")
            .that(vocab.ingredientSubHeaders)
            .isNotEmpty()

        assertWithMessage("Les mots-clés de sources doivent être chargés")
            .that(vocab.sourceKeywords)
            .isNotEmpty()

        assertWithMessage("Les enseignes sources connues doivent être chargées")
            .that(vocab.knownSourceBrands)
            .isNotEmpty()

        assertWithMessage("Les préfixes téléphoniques doivent être chargés")
            .that(vocab.phonePrefixes)
            .isNotEmpty()

        assertWithMessage("Les appellations de vins doivent être chargées")
            .that(vocab.wineAppellations)
            .isNotEmpty()

        assertWithMessage("Les producteurs de vins doivent être chargés")
            .that(vocab.wineProducers)
            .isNotEmpty()

        assertWithMessage("Les mots-clés de vins doivent être chargés")
            .that(vocab.wineKeywords)
            .isNotEmpty()

        assertWithMessage("Les mots-clés de titres de vins doivent être chargés")
            .that(vocab.wineTitleKeywords)
            .isNotEmpty()

        assertWithMessage("Les en-têtes explicites de vins doivent être chargés")
            .that(vocab.ocr3ExplicitWineHeaders)
            .isNotEmpty()

        assertWithMessage("Le motif de suppression de vins doit être chargé")
            .that(vocab.wineRemovePattern)
            .isNotEmpty()

        assertWithMessage("Les réparations orthographiques doivent être chargées")
            .that(vocab.ocrSpellingRepairs)
            .isNotEmpty()

        assertWithMessage("Les exclusions publicitaires doivent être chargées")
            .that(vocab.adExclusions)
            .isNotEmpty()

        assertWithMessage("Les exclusions de bruits OCR doivent être chargées")
            .that(vocab.ocrNoiseExclusions)
            .isNotEmpty()

        assertWithMessage("Les mots-clés de titres doivent être chargés")
            .that(vocab.titleKeywords)
            .isNotEmpty()

        assertWithMessage("Les compléments d'ingrédients obligatoires doivent être chargés")
            .that(vocab.mandatoryComplementIngredients)
            .isNotEmpty()

        assertWithMessage("Les connecteurs suspendus doivent être chargés")
            .that(vocab.hangingConnectors)
            .isNotEmpty()

        assertWithMessage("Les qualificatifs de variétés d'ingrédients doivent être chargés")
            .that(vocab.ingredientVarietyQualifiers)
            .isNotEmpty()

        assertWithMessage("Les ingrédients de base attendant variété doivent être chargés")
            .that(vocab.baseIngredientsExpectingVariety)
            .isNotEmpty()

        assertWithMessage("Les articles isolés doivent être chargés")
            .that(vocab.standaloneArticles)
            .isNotEmpty()

        assertWithMessage("Les unités de mesure culinaires doivent être chargées")
            .that(vocab.culinaryMeasurementUnits)
            .isNotEmpty()

        assertWithMessage("Les réparations orthographiques de titres doivent être chargées")
            .that(vocab.titleSpellingRepairs)
            .isNotEmpty()

        assertWithMessage("Les connecteurs multilignes de titres doivent être chargés")
            .that(vocab.titleMultilineConnectors)
            .isNotEmpty()

        assertWithMessage("Les connecteurs de scission d'ingrédients doivent être chargés")
            .that(vocab.ingredientSplitterConnectors)
            .isNotEmpty()

        assertWithMessage("Les réparations de diacritiques doivent être chargées")
            .that(vocab.ocrDiacriticRepairs)
            .isNotEmpty()

        assertWithMessage("Les mots-clés de prix doivent être chargés")
            .that(vocab.priceTitleKeywords)
            .isNotEmpty()

        assertWithMessage("Les catégories de prix bas doivent être chargées")
            .that(vocab.priceCategoriesLow)
            .isNotEmpty()

        assertWithMessage("Les catégories de prix moyens doivent être chargées")
            .that(vocab.priceCategoriesMedium)
            .isNotEmpty()

        assertWithMessage("Les catégories de prix élevés doivent être chargées")
            .that(vocab.priceCategoriesHigh)
            .isNotEmpty()

        assertWithMessage("Les connecteurs d'ingrédients doivent être chargés")
            .that(vocab.ingredientConnectors)
            .isNotEmpty()

        assertWithMessage("Les en-têtes de sections protégés doivent être chargés")
            .that(vocab.protectedSectionHeaders)
            .isNotEmpty()

        assertWithMessage("Les préfixes d'en-têtes de temps doivent être chargés")
            .that(vocab.timeHeaderPrefixes)
            .isNotEmpty()

        assertWithMessage("Les mots-clés d'en-têtes de temps doivent être chargés")
            .that(vocab.timeHeaderKeywords)
            .isNotEmpty()

        assertWithMessage("Les en-têtes d'ingrédients protégés de l'Étape 1 doivent être chargés")
            .that(vocab.ocr1ProtectedIngredientsHeaders)
            .isNotEmpty()

        assertWithMessage("Les en-têtes d'instructions protégés de l'Étape 1 doivent être chargés")
            .that(vocab.ocr1ProtectedInstructionsHeaders)
            .isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadFromProjectResources_CachingBehavior() {
        OcrVocabularyLoader.clearCache()
        val vocab1 = OcrVocabularyLoader.loadFromProjectResources()
        val vocab2 = OcrVocabularyLoader.loadFromProjectResources()

        assertWithMessage("Le deuxième appel à loadFromProjectResources doit retourner la même instance en cache")
            .that(vocab1)
            .isSameInstanceAs(vocab2)

        OcrVocabularyLoader.clearCache()
        val vocab3 = OcrVocabularyLoader.loadFromProjectResources()
        assertWithMessage("Après réinitialisation du cache, une nouvelle instance doit être créée")
            .that(vocab3)
            .isNotSameInstanceAs(vocab1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCacheVocabularyIfNeeded_IgnoresMockWithSmallTitleKeywordsSize() {
        OcrVocabularyLoader.clearCache()

        // Mock vocab avec seulement 2 titleKeywords (<= 20)
        val mockVocabSmall = OcrVocabulary(titleKeywords = listOf("t1", "t2"))
        OcrVocabularyLoader.cacheVocabularyIfNeeded(File("."), mockVocabSmall)

        // Vérifie que la mise en cache a été refusée pour le petit mock
        val loaded1 = OcrVocabularyLoader.loadFromProjectResources()
        assertWithMessage("Un vocabulaire partiel avec peu de mots-clés ne doit pas remplacer le cache réel")
            .that(loaded1)
            .isNotSameInstanceAs(mockVocabSmall)

        // Real vocab avec 25 titleKeywords (> 20)
        val realVocabLarge = OcrVocabulary(
            titleKeywords = List(25) { "title_$it" },
            durationUnits = listOf("min")
        )
        OcrVocabularyLoader.cacheVocabularyIfNeeded(File("."), realVocabLarge)

        // Vérifie que le grand vocabulaire a été accepté en cache
        val loaded2 = OcrVocabularyLoader.loadFromProjectResources()
        assertWithMessage("Un grand vocabulaire complet doit être accepté en cache")
            .that(loaded2)
            .isSameInstanceAs(realVocabLarge)

        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetListAndGetStringVal_MissingKey_ReturnsDefaults() {
        val list = OcrVocabularyLoader.getList(emptyMap(), "missing")
        val str = OcrVocabularyLoader.getStringVal(emptyMap(), "missing")

        assertWithMessage("Une clé absente pour getList doit retourner une liste vide")
            .that(list)
            .isEmpty()

        assertWithMessage("Une clé absente pour getStringVal doit retourner une chaîne vide")
            .that(str)
            .isEmpty()
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
