package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Test unitaire d'isolation TDD dédié au Problème JVM #2 :
 * Élimination stricte des retours chariot Windows (\r\n) lors du parsing XML DOM dans [OcrVocabularyLoader].
 */
class OcrJvmWindowsCrLfTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testParseXmlDocument_StripsWindowsCarriageReturnsFromXmlItems() {
        val xmlWithWindowsCrLf = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string-array name="test_crlf_array">
                    <item>mot1&#13;</item>
                    <item>mot2&#13;&#10;</item>
                </string-array>
                <string name="test_crlf_string">valeur1&#13;</string>
            </resources>
        """.trimIndent()

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(xmlWithWindowsCrLf.byteInputStream(Charsets.UTF_8))

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseXmlDocument(doc, stringArrays, strings)

        val items = stringArrays["test_crlf_array"] ?: emptyList()
        val stringVal = strings["test_crlf_string"] ?: ""

        assertWithMessage("Les éléments du tableau XML doivent être nettoyés des retours chariot \\r\\n")
            .that(items)
            .containsExactly("mot1", "mot2")

        assertWithMessage("La chaîne XML doit être nettoyée du retour chariot \\r")
            .that(stringVal)
            .isEqualTo("valeur1")

        // Vérifie qu'aucun \r ne persiste dans les chaînes extraites du XML
        for (item in items) {
            assertWithMessage("L'élément '$item' ne doit pas contenir de retour chariot \\r")
                .that(item)
                .doesNotContain("\r")

            assertWithMessage("La longueur de l'élément '$item' doit être de 4 caractères")
                .that(item.length)
                .isEqualTo(4)
        }
        assertWithMessage("La valeur de chaîne '$stringVal' ne doit pas contenir de retour chariot \\r")
            .that(stringVal)
            .doesNotContain("\r")
    }
}
