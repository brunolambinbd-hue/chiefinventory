plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.parabdcollector"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.parabdcollector"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        unitTests.isReturnDefaultValues = true // Ajout pour mocker les classes Android dans les tests unitaires
    }
}

dependencies {
    implementation(project(":imagecomparison"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.coil)
    implementation(libs.androidx.work.runtime.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.image.cropper)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.lifecycle.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test) // Ajout pour les tests instrumentés
}

// Ajout de la "Porte de Qualité" (Quality Gate)
// On utilise afterEvaluate pour s'assurer que toutes les tâches ont été créées
// avant d'essayer de leur ajouter une dépendance.
afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
    tasks.named("bundleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
}
