plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

    compileSdk = 34
    namespace = "com.example.styleanime"


    defaultConfig {
        applicationId = "com.example.styleanime"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        mlModelBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // AndroidX Camera
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")

    // AndroidX Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.compose.runtime:runtime-livedata:1.6.2")
    implementation ("androidx.activity:activity-compose:1.8.2")

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("de.hdodenhof:circleimageview:3.0.0")

    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.ui:ui:1.6.2")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.compose.material:material:1.6.2")
    implementation ("androidx.compose.ui:ui-tooling:1.6.2")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
}
