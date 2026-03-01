import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

val gitCommitHash: String = try {
    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .start()
        .inputStream
        .bufferedReader()
        .use { it.readText().trim() }
} catch (_: Exception) {
    "unknown"
}

android {
    namespace = "com.example.chiefinventory"
    compileSdk = 35

    @Suppress("UnstableApiUsage")
    androidResources {
        // La nouvelle API pour filtrer les locales
        localeFilters += listOf("fr", "en")
    }

    sourceSets {
        getByName("androidTest") {
            java.srcDirs(
                "src/androidTest/java",
                "src/androidTest/java-ui",
                "src/androidTest/java-integration"
            )
        }
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            } else {
                // Fallback si le fichier properties n'existe pas encore ou a été renommé
                keyAlias = "chiefinventory"
                keyPassword = "votre_mot_de_passe"
                storeFile = file("../chiefinventory.jks")
                storePassword = "votre_mot_de_passe"
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.chiefinventory"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GIT_COMMIT_HASH", "\"$gitCommitHash\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"https://dev.api.com\"")
            buildConfigField("boolean", "ENABLE_LOGS", "true")
        }
        release {
            buildConfigField("String", "API_URL", "\"https://prod.api.com\"")
            buildConfigField("boolean", "ENABLE_LOGS", "false")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }

    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}

dependencies {
    implementation(project(":imagecomparison"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.coil)
    implementation(libs.androidx.work.runtime.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.image.cropper)
    implementation(libs.mlkit.text.recognition)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.lifecycle.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.truth)
}

afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
    tasks.named("bundleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
}
