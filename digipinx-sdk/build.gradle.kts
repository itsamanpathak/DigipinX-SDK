plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.amanpathak.digipinx.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        // Library version
        buildConfigField("String", "SDK_VERSION", "\"1.0.0\"")
        buildConfigField("String", "SDK_NAME", "\"DigipinX Android SDK\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.itsamanpathak"
            artifactId = "digipinx-sdk"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("DigipinX Android SDK")
                description.set("Digipin finder SDK for Android - Unofficial implementation of India Post Digipin system")
                url.set("https://github.com/itsamanpathak/DigipinX-SDK")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("itsamanpathak")
                        name.set("Aman Pathak")
                        email.set("itsamanpathak@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/itsamanpathak/DigipinX-SDK.git")
                    developerConnection.set("scm:git:ssh://github.com/itsamanpathak/DigipinX-SDK.git")
                    url.set("https://github.com/itsamanpathak/DigipinX-SDK")
                }
            }
        }
    }
}