plugins {
    id("com.android.application")
}

android {
    namespace = "org.opensmartisanos.ui.catalog"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.opensmartisanos.ui.catalog"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core"))
}
