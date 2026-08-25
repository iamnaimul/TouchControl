plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.naimul.touchcontrol"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.naimul.touchcontrol"
        minSdk = 28
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Only dependency left on purpose: core-ktx is a thin, ~small set of
    // Kotlin extensions (used here for getSystemService<T>()), not a large
    // library. AppCompat has been removed below — MainActivity extends the
    // plain platform Activity and uses no AppCompat widgets, so it was
    // pulling in the whole androidx.appcompat graph for nothing.
    implementation("androidx.core:core-ktx:1.15.0")
}
