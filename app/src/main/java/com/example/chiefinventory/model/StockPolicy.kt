package com.example.chiefinventory.model

/**
 * Politiques de réassort du stock pour les ingrédients du garde-manger.
 */
object StockPolicy {
    /** Ingrédient de base à garder en permanence en stock (déclenche des alertes de réassort). */
    const val PERMANENT: String = "PERMANENT"

    /** Ingrédient acheté uniquement sur commande pour une recette spécifique. */
    const val ON_DEMAND: String = "ON_DEMAND"
}
