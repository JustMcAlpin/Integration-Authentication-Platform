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
        val twitterClientId: String =
            (project.findProperty("TWITTER_CLIENT_ID") as String?)    // gradle.properties
                ?: System.getenv("TWITTER_CLIENT_ID")                 // env var fallback
                ?: ""

        buildConfigField(
            "String",
            "TWITTER_CLIENT_ID",
            "\"$twitterClientId\""
        )

        applicationId = "com.example.integrationauthenticationplatform"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // OAuth redirect schemes
        manifestPlaceholders["appAuthRedirectScheme"] =
            "com.googleusercontent.apps.990112477927-oe9qfesiink1jrdasu38h7jh12cck4m9"
        manifestPlaceholders["msAuthRedirectScheme"] = "com.example.integrationauth"

        // BuildConfig constants (read from gradle.properties or env)
        buildConfigField(
            "String",
            "ENCRYPTION_KEY_B64",
            "\"${project.findProperty("ENCRYPTION_KEY") ?: System.getenv("ENCRYPTION_KEY") ?: ""}\""
        )
        buildConfigField(
            "String",
            "LINKEDIN_CLIENT_ID",
            "\"${project.findProperty("LINKEDIN_CLIENT_ID") ?: System.getenv("LINKEDIN_CLIENT_ID") ?: ""}\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Compose Compiler version is handled by the compose-compiler plugin;
    // you can delete this block if you'd like.
    // composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "DEV_TWILIO_SID",
                "\"${System.getenv("DEV_TWILIO_SID") ?: (project.findProperty("DEV_TWILIO_SID") ?: "")}\""
            )
            buildConfigField(
                "String",
                "DEV_TWILIO_TOKEN",
                "\"${System.getenv("DEV_TWILIO_TOKEN") ?: (project.findProperty("DEV_TWILIO_TOKEN") ?: "")}\""
            )
            buildConfigField(
                "String",
                "DEV_SENDGRID_KEY",
                "\"${System.getenv("DEV_SENDGRID_KEY") ?: (project.findProperty("DEV_SENDGRID_KEY") ?: "")}\""
            )
            buildConfigField(
                "boolean",
                "DEMO_MODE",
                "true"
            )   // demo on for debug
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "DEV_TWILIO_SID", "\"\"")
            buildConfigField("String", "DEV_TWILIO_TOKEN", "\"\"")
            buildConfigField("String", "DEV_SENDGRID_KEY", "\"\"")
            buildConfigField("boolean", "DEMO_MODE", "false")  // demo off for "release"
        }
    }
}

dependencies {
    // --- Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Lifecycle / Coroutines ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Room ---
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // --- Networking / OAuth ---
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("net.openid:appauth:0.11.1")

    // --- UI extras ---
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")

    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
