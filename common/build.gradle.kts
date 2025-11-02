plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    compileOnly(project(":hideapi"))

    implementation(libs.kotlin.coroutine)
    implementation(libs.androidx.core)
    implementation(libs.gson)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    //implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}
