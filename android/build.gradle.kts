// Archivo de build raíz del proyecto.
// Declara los plugins usados por los módulos, sin aplicarlos aquí.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
