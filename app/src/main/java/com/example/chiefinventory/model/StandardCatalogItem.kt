package com.example.chiefinventory.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Représente un produit type du catalogue standard d'initialisation du garde-manger.
 * Provenance : Open Food Facts & ANSES CIQUAL (Adaptation Franco-Belge).
 *
 * @see REQ-4.3.1.1 Catalogue XML au format à 7 champs.
 * @property name Le nom d'usage de l'aliment (ex: "Beurre doux", "Chicons / Endives").
 * @property defaultQuantity La quantité standard suggérée (ex: 250.0).
 * @property defaultUnit L'unité de mesure culinaire ou de contenant (ex: "g", "kg", "sachet", "ravier").
 * @property category La famille de rayon du magasin (ex: "Crémerie", "Épicerie", "Légumes frais").
 * @property defaultLocationName L'emplacement maison naturel suggéré (ex: "Frigo", "Placard", "Surgélateur", "Cave").
 * @property stockPolicy La politique de stock ("PERMANENT" pour alerte réassort ou "ON_DEMAND" pour frais sur commande).
 * @property minThreshold Le seuil d'alerte de stock faible non nul.
 * @property packSize La taille du conditionnement/pack d'achat (ex: 6 pour pack de 6 L ou boîte de 6).
 */
@Parcelize
data class StandardCatalogItem(
    val name: String,
    val defaultQuantity: Double = 1.0,
    val defaultUnit: String? = null,
    val category: String = "Épicerie",
    val defaultLocationName: String = "Placard",
    val stockPolicy: String = "PERMANENT",
    val minThreshold: Double = defaultQuantity / 2.0,
    val packSize: Double = 1.0
) : Parcelable
