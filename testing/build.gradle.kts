plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.sangcomz.asynclocationmap.testing"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

dependencies {
    // Domain module
    implementation(project(":domain"))

    // Data module (for interfaces like LocationLocalDataSource, LocationRemoteDataSource)
    implementation(project(":data"))

    // Coroutines
    implementation(libs.coroutines.core)

    // Javax Inject (for @Inject annotation)
    implementation(libs.javax.inject)
}

