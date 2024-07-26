plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.googleService)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "net.ienlab.sogangassist"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.ienlab.sogangassist"
        minSdk = 27
        targetSdk = 34
        versionCode = 34
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "versionName", versionName ?: "-")
    }

    buildTypes {
        release {
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

//    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.icons.extended)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    ksp(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt)

    implementation(libs.androidx.window)
    implementation(libs.androidx.compose.materialWindow)
    implementation(libs.androidx.datastore.pref)

//    implementation 'com.github.bumptech.glide:glide:4.13.2'
//    annotationProcessor 'com.github.bumptech.glide:compiler:4.13.2'

    implementation(libs.compose.fadingedges)
    implementation(libs.compose.permission)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.grid)
    implementation(libs.compose.pref)
    implementation(libs.compose.drawablepainter)
    implementation(libs.google.oss.license)
    implementation(libs.accompanist.webview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.process)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.inappmessaging.display)

    implementation(libs.touchlane.gridpad)
    implementation(libs.volley)
    implementation(libs.compose.markdown)
    implementation(libs.cloudy)

    // widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.glance.appwidget.preview)
    implementation(libs.glance.host)
    implementation(libs.glance.preview)
    implementation(libs.legacy.material)

    // in app billing
    implementation(libs.android.billing)

    // admob
    implementation(libs.play.ads)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}