plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jlleitschuh.gradle.ktlint")
  id("com.google.devtools.ksp")
}

android {
  sourceSets {
    getByName("main").resources {
      srcDir(file("src/main/res"))
    }
  }

  namespace = "com.ifmo.balda"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.ifmo.balda"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

val roomVersion = "2.5.1"

dependencies {
  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.8.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.room:room-runtime:$roomVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
  androidTestImplementation("androidx.test:runner:1.5.2")
}
