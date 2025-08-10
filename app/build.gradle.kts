plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.kapt") // for Room
}

android {
    namespace = "com.example.integrationauthenticationplatform"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.integrationauthenticationplatform"
        minSdk = 26   // Base64 needs 26+
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ENCRYPTION_KEY_B64",
            "\"${project.findProperty("ENCRYPTION_KEY") ?: System.getenv("ENCRYPTION_KEY") ?: ""}\""
        )

        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.integrationauth"
    }

    buildFeatures {
        compose = true
        android.buildFeatures.buildConfig = true
    }

    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    // --- Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.compiler:compiler:1.5.15")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // --- Lifecycle / Coroutines ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // --- Room ---
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    // --- Tests ---
    testImplementation("junit:junit:4.13.2")
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("net.openid:appauth:0.11.1")
    implementation("com.google.android.material:material:1.12.0")
}