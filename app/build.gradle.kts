plugins {
    alias(libs.plugins.android.application)
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.example.studentintelligentsystem"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.studentintelligentsystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read API key from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        // Add API key to BuildConfig
        val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        // Add Supabase configuration to BuildConfig
        val supabaseUrl = properties.getProperty("SUPABASE_URL") ?: ""
        val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)

    // Supabase dependencies
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.4")

    // Ktor client (required by Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")
    implementation("io.ktor:ktor-client-serialization:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mpandroidchart)
    implementation(libs.generativeai)
    implementation(libs.guava)
    implementation(libs.reactiveStreams)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}