pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // La version de l'AGP est définie ici
        id("com.android.application") version "8.13.0" apply false
        id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    }
}
@Suppress("UnstableApiUsage") // <-- Ajoutez cette ligne
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ParaBDCollector"
include(":app")
include(":imagecomparison")
