// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    // Ajout du plugin SonarQube - Version compatible avec Kotlin 1.9.x
    id("org.sonarqube") version "4.4.1.3373"
}

sonarqube {
    properties {
        property("sonar.projectKey", "brunolambinbd-hue_ChefInventory")
        property("sonar.organization", "brunolambinbd-hue")
        property("sonar.host.url", "https://sonarcloud.io")

        // Éviter la compilation implicite (recommandé par le plugin)
        property("sonar.gradle.skipCompile", "true")
        
        // LA LIGNE LA PLUS IMPORTANTE : Exclure les fichiers générés
        property("sonar.exclusions", "**/build/**, **/generated/**, **/.gradle/**, **/*Binding.java, **/*_Impl.java")
    }
}
