package com.example.chiefinventory.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Représente un ingrédient du garde-manger de la cuisine (Table SQLite `ingredients`).
 *
 * @property id Identifiant unique auto-généré.
 * @property name Le nom d'usage de l'aliment (ex: "Lait demi-écrémé", "Beurre doux").
 * @property quantity La quantité actuelle en stock au garde-manger.
 * @property unit L'unité de mesure culinaire (ex: "g", "L", "pièce").
 * @property category Le rayon de supermarché (ex: "Crémerie", "Épicerie").
 * @property locationId Emplacement physique maison (ex: Frigo, Placard).
 * @property stockPolicy Politique de réassort ("PERMANENT" pour réassort automatique ou "ON_DEMAND").
 * @property minThreshold Seuil minimum d'alerte non nul sous lequel déclencher le réassort.
 * @property packSize Taille du conditionnement/pack d'achat (ex: 6 pour pack de 6 L).
 */
@Parcelize
@Entity(tableName = "ingredients", indices = [Index(value = ["locationId"])])
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null, // e.g., "g", "ml", "pcs"
    val category: String? = null, // e.g., "Dairy", "Vegetables"
    val expirationDate: Long? = null,
    val description: String? = null,
    val supplementalInfo: String? = null, // Infos extraites des parenthèses (ex: poids alternatif)
    val imageUri: String? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageEmbedding: ByteArray? = null,
    val ocrText: String? = null,
    val locationId: Long? = null, // ID from the locations table (type INGREDIENT)
    val stockPolicy: String = "PERMANENT", // PERMANENT or ON_DEMAND
    val minThreshold: Double? = null, // Seuil minimum d'alerte non nul
    val packSize: Double = 1.0, // Taille du conditionnement/pack d'achat (ex: 6)
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
