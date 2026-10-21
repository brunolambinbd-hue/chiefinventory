import java.net.URL
import java.io.File

/**
 * Tâche Gradle v3 pour synchroniser l'INTÉGRALITÉ de la documentation partagée depuis GitHub.
 * Utilise shared-files.txt comme index pour découvrir les nouveaux fichiers.
 */
tasks.register("syncProjectStandards") {
    group = "Verification"
    description = "Synchronise dynamiquement tous les standards et documents depuis GitHub."

    doLast {
        val baseUrl = "https://raw.githubusercontent.com/brunolambinbd-hue/ProjectStandards/main"
        val manifestUrl = "$baseUrl/shared-files.txt"

        println("📡 Récupération de l'index des fichiers : $manifestUrl")

        try {
            // 1. Toujours synchroniser le .cursorrules (à la racine)
            syncFile("$baseUrl/.cursorrules", ".cursorrules")

            // 2. Récupérer la liste des documents
            val manifestContent = URL(manifestUrl).readText()
            val filesToSync = manifestContent.lines().filter { it.isNotBlank() }

            println("📂 ${filesToSync.size} fichiers détectés dans la documentation.")

            // 3. Synchroniser chaque fichier de la liste
            filesToSync.forEach { relativePath ->
                val remoteUrl = "$baseUrl/$relativePath"
                // On s'assure que le chemin local est correct (docs/shared/...)
                val localPath = relativePath.replace("docs/", "docs/shared/")
                syncFile(remoteUrl, localPath)
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la synchronisation globale : ${e.message}")
        }
    }
}

/**
 * Télécharge un fichier si distant différent du local.
 */
fun syncFile(remoteUrl: String, localPath: String) {
    val projectDir = project.projectDir // Accès sécurisé au dossier projet
    val localFile = File(projectDir, localPath)

    try {
        val remoteContent = URL(remoteUrl).readText()
        if (!localFile.exists() || localFile.readText() != remoteContent) {
            localFile.parentFile.mkdirs()
            localFile.writeText(remoteContent)
            println("✅ Mis à jour : $localPath")
        } else {
            // println("👍 Déjà à jour : $localPath")
        }
    } catch (e: Exception) {
        println("❌ Échec sur $localPath : ${e.message}")
    }
}

// Lancement automatique au moment du Gradle Sync (via l'IDE)
tasks.named("help") {
    dependsOn("syncProjectStandards")
}
