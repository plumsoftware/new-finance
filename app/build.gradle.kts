plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
}

val googleServicesFile = file("google-services.json")
val isFirebaseEnabled = googleServicesFile.exists()

if (isFirebaseEnabled) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "ru.plumsoftware.finance"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.plumsoftware.finance"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "FIREBASE_ENABLED", isFirebaseEnabled.toString())
        buildConfigField("int", "PLATFORM", "1")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("key_store.jks")
            storePassword = "81abbc2x0BBAc4XtOZzN3uyvU20km6xD9KEXxVJNyQCw1WfIpQ"
            keyAlias = "key_new_finance"
            keyPassword = "81abbc2x0BBAc4XtOZzN3uyvU20km6xD9KEXxVJNyQCw1WfIpQ"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Корутины
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // KOIN
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Biometric
    implementation(libs.androidx.biometric)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Зависимости всегда (код FCM/In-App в проекте); плагин google-services — только при наличии json
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging.display)

    // Vico Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Yandex Mobile Ads (RuStore)
    implementation(libs.yandex.mobileads)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.runtime)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}