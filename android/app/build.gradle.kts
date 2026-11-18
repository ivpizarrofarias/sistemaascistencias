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

        // URL del backend local. Se puede sobrescribir en android/local.properties
        // (no versionado), por ejemplo para probar desde un dispositivo físico.
        val localProperties = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        val localUrl = localProperties.getProperty("backend.local.url", "http://10.0.2.2:8080/").trim()
        require(localUrl.startsWith("http://") || localUrl.startsWith("https://")) {
            "backend.local.url debe empezar con http:// o https://"
        }
        val finalUrl = if (localUrl.endsWith("/")) localUrl else "$localUrl/"
        buildConfigField(
            "String",
            "LOCAL_BASE_URL",
            "\"" + finalUrl.replace("\\", "\\\\").replace("\"", "\\\"") + "\"",
        )
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
