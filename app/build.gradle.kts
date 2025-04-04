plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.gms)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.motivationcalendarapi"
    compileSdk = 35

    packaging {
        resources.excludes.add("META-INF/*")
        resources.excludes.add("google/protobuf/*")
    }

    defaultConfig {
        applicationId = "com.example.motivationcalendarapi"
        minSdk = 28
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {


    // Google auth
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //Firebase bom
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    //Database
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")

    //Firebase storage
    implementation("com.google.firebase:firebase-storage")

    //Firebase Auth
    implementation("com.google.firebase:firebase-auth")

    //navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    //Retrofit 2
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    //coil
    implementation("io.coil-kt:coil-compose:2.7.0")
//
//    //Live data
//    implementation ("androidx.compose.runtime:runtime-livedata:1.0.0-beta01")

//    //work-manager
//    implementation ("androidx.work:work-runtime-ktx:2.7.1")
//    implementation("androidx.compose.material:material:1.8.0-beta01")
//    implementation("androidx.wear.compose:compose-navigation:1.4.1")

    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)


    //ui
//    implementation(libs.ui)
    implementation(libs.androidx.foundation.android)


    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.ui.text.google.fonts)

//    implementation(libs.logging.interceptor)
//    implementation(libs.screenshot.validation.junit.engine)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

}
