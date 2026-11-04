package com.chefinventory.app.model

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [OpenFoodFactsTaxonomy].
 */
class OpenFoodFactsTaxonomyTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsTaxonomy_DefaultConstructor() {
        val taxonomy = OpenFoodFactsTaxonomy(cleanName = "haricots verts")

        assertThat(taxonomy.id).isEqualTo(0L)
        assertThat(taxonomy.cleanName).isEqualTo("haricots verts")
        assertThat(taxonomy.category).isNull()
        assertThat(taxonomy.synonyms).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsTaxonomy_FullConstructorAndGeneratedMethods() {
        val tax1 = OpenFoodFactsTaxonomy(
            id = 10L,
            cleanName = "haricots verts",
            category = "Légumes",
            synonyms = "haricots, princesses"
        )

        val tax2 = tax1.copy()
        val tax3 = tax1.copy(cleanName = "haricots beurre")

        assertThat(tax1).isEqualTo(tax2)
        assertThat(tax1).isNotEqualTo(tax3)
        assertThat(tax1.hashCode()).isEqualTo(tax2.hashCode())
        assertThat(tax1.toString()).contains("OpenFoodFactsTaxonomy")

        assertThat(tax1.component1()).isEqualTo(10L)
        assertThat(tax1.component2()).isEqualTo("haricots verts")
        assertThat(tax1.component3()).isEqualTo("Légumes")
        assertThat(tax1.component4()).isEqualTo("haricots, princesses")
    }
}
