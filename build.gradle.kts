// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "8.0.0" apply false
  id("com.android.library") version "8.0.0" apply false
  kotlin("android") version "1.8.10" apply false
  kotlin("plugin.serialization") version "1.8.10" apply false
  id("org.jlleitschuh.gradle.ktlint") version "11.3.1" apply false
  id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}
