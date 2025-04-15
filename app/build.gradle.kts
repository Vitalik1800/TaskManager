plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.vs18.taskmanager"
    compileSdk = 35

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES")  // Exclude the DEPENDENCIES file
        jniLibs.excludes.add("lib/armeabi-v7a/some-library.so")
    }

    defaultConfig {
        applicationId = "com.vs18.taskmanager"
        minSdk = 21
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.google.api.client)
    implementation(libs.play.services.auth.v2010)
    implementation(libs.core.jackson.databind)
    implementation (libs.jackson.core)
    implementation(libs.api.client.google.api.client.android.v1330)
    implementation(libs.google.api.services.calendar)
    implementation(libs.google.http.client.gson)  // Ця залежність потрібна для NetHttpTransport
    implementation(libs.google.api.client.jackson2.v272) // Залежність для Jackson
    implementation(libs.google.auth.library.oauth2.http.v0252)  // Залежність для OAuth 2.0
    implementation(libs.google.api.services.calendar.vv3rev4111250)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}