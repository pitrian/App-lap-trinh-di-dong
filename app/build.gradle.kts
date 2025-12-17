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
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "META-INF/*.md"
        }
    }

    dependencies {
        // AndroidX Core
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.activity:activity-ktx:1.8.0")
        implementation("androidx.fragment:fragment-ktx:1.6.1")

        // Material Design
        implementation("com.google.android.material:material:1.10.0")

        // RecyclerView & CardView
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.cardview:cardview:1.0.0")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation("com.google.firebase:firebase-storage")

        // Firebase UI (optional)
        implementation("com.firebaseui:firebase-ui-auth:8.0.2")
        implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

        // Google Play Services
        implementation("com.google.android.gms:play-services-auth:20.7.0")

        // Image Loading - Glide
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

        // Lottie Animations
        implementation("com.airbnb.android:lottie:6.1.0")

        // Markwon (Markdown) - Fixed version to avoid conflicts
        implementation("io.noties.markwon:core:4.6.2")
        implementation("io.noties.markwon:ext-tables:4.6.2")
        implementation("io.noties.markwon:ext-strikethrough:4.6.2")

        // Email (if needed, but consider removing if not used)
        // implementation("com.sun.mail:android-mail:1.6.7")
        // implementation("com.sun.mail:android-activation:1.6.7")

        // Navigation Component
        implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
        implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

        // Lifecycle Components
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

        implementation("com.sun.mail:android-mail:1.6.7")
        implementation("com.sun.mail:android-activation:1.6.7")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    }
}
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}
