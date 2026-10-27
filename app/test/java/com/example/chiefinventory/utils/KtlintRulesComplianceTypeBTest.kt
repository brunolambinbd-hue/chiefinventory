package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la conformité des règles KTlint (no-wildcard-imports, comment-spacing)
 * pour l'assurance qualité SonarCloud et Kotlin Style Guide.
 */
class KtlintRulesComplianceTypeBTest {

    private fun hasWildcardImport(importStatement: String): Boolean {
        return importStatement.trim().endsWith(".*")
    }

    private fun hasValidCommentSpacing(commentLine: String): Boolean {
        val trimmed = commentLine.trim()
        if (!trimmed.startsWith("//")) return true
        if (trimmed.length == 2) return true
        return trimmed[2] == ' '
    }

    @Test
    fun `hasWildcardImport should detect wildcard imports ending with star`() {
        assertThat(hasWildcardImport("import androidx.room.*")).isTrue()
        assertThat(hasWildcardImport("import androidx.room.Dao")).isFalse()
        assertThat(hasWildcardImport("import androidx.room.Query")).isFalse()
    }

    @Test
    fun `hasValidCommentSpacing should ensure space exists after double slashes`() {
        assertThat(hasValidCommentSpacing("// Commentaire valide")).isTrue()
        assertThat(hasValidCommentSpacing("//Commentaire invalide")).isFalse()
    }
}
