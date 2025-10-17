plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Ensure this is a recent version
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false // Ensure this matches your Kotlin version
    id("com.google.gms.google-services") version "4.4.3" apply false
}