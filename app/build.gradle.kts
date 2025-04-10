plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.manit.hostel.assist.students"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.manit.hostel.assist.students"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("default") {
            storeFile = file("R:/Aprojects/manit.jks")
            storePassword = "bettermanit"
            keyAlias = "hostel"
            keyPassword = "bettermanit"
        }
    }
    buildTypes {

        debug {
            signingConfig = signingConfigs.getByName("default")
        }
        release {
            signingConfig = signingConfigs.getByName("default")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.guolindev.permissionx:permissionx:1.8.1")
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
    implementation("com.android.volley:volley:1.2.1")
    //crashlytics
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation(project(":ROCKUI"))
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")


}