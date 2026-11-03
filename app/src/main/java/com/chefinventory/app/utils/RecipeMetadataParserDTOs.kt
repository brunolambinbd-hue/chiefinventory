package com.chefinventory.app.utils

/**
 * Conteneur du résultat d'extraction de métadonnées.
 *
 * @property isPureMetadata `true` si la ligne contenait uniquement des métadonnées et doit être sautée.
 * @property remainingLine Le texte de la ligne restant après suppression des métadonnées extraites.
 */
data class MetadataResult(
    val isPureMetadata: Boolean,
    val remainingLine: String
)
