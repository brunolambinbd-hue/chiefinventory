plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
//    id("de.undercouch.download")
}

android {
    namespace = "com.example.imagecomparison"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }

    // Ajout pour que Gradle lise le dossier resources des tests
    sourceSets {
        getByName("test") {
            resources.srcDirs("src/test/resources")
        }
    }
}

dependencies {
    // Use 'api' with the version catalog to expose this dependency to the main app module correctly
    api(libs.mediapipe.tasks.vision)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// On définit la variable pour le script et on l'applique
//extra.set("ASSET_DIR", project.file("src/main/assets"))
//apply(from = rootProject.file("download_models.gradle"))
