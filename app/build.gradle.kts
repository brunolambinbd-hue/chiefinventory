import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("jacoco")
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
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

val buildTimestamp: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())

android {
    namespace = "com.chefinventory.app"
    compileSdk = libs.versions.sdkCompile.get().toString().toInt()

    @Suppress("UnstableApiUsage")
    androidResources {
        localeFilters += listOf("fr", "en")
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res",
                "src/main/res-pantry",
                "src/main/res-catalog",
                "src/main/res-ocr"
            )
        }
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
        applicationId = "com.chefinventory.app"
        minSdk = libs.versions.sdkMin.get().toString().toInt()
        targetSdk = libs.versions.sdkTarget.get().toString().toInt()
        versionCode = project.property("APP_VERSION_CODE").toString().toInt()
        versionName = project.property("APP_VERSION_NAME").toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GIT_COMMIT_HASH", "\"$gitCommitHash\"")
        buildConfigField("String", "BUILD_DATE", "\"$buildTimestamp\"")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "API_URL", "\"https://dev.api.com\"")
            buildConfigField("boolean", "ENABLE_LOGS", "true")
            enableUnitTestCoverage = true
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
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
    }
    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get().toString()
    }

    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}

dependencies {
    implementation(libs.ocr)
    implementation(libs.traceability)
    implementation(libs.locationmanagement)

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
    implementation(libs.jsoup)
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation("org.json:json:20231013")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.lifecycle.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.truth)
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("fullCoverageReport") {
    group = "Reporting"
    description = "Génère le rapport de couverture JaCoCo XML et HTML pour les tests unitaires Debug."

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/fullCoverageReport/report.xml"))
        html.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/fullCoverageReport/html"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Binding.*",
        "**/*_Impl*.*",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec", "jacoco/testDebugUnitTest.exec")
    })
}

tasks.register("jacocoTestReport") {
    dependsOn("fullCoverageReport")
}

afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
    tasks.named("bundleRelease") {
        dependsOn(tasks.named("testReleaseUnitTest"))
    }
}
