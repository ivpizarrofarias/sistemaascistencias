import java.util.Properties

// Configuración del módulo "app" (aplicación Android).
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cl.ipfsoft.backend.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.ipfsoft.backend.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Las URLs de backend se leen de android/local.properties (NO versionado),
        // así la URL de producción no aparece en GitHub.
        val localProperties = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        fun backendUrl(key: String, fallback: String): String {
            val raw = localProperties.getProperty(key, fallback).trim()
            if (raw.isEmpty()) return ""
            require(raw.startsWith("http://") || raw.startsWith("https://")) {
                "$key debe empezar con http:// o https://"
            }
            return if (raw.endsWith("/")) raw else "$raw/"
        }
        fun kotlinString(value: String): String =
            "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

        buildConfigField("String", "LOCAL_BASE_URL", kotlinString(backendUrl("backend.local.url", "http://10.0.2.2:8080/")))
        buildConfigField("String", "PRODUCTION_BASE_URL", kotlinString(backendUrl("backend.production.url", "")))
    }

    buildTypes {
        release {
            // Minificación desactivada por simplicidad.
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX / Lifecycle / Activity
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose (con BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navegación Compose
    implementation(libs.androidx.navigation.compose)

    // Networking: Retrofit + Gson + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Almacenamiento local de preferencias (guardar token)
    implementation(libs.androidx.datastore.preferences)

    // Tooling de desarrollo Compose
    debugImplementation(libs.androidx.ui.tooling)
}
