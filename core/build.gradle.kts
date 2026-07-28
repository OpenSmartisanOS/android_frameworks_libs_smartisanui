plugins {
    id("com.android.library")
    id("maven-publish")
}

group = "org.opensmartisanos"
version = "0.1.0-SNAPSHOT"

android {
    namespace = "org.opensmartisanos.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    lint {
        // The extracted ROM expand slots intentionally rely on horizontal's default value.
        disable += "Orientation"
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "smartisan-ui-core"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
