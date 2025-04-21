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
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose (versión 1.7.8)
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")

    // Navigation con Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Conversor Gson para parsear JSON automáticamente
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp para logging (opcional pero muy útil para depurar)
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

}

apply(plugin = "com.google.gms.google-services")
