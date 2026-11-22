package com.chefinventory.app.utils

import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.chefinventory.app.ui.actvity.EditIngredientActivity

/**
 * Assistant de l'IHM ("Vieille École") responsable du formatage hypertexte des lignes d'ingrédients.
 *
 * Responsabilités :
 * - Localiser le mot noyau d'ingrédient sur chaque ligne du champ texte.
 * - Transformer le texte en [SpannableStringBuilder] avec des [ClickableSpan] bleu soulignés.
 * - Intercepter le clic utilisateur pour naviguer vers [EditIngredientActivity].
 */
object OcrIngredientSpannableFormatter {

    /**
     * Formate un texte multi-lignes d'ingrédients en y insérant des liens hypertextes interactifs sur les mots d'ingrédients noyaux.
     *
     * @param context Le contexte Android permettant de démarrer l'Activity.
     * @param fullText Le texte complet multi-lignes des ingrédients.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param offTaxonomyList La liste des noms d'aliments français Open Food Facts (Room DB).
     * @param onIngredientClick Callback facultatif lors du clic (pour les tests unitaires TDD).
     * @return Le [SpannableStringBuilder] enrichi de liens hypertextes cliquables.
     */
    fun formatIngredientHyperlinks(
        context: Context,
        fullText: String,
        vocabulary: OcrVocabulary,
        offTaxonomyList: List<String> = emptyList(),
        onIngredientClick: ((ingredientName: String) -> Unit)? = null
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(fullText)
        if (fullText.isBlank()) return builder

        val lines = fullText.split("\n")
        var currentOffset = 0

        for (line in lines) {
            if (line.isNotBlank()) {
                val spanRanges = IngredientParser.findCoreIngredientSpans(line, vocabulary, offTaxonomyList)
                spanRanges.forEach { spanRange ->
                    val targetWord = line.substring(spanRange.first, spanRange.last).trim()
                    val startInBuilder = currentOffset + spanRange.first
                    val endInBuilder = currentOffset + spanRange.last

                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            if (onIngredientClick != null) {
                                onIngredientClick.invoke(targetWord)
                            } else {
                                openEditIngredientActivity(context, targetWord)
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = true
                            ds.color = context.getColor(com.chefinventory.app.R.color.colorPrimary)
                        }
                    }

                    builder.setSpan(
                        clickableSpan,
                        startInBuilder,
                        endInBuilder,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            currentOffset += line.length + 1 // +1 pour le saut de ligne \n
        }

        return builder
    }

    /**
     * Ouvre [EditIngredientActivity] en transmettant le nom d'ingrédient pré-rempli.
     */
    private fun openEditIngredientActivity(context: Context, ingredientName: String) {
        val intent = Intent(context, EditIngredientActivity::class.java).apply {
            putExtra(EditIngredientActivity.EXTRA_PREFILLED_NAME, ingredientName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
