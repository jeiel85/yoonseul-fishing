import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException
import java.io.File
import java.util.Properties

abstract class ExportReleaseToDesktopTask : DefaultTask() {
  @get:Input
  abstract val versionName: Property<String>

  @get:Input
  abstract val versionCode: Property<Int>

  @get:InputFile
  abstract val aabFile: RegularFileProperty

  @get:InputFile
  abstract val releaseNotesFile: RegularFileProperty

  @get:Internal
  abstract val projectRootDir: DirectoryProperty

  @TaskAction
  fun export() {
    val home = File(System.getProperty("user.home"))
    val candidates = listOf(
      File(home, "OneDrive/바탕 화면"),
      File(home, "OneDrive/Desktop"),
      File(home, "Desktop")
    )
    val desktop = candidates.firstOrNull { it.isDirectory }
      ?: throw GradleException(
        "Could not find a Desktop directory. Tried:\n" +
          candidates.joinToString("\n") { "  - ${it.absolutePath}" }
      )

    val buildDir = File(desktop, "Build")
    if (!buildDir.exists()) {
      buildDir.mkdirs()
    }

    val aab = aabFile.get().asFile
    if (!aab.isFile) {
      throw GradleException(
        "Release AAB not found at ${aab.absolutePath}. " +
          "bundleRelease should have produced it; check the build log."
      )
    }

    val releaseNotes = releaseNotesFile.get().asFile
    if (!releaseNotes.isFile) {
      throw GradleException(
        "Missing release notes at ${releaseNotes.absolutePath}."
      )
    }

    val releaseNotesText = releaseNotes.readText().trim()
    if (!releaseNotesText.contains("<ko-KR>") || !releaseNotesText.contains("<en-US>")) {
      throw GradleException(
        "Release notes must contain <ko-KR> and <en-US> blocks: ${releaseNotes.absolutePath}"
      )
    }

    val baseName = "YoonseulFishing-v${versionName.get()}-vc${versionCode.get()}"
    val aabTarget = File(buildDir, "$baseName.aab")
    val txtTarget = File(buildDir, "$baseName-release-notes.txt")

    aab.copyTo(aabTarget, overwrite = true)
    txtTarget.writeText(releaseNotesText + System.lineSeparator())

    val storeAssetsDir = File(buildDir, "store-assets")
    if (!storeAssetsDir.exists()) {
      storeAssetsDir.mkdirs()
    }

    val projectRoot = projectRootDir.get().asFile
    val storeGraphics = File(projectRoot, "store-graphics")

    val iconSource = File(storeGraphics, "icon-512.png")
    if (iconSource.isFile) {
      val iconTarget = File(storeAssetsDir, "icon-512.png")
      iconSource.copyTo(iconTarget, overwrite = true)
      logger.lifecycle("Wrote ${iconTarget.absolutePath} (${iconTarget.length()} bytes)")
    }

    val featureGraphicSource = File(storeGraphics, "feature-graphic-1024x500.png")
    if (featureGraphicSource.isFile) {
      val fgTarget = File(storeAssetsDir, "feature-graphic-1024x500.png")
      featureGraphicSource.copyTo(fgTarget, overwrite = true)
      logger.lifecycle("Wrote ${fgTarget.absolutePath} (${fgTarget.length()} bytes)")
    }

    val descriptionsSource = File(storeGraphics, "play-console-descriptions")
    if (descriptionsSource.isDirectory) {
      val descriptionsTarget = File(storeAssetsDir, "play-console-descriptions")
      descriptionsTarget.mkdirs()
      descriptionsSource.listFiles()?.forEach { file ->
        if (file.isFile) {
          val fileTarget = File(descriptionsTarget, file.name)
          file.copyTo(fileTarget, overwrite = true)
          logger.lifecycle("Wrote ${fileTarget.absolutePath} (${fileTarget.length()} bytes)")
        }
      }
    }

    logger.lifecycle("Wrote ${aabTarget.absolutePath} (${aabTarget.length()} bytes)")
    logger.lifecycle("Wrote ${txtTarget.absolutePath} (${txtTarget.length()} bytes)")
  }
}

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.jeiel85.healingfishing"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.jeiel85.healingfishing"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // Credentials live in the gitignored .keystore/keystore.properties (never committed).
      // CI / other machines can fall back to KEYSTORE_PATH / STORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD env vars.
      val keystorePropsFile = rootProject.file(".keystore/keystore.properties")
      if (keystorePropsFile.exists()) {
        val props = Properties()
        keystorePropsFile.inputStream().use { stream -> props.load(stream) }
        storeFile = rootProject.file(props.getProperty("storeFile"))
        storePassword = props.getProperty("storePassword")
        keyAlias = props.getProperty("keyAlias")
        keyPassword = props.getProperty("keyPassword")
      } else {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "${rootDir}/.keystore/yoonseul-fishing-upload.jks")
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // signingConfig = signingConfigs.getByName("debugConfig")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

val exportVersionName = android.defaultConfig.versionName
  ?: throw GradleException("versionName is not set in defaultConfig")
val exportVersionCode = android.defaultConfig.versionCode
  ?: throw GradleException("versionCode is not set in defaultConfig")
val exportReleaseAab = layout.buildDirectory.file("outputs/bundle/release/app-release.aab")
val exportReleaseNotes = rootProject.layout.projectDirectory.file("store-graphics/play-console-current/release-notes.txt")

tasks.register<ExportReleaseToDesktopTask>("exportReleaseToDesktop") {
  group = "healingfishing"
  description = "Copies the release AAB and Play Console release notes to the Build directory on user's Desktop"

  dependsOn("bundleRelease")
  versionName.set(exportVersionName)
  versionCode.set(exportVersionCode)
  aabFile.set(exportReleaseAab)
  releaseNotesFile.set(exportReleaseNotes)
  projectRootDir.set(rootProject.layout.projectDirectory)
}
