import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

// Loaded from app/keystore.properties, which is git-ignored and is generated
// either locally (manual copy) or by the CI workflow (.github/workflows/ci.yml)
// from the KEYSTORE_* GitHub secrets. Release builds are left unsigned when
// this file is absent, so a local `assembleRelease` without it still compiles.
val keystorePropertiesFile = rootProject.file("app/keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseSigning = keystorePropertiesFile.exists()

android {
    namespace = "com.gridlab.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gridlab.app"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("21"))
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
    parallel = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.network.okhttp)

    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.m3)

    detektPlugins(libs.detekt.formatting)

    testImplementation(libs.junit)
    testImplementation(libs.konsist)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
