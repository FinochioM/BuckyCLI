import os.*

object TemplateGenerator:
  private val S2D_GROUP_ID = "io.github.finochiom"
  private val S2D_ARTIFACT_ID = "s2d_native0.5_3"
  private val S2D_VERSION = "1.0.1"

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

    val buildSbtContent = createSbtBuildContent()
    os.write(projectPath / "build.sbt", buildSbtContent)

    println("SBT project files created (build.sbt, project/plugins.sbt, project/build.properties)")

  private def createScalaCliProject(projectPath: os.Path): Unit =
    val projectScalaContent = createScalaCliBuildContent(projectPath)
    os.write(projectPath / "project.scala", projectScalaContent)
    println("Scala-CLI project file created (project.scala)")

  private def createSbtBuildContent(): String =
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
        "-Llibraries/SDL2/lib",
        "-Llibraries/glew/lib/Release/x64",
        "-Llibraries/STB",
        "-lSDL2",
        "-lSDL2main",
        "-lglew32",
        "-lopengl32",
        "-lglu32"
      ))
    },
    nativeConfig ~= { c =>
      c.withCompileOptions(c.compileOptions ++ Seq(
        "-Ilibraries/SDL2/include",
        "-Ilibraries/glew/include",
        "-Ilibraries/STB/include"
      ))
    }
  )
  .enablePlugins(ScalaNativePlugin)"""

  private def createScalaCliBuildContent(projectPath: os.Path): String =
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