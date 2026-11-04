package com.chefinventory.app.model

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le modèle de données [OpenFoodFactsTaxonomy].
 */
class OpenFoodFactsTaxonomyTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsTaxonomy_DefaultValues() {
        val taxonomy = OpenFoodFactsTaxonomy(
            cleanName = "haricots verts"
        )

        assertThat(taxonomy.id).isEqualTo(0L)
        assertThat(taxonomy.cleanName).isEqualTo("haricots verts")
        assertThat(taxonomy.category).isNull()
        assertThat(taxonomy.categoryCode).isEqualTo(10)
        assertThat(taxonomy.synonyms).isNull()
        assertThat(taxonomy.averageWeightStr).isNull()
        assertThat(taxonomy.seasonStr).isNull()
        assertThat(taxonomy.carbonFootprintStr).isNull()
        assertThat(taxonomy.ciqualName).isNull()
        assertThat(taxonomy.ecobalyseCategory).isNull()
        assertThat(taxonomy.parentsStr).isNull()
        assertThat(taxonomy.childrenStr).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsTaxonomy_FullConstructorAndGeneratedMethods() {
        val taxonomy1 = OpenFoodFactsTaxonomy(
            id = 10L,
            cleanName = "haricots verts",
            category = "Légumes",
            categoryCode = 1,
            synonyms = "haricots, princesses",
            averageWeightStr = "250 g / unité",
            seasonStr = "De Mai à Octobre",
            carbonFootprintStr = "0.7 kg CO2 / kg",
            ciqualName = "Haricot vert, cuit",
            ecobalyseCategory = "fruits_and_vegetables",
            parentsStr = "en:vegetable",
            childrenStr = "fr:haricots-verts-extra-fins"
        )

        val taxonomy1Copy = taxonomy1.copy(id = 10L)
        val taxonomy2 = taxonomy1.copy(id = 20L)

        assertThat(taxonomy1).isEqualTo(taxonomy1Copy)
        assertThat(taxonomy1).isNotEqualTo(taxonomy2)
        assertThat(taxonomy1.hashCode()).isEqualTo(taxonomy1Copy.hashCode())
        assertThat(taxonomy1.toString()).contains("haricots verts")

        // Destructuring / Component N methods verification
        val (id, cleanName, category, categoryCode, synonyms) = taxonomy1
        assertThat(id).isEqualTo(10L)
        assertThat(cleanName).isEqualTo("haricots verts")
        assertThat(category).isEqualTo("Légumes")
        assertThat(categoryCode).isEqualTo(1)
        assertThat(synonyms).isEqualTo("haricots, princesses")
    }
}
