plugins {
    kotlin("android")
    id("com.android.library")
}

dependencies {
    implementation(project(":common"))

    implementation(libs.kotlin.coroutine)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    //implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)
    implementation(libs.gson)
    implementation(libs.logging.interceptor)
    //implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
    //implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.10.0")
    implementation ("dnsjava:dnsjava:3.5.3")
}