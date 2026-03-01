@file:Suppress("NonAsciiCharacters")

package com.example.chiefinventory.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Tests unitaires paramétrés pour l'objet utilitaire [DescriptionParser].
 *
 * Cette classe vérifie que la logique d'extraction des informations
 * fonctionne correctement pour différents formats de chaînes en utilisant un test par cas.
 */
@RunWith(Parameterized::class)
class DescriptionParserTest(
    private val caseName: String,
    private val title: String?,
    private val description: String?,
    private val expected: DescriptionParser.ParsedInfo
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}") // Utilise le premier paramètre (caseName) pour nommer le test
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf(
                    "Cas 1: Extraction standard",
                    "Titre de l'objet (30/40cm)",
                    "Description avec un Tirage : 500 ex.",
                    DescriptionParser.ParsedInfo(
                        tirage = "500",
                        dimensions = "30/40"
                    )
                ),
                arrayOf(
                    "Cas 2: Aucune information",
                    "Un titre simple",
                    "Une description sans détails.",
                    DescriptionParser.ParsedInfo(
                        tirage = null,
                        dimensions = null
                    )
                ),
                arrayOf(
                    "Cas 3: Dimension décimale avec point",
                    "Objet avec dimension (25.5/35.5cm)",
                    null,
                    DescriptionParser.ParsedInfo(
                        tirage = null,
                        dimensions = "25.5/35.5"
                    )
                ),
                arrayOf(
                    "Cas 4: Dimension décimale avec virgule",
                    null,
                    "Taille: (25,5/35,5cm)",
                    DescriptionParser.ParsedInfo(
                        tirage = null,
                        dimensions = "25,5/35,5"
                    )
                )
            )
        }
    }

    /**
     * Exécute un seul cas de test fourni par le constructeur paramétré.
     */
    @Test
    fun `Le cas de test doit passer`() {
        // WHEN : La fonction parse est appelée avec les données du cas de test.
        val result = DescriptionParser.parse(title, description)

        // THEN : Le résultat doit correspondre au résultat attendu pour ce cas.
        assertEquals("Échec sur le cas : '$caseName'", expected, result)
    }
}
