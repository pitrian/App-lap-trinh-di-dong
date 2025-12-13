plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.appattt"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.appattt"
        minSdk = 29
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }

    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation("com.google.firebase:firebase-storage")
        implementation("com.google.android.gms:play-services-auth:20.7.0")
        implementation("com.sun.mail:android-mail:1.6.7")
        implementation("com.sun.mail:android-activation:1.6.7")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        implementation("com.airbnb.android:lottie:6.1.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
}
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}
