plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Motor moderno KSP para el compilador de Room
    id("com.google.gms.google-services") // Plugin indispensable para vincular los Web Services de Firebase
    id("org.jetbrains.dokka")
}

android {
    namespace = "com.iax.gestordeturnos"
    compileSdk = 35 // Configurado para dar soporte a las últimas API multimedia de Android 15

    defaultConfig {
        applicationId = "com.iax.gestordeturnos"
        minSdk = 26 // SDK mínimo requerido para el uso nativo de java.time sin des-azucarado (LocalDate, YearMonth)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true // Enciende el compilador declarativo para renderizar las pantallas
    }
}
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            moduleName.set("Gestor de Turnos - Documentación API")
            includes.from("README.md") // Integra tu archivo de presentación como portada de la web

            // Permite que se visualicen los autores Lucas y Juan Pedro en la web
            skipEmptyPackages.set(true)
        }
    }
}
kotlin {
    jvmToolchain(21) // Sincroniza las herramientas de compilación de Kotlin a la versión 21 de Java
}

dependencies {
    // --- 1. CAPA DE PRESENTACIÓN / JETPACK COMPOSE DE CORE (Sincronizado vía BOM) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)

    // --- 2. CAPA DE PERSISTENCIA LOCAL / JETPACK ROOM (BBDD SQLite) ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    androidTestImplementation(libs.androidx.core.testing) // Extensiones de corrutinas para permitir funciones 'suspend' en los DAOs
    ksp(libs.room.compiler) // Compilador optimizado sobre KSP para generar el código relacional subyacente

    // --- 3. INFRAESTRUCTURA DE NAVEGACIÓN Y COMPOSICIÓN DE ARQUITECTURA ---
    implementation("androidx.navigation:navigation-compose:2.7.7") // Orquestador central NavHost
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Delegación y ciclo de vida MVVM
    implementation("com.github.skydoves:colorpicker-compose:1.1.3") // Selector multimedia interactivo de espectro HSV

    // --- 4. PRUEBAS UNITARIAS Y DE INTEGRACIÓN (TESTING) ---
    testImplementation(libs.junit)
    testImplementation(libs.core.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Emulador de hilos para probar Corrutinas/Flow
    testImplementation(libs.androidx.core.testing) // Regla para forzar la ejecución síncrona de tareas de Jetpack
    testImplementation("org.mockito:mockito-core:5.7.0") // Permite mockear componentes remotos como FirebaseAuth
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // Extensiones sintácticas de Mockito para Kotlin
    testImplementation("org.robolectric:robolectric:4.11.1")




    // --- 5. INFRAESTRUCTURA DE RED / FIREBASE BOM (Plataforma única de versiones) ---
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    // Modificadores multimedia remotos de Firebase inyectados de forma unificada (Sin especificar versiones duplicadas)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Requisito indispensable de red consumido por el AuthViewModel
    implementation("com.google.firebase:firebase-firestore") // Base de datos en la nube preparada para futuras sincronizaciones
}
