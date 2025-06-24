import os.*
import scala.util.Try
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import upickle.default.*

object TemplateGenerator:
  private val S2D_GROUP_ID = "io.github.finochiom"
  private val S2D_ARTIFACT_ID = "s2d_native0.5_3"
  private lazy val S2D_VERSION = getLatestVersion()

  private def getLatestVersion(): String =
    Try {
      println("Fetching latest version from GitHub tags...")
      val client = HttpClient.newHttpClient()
      val url = "https://api.github.com/repos/FinochioM/S2D/tags"

      val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .build()

      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      val json = ujson.read(response.body())

      if (json.arr.nonEmpty) {
        val latestTag = json.arr(0)("name").str
        val version = if (latestTag.startsWith("v")) latestTag.drop(1) else latestTag
        println(s"Found latest tag: $latestTag -> version: $version")
        version
      } else {
        println("NO TAGS FOUND, DEFAULTING TO 0.1.6")
        "0.1.6"
      }
    }.recover { case e =>
      println(s"ERROR FETCHING VERSION: ${e.getMessage}")
      "0.1.6"
    }.getOrElse("0.1.6")

  def createBuildFile(projectPath: os.Path, buildSystem: String): Unit =
    buildSystem match
      case "sbt" =>
        createSbtProject(projectPath)
      case _ =>
        createScalaCliProject(projectPath)

  private def createSbtProject(projectPath: os.Path): Unit =
    val projectDir = projectPath / "project"
    os.makeDir.all(projectDir)

    val pluginsSbtContent = """addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.8")"""
    os.write(projectDir / "plugins.sbt", pluginsSbtContent)

    val buildPropertiesContent = """sbt.version = 1.11.2"""
    os.write(projectDir / "build.properties", buildPropertiesContent)

    val buildSbtContent = createSbtBuildContent(projectPath)
    os.write(projectPath / "build.sbt", buildSbtContent)

    println("SBT project files created (build.sbt, project/plugins.sbt, project/build.properties)")

  private def createScalaCliProject(projectPath: os.Path): Unit =
    val projectScalaContent = createScalaCliBuildContent(projectPath)
    os.write(projectPath / "project.scala", projectScalaContent)
    println("Scala-CLI project file created (project.scala)")
    println("Version: " + S2D_VERSION)

  private def createSbtBuildContent(projectPath: os.Path): String =
    if OSDetection.isWindows then createWindowsSbtContent(projectPath)
    else createUnixSbtContent(projectPath)

  private def createScalaCliBuildContent(projectPath: os.Path): String =
    if OSDetection.isWindows then createWindowsScalaCliContent(projectPath)
    else createUnixScalaCliContent(projectPath)

  private def createWindowsSbtContent(projectPath: os.Path): String =
    val absoluteProjectPath = projectPath.toString.replace("\\", "\\\\")
    s"""ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "s2d-project",
    libraryDependencies ++= Seq(
      "$S2D_GROUP_ID" % "$S2D_ARTIFACT_ID" % "$S2D_VERSION"
    ),
    nativeConfig ~= { c =>
      c.withLinkingOptions(c.linkingOptions ++ Seq(
        "-L${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\lib",
        "-L${absoluteProjectPath}\\\\libraries\\\\glew\\\\lib\\\\Release\\\\x64",
        "-L${absoluteProjectPath}\\\\libraries\\\\STB",
        "-lSDL2",
        "-lSDL2main",
        "-lglew32",
        "-lopengl32",
        "-lglu32"
      ))
    },
    nativeConfig ~= { c =>
      c.withCompileOptions(c.compileOptions ++ Seq(
        "-I${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\include",
        "-I${absoluteProjectPath}\\\\libraries\\\\glew\\\\include",
        "-I${absoluteProjectPath}\\\\libraries\\\\STB\\\\include"
      ))
    }
  )
  .enablePlugins(ScalaNativePlugin)"""

  private def createUnixSbtContent(projectPath: os.Path): String =
    s"""ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "s2d-project",
    libraryDependencies ++= Seq(
      "$S2D_GROUP_ID" % "$S2D_ARTIFACT_ID" % "$S2D_VERSION"
    ),
    // WARNING: The following configuration is for Windows only
    // You need to manually configure native library linking for your platform:
    // 
    // For Linux: Install SDL2, GLEW, and STB development packages
    // Example: sudo apt-get install libsdl2-dev libglew-dev libstb-dev
    // 
    // For macOS: Install via Homebrew
    // Example: brew install sdl2 glew
    // 
    // Then update the linking options below with correct paths for your system
    
    /* 
    nativeConfig ~= { c =>
      c.withLinkingOptions(c.linkingOptions ++ Seq(
        // Add your platform-specific library paths here
        "-lSDL2",
        "-lSDL2main", 
        "-lGLEW",
        "-lGL"  // or "-framework OpenGL" for macOS
      ))
    },
    nativeConfig ~= { c =>
      c.withCompileOptions(c.compileOptions ++ Seq(
        // Add your platform-specific include paths here
      ))
    }
    */
  )
  .enablePlugins(ScalaNativePlugin)"""

  private def createWindowsScalaCliContent(projectPath: os.Path): String =
    val absoluteProjectPath = projectPath.toString.replace("\\", "\\\\")
    s"""//> using scala 3.3.6
//> using platform native
//> using dep "$S2D_GROUP_ID:$S2D_ARTIFACT_ID:$S2D_VERSION"

//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\include"
//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\STB\\\\include"
//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\glew\\\\include"

//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\lib"
//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\STB\\\\"
//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\glew\\\\lib\\\\Release\\\\x64"
//> using nativeLinking "-lSDL2"
//> using nativeLinking "-lSDL2main"
//> using nativeLinking "-lstb_image"
//> using nativeLinking "-lglew32"
//> using nativeLinking "-lopengl32"
//> using nativeLinking "-lglu32"
"""

  private def createUnixScalaCliContent(projectPath: os.Path): String =
    s"""//> using scala 3.3.6
//> using platform native
//> using dep "$S2D_GROUP_ID:$S2D_ARTIFACT_ID:$S2D_VERSION"

// WARNING: This project was generated on a Unix/Linux/macOS system
// The native library configuration below is commented out and needs manual setup
// 
// For Linux: Install development packages
// Example: sudo apt-get install libsdl2-dev libglew-dev
// 
// For macOS: Install via Homebrew  
// Example: brew install sdl2 glew
//
// Then uncomment and adjust the paths below:

/*
//> using nativeCompile "-I/usr/include/SDL2"
//> using nativeCompile "-I/usr/include/GL" 

//> using nativeLinking "-lSDL2"
//> using nativeLinking "-lSDL2main"
//> using nativeLinking "-lGLEW"
//> using nativeLinking "-lGL"
*/
"""

  def createMainTemplate(projectPath: os.Path): Unit =
    val mainScalaContent = s"""import s2d.core.{Window, Drawing}
import s2d.types.*

object main {
  def main(args: Array[String]): Unit = {
    println("S2D Project Template")
    println("Starting S2D application...")
    
    // S2D library (version $S2D_VERSION) is automatically imported
    // Create a window using S2D
    Window.create(800, 600, "My S2D Application")
    
    // Main game loop
    while !Window.shouldCloseWindow() do
      Drawing.beginFrame()
      Drawing.clear(Color.fromHex("#3498DB").getOrElse(Color.Blue))

      // TODO: Add your game logic here

      Drawing.endFrame()
    Window.close()
    println("S2D application terminated.")
  }
}"""

    os.write(projectPath / "main.scala", mainScalaContent)