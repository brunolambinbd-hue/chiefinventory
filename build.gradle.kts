import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    id("org.sonarqube") version "5.1.0.4882"
}

// 🔑 Récupération sécurisée du jeton SonarCloud depuis local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val sonarToken: String? = localProperties.getProperty("sonar.token") ?: System.getenv("SONAR_TOKEN")

sonarqube {
    properties {
        property("sonar.projectKey", "brunolambinbd-hue_ChefInventory")
        property("sonar.organization", "brunolambinbd-hue")
        property("sonar.host.url", "https://sonarcloud.io")

        // Injection automatique du jeton
        sonarToken?.let { property("sonar.token", it) }

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.scm.provider", "git")
        property("sonar.gradle.skipCompile", "true")

        // Exclusions de fichiers binaires ou générés
        property(
            "sonar.exclusions",
            """
            **/build/**,
            **/generated/**,
            **/.gradle/**,
            **/*Binding.java,
            **/*_Impl.java,
            **/R.java,
            **/BuildConfig.java,
            **/*.tflite,
            **/*.webp,
            **/*.png,
            **/*.jpg,
            **/*.json
            """.trimIndent()
        )

        property("sonar.tests", "src/test/java,src/androidTest/java")
    }
}

subprojects {
    apply(plugin = "org.sonarqube")
    sonarqube {
        properties {
            val buildDir = project.layout.buildDirectory.get().asFile

            // Pointage vers le rapport de tests unitaires
            property("sonar.junit.reportPaths", "$buildDir/test-results/testDebugUnitTest")

            // Pointage vers les rapports XML JaCoCo
            property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/fullCoverageReport/report.xml")

            // Pointage vers le rapport Lint Android
            property("sonar.androidLint.reportPaths", "$buildDir/reports/lint/lint-report.xml")
        }
    }
}

/*
 * TÂCHE D'ORCHESTRATION COMPLÈTE
 * Lance Lint, les tests unitaires (avec couverture JaCoCo) et envoie à SonarCloud.
 */
tasks.register("runFullSonarAnalysis") {
    group = "Reporting"
    description = "Exécute Lint, les tests (Couverture JaCoCo) et télécharge le rapport vers SonarCloud."

    dependsOn(":app:lintDebug")
    dependsOn(":app:fullCoverageReport")

    finalizedBy("sonar")

    doLast {
        println("\n🚀 CYCLE SONAR : Rapports générés. Lancement de l'upload vers SonarCloud...")
    }
}
