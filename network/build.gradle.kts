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
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.9.0")
}