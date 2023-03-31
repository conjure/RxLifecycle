import kotlinx.html.dom.document
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

android {
    namespace = "uk.co.conjure.rxlifecycle"

    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components["release"])

                // You can then customize attributes of the publication as shown below.
                groupId = "uk.co.conjure"
                artifactId = "rxlifecycle"
                version = "1.0.0-alpha02"
            }
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        templatesDir = file("dokka/templates")
        footerMessage = "(c) 2022 Conjure Ltd."
        separateInheritedMembers = false
        mergeImplicitExpectActualDeclarations = false

    }
    dokkaSourceSets.configureEach {

        documentedVisibilities.set(
            setOf(
                Visibility.PUBLIC,
                Visibility.PROTECTED,
            )
        )

        val readmeFile = file("$projectDir/README.md")
        // If the module has a README, add it to the module's index
        if (readmeFile.exists()) {
            includes.from(readmeFile)
        }
    }
}


dependencies {
    api("com.github.conjure:view-lifecycle:1.0.0-alpha01")

    api("io.reactivex.rxjava3:rxjava:3.1.5")
    api("io.reactivex.rxjava3:rxkotlin:3.0.1")
    api("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.jakewharton.rxbinding4:rxbinding:4.0.0")
    implementation("com.jakewharton.rxbinding4:rxbinding-material:4.0.0")

    implementation("androidx.appcompat:appcompat:1.5.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.lifecycle:lifecycle-runtime-testing:2.5.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.8.10")
    }
}