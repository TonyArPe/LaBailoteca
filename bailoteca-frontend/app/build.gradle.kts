plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.bailotecaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bailotecaapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Soporte para Jetpack Compose
    buildFeatures {
        compose = true
    }

    // Versión del compilador de Kotlin para Compose
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // AndroidX básicos
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity para Compose
    implementation(libs.androidx.activity.compose.v182)

    // Jetpack Compose (versión 1.7.8)
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation(libs.androidx.foundation.layout.android)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Couritines
    implementation(libs.kotlinx.coroutines.android)

    // Navigation con Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation(libs.firebase.auth.ktx)

    // Retrofit
    implementation(libs.retrofit)

    // Conversor Gson para parsear JSON automáticamente
    implementation(libs.converter.gson)

    // OkHttp para logging (opcional pero muy útil para depurar)
    implementation(libs.logging.interceptor)

}

apply(plugin = "com.google.gms.google-services")
