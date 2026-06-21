import java.net.URI
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka)
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

android {
    namespace = "de.codingsolutions.valloview"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.codingsolutions.valloview"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.register("renameApks") {
    notCompatibleWithConfigurationCache("Accesses project object at execution time")
    doLast {
        val gitBranch = try {
            val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
                .redirectErrorStream(true)
                .start()
            val branchName = process.inputStream.bufferedReader().use { it.readLine()?.trim() }
            process.waitFor()
            branchName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

        // Extract project name from applicationId (last part after the last dot)
        val applicationId = android.defaultConfig.applicationId ?: "app"
        val projectName = applicationId.substringAfterLast(".")
        val versionName = android.defaultConfig.versionName ?: "0.0.0"

        val buildOutputDir = layout.buildDirectory.dir("outputs/apk").get().asFile
        if (buildOutputDir.exists()) {
            buildOutputDir.walk()
                .filter { it.isFile && it.name.endsWith(".apk") }
                .forEach { apkFile ->
                    val buildType = when {
                        apkFile.path.contains("debug") -> "debug"
                        apkFile.path.contains("release") -> "release"
                        else -> "unknown"
                    }
                    val newApkName =
                        "$projectName-${versionName}_$gitBranch-$buildType.apk"
                    val newFile = apkFile.parentFile?.resolve(newApkName)
                    if (newFile != null && apkFile != newFile) {
                        apkFile.renameTo(newFile)
                        println("Renamed: ${apkFile.name} -> ${newFile.name}")
                    }
                }
        }
    }
}

afterEvaluate {
    tasks.named("assemble") {
        finalizedBy("renameApks")
    }
}

dependencies {
    "ktlintRuleset"(libs.ktlint.compose.rules)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.okhttp)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

dokka {
    moduleName.set("ValloView")
    moduleVersion.set(project.version.toString())

    dokkaSourceSets.configureEach {
        includes.from(layout.projectDirectory.file("Module.md"))

        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Protected,
                VisibilityModifier.Internal,
            ),
        )

        reportUndocumented.set(true)
        skipEmptyPackages.set(true)

        sourceLink {
            localDirectory.set(layout.projectDirectory.dir("src/main/java"))
            remoteUrl.set(URI("https://github.com/DEIN_GEWÄHLTES_REPO/tree/main/app/src/main/java"))
            remoteLineSuffix.set("#L")
        }
    }

    pluginsConfiguration.html {
        footerMessage.set("(c) 2026 Christian Neukam - ValloView Documentation")
    }
}
