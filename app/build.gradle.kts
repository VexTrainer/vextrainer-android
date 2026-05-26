import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(keystorePropertiesFile.inputStream())

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
//	id("kotlin-kapt")
}

android {
    namespace = "com.vextrainer.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vextrainer.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "1.01"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile     = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias      = keystoreProperties["keyAlias"] as String
            keyPassword   = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
            exclude(group = "org.jetbrains", module = "annotations-java5")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
	// implementation("io.noties.markwon:image:4.6.2")
    implementation(libs.markwon.image)
    //implementation("io.noties.markwon:image-picasso:4.6.2")  // or image-glide
    implementation(libs.markwon.image.picasso)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    // Material Icons Extended needed for Quiz, History, CheckCircle icons
    implementation("androidx.compose.material:material-icons-extended")
    // implementation(libs.material.icons.extended)
    // implementation(libs.materialIconsExtended)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Secure Storage
    implementation(libs.security.crypto)

    // Credential Manager
    // implementation(libs.credentials)
    // implementation(libs.credentials.play)

    // Markdown + Syntax Highlighting
    implementation(libs.markwon.core)
    implementation(libs.markwon.syntax)
    implementation(libs.prism4j)
    // kapt("io.noties:prism4j:2.0.0:bundler")    // generates GrammarLocatorDef from @PrismBundle

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
