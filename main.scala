import os.*
import org.eclipse.jgit.api.Git
import java.io.File

@main
def main(args: String*): Unit =
  args.toList match
    case "--generate" :: _ =>
      showAsciiArt()
      generateTemplate()
    case "--help" :: _ =>
      showHelp()
    case Nil =>
      showHelp()
    case unknown =>
      println(s"Unknown command: ${unknown.mkString(" ")}")
      showHelp()

def showAsciiArt(): Unit =
  val asciiArt = """
  ╔═══════════════════════════════════════╗
  ║                                       ║
  ║       ███████╗██████╗ ██████╗         ║
  ║       ██╔════╝╚════██╗██╔══██╗        ║
  ║       ███████╗ █████╔╝██║  ██║        ║
  ║       ╚════██║██╔═══╝ ██║  ██║        ║
  ║       ███████║███████╗██████╔╝        ║
  ║       ╚══════╝╚══════╝╚═════╝         ║
  ║                                       ║
  ║         Scala 2D Native Library       ║
  ║               CLI Tool                ║
  ║                                       ║
  ╚═══════════════════════════════════════╝
  """
  println(asciiArt)

def showHelp(): Unit =
  println("S2D - Scala 2D Native Library Template Generator")
  println()
  println("Usage:")
  println("  s2d --generate    Generate a new S2D project template")
  println("  s2d --help        Show this help message")
  println()

def generateTemplate(): Unit =
  println("Starting S2D project template generation...")
  println()

  val projectName = getUserInput("Enter project name", "my-s2d-project")
  val buildSystem = getBuildSystem()
  val projectPath = getUserInput("Enter project path", ".")

  println(s"Creating project '$projectName' at '$projectPath'")
  println(s"Using build system: $buildSystem")
  println()

  createProjectStructure(projectName, projectPath, buildSystem)

def getUserInput(prompt: String, defaultValue: String): String =
  print(s"$prompt [$defaultValue]: ")
  val input = scala.io.StdIn.readLine()
  if input.trim.isEmpty then defaultValue else input.trim

def getBuildSystem(): String =
  println("Choose build system:")
  println("  1. scala-cli (recommended)")
  println("  2. sbt")
  print("Enter choice [1]: ")

  val choice = scala.io.StdIn.readLine()
  choice.trim match
    case "2" => "sbt"
    case _ => "scala-cli"

def createProjectStructure(projectName: String, projectPath: String, buildSystem: String): Unit =
  try
    val basePath = os.Path(projectPath)
    val fullProjectPath = basePath / projectName

    println("Creating project directory...")
    os.makeDir.all(fullProjectPath)

    println("Downloading S2D libraries...")
    cloneLibraries(fullProjectPath)

    println("Setting up project files...")
    createBuildFile(fullProjectPath, buildSystem)
    createMainTemplate(fullProjectPath)
    copyRequiredDlls(fullProjectPath)

    // Create assets directory for resources
    println("Creating assets directory...")
    os.makeDir.all(fullProjectPath / "assets")

    println()
    println(s"Project '$projectName' created successfully!")
    println(s"Location: ${fullProjectPath.toString}")
    println()
    println("To build and run your project:")
    buildSystem match
      case "sbt" =>
        println(s"  cd $projectName")
        println("  sbt run")
      case _ =>
        println(s"  cd $projectName")
        println("  scala-cli run .")

  catch
    case e: Exception =>
      println(s"Error creating project: ${e.getMessage}")
      e.printStackTrace()

def cloneLibraries(projectPath: os.Path): Unit =
  val librariesUrl = "https://github.com/FinochioM/S2D_Libraries.git"
  val librariesPath = projectPath / "libraries"

  Git.cloneRepository()
    .setURI(librariesUrl)
    .setDirectory(librariesPath.toIO)
    .call()
    .close()

def createBuildFile(projectPath: os.Path, buildSystem: String): Unit =
  buildSystem match
    case "sbt" =>
      // Create project directory for SBT
      val projectDir = projectPath / "project"
      os.makeDir.all(projectDir)

      // Create plugins.sbt
      val pluginsSbtContent = """addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.8")"""
      os.write(projectDir / "plugins.sbt", pluginsSbtContent)

      // Create build.properties
      val buildPropertiesContent = """sbt.version = 1.11.2"""
      os.write(projectDir / "build.properties", buildPropertiesContent)

      // Create main build.sbt
      val buildSbtContent = createSbtBuildContent()
      os.write(projectPath / "build.sbt", buildSbtContent)

      println("SBT project files created (build.sbt, project/plugins.sbt, project/build.properties)")

    case _ =>
      val projectScalaContent = createScalaCliBuildContent(projectPath)
      os.write(projectPath / "project.scala", projectScalaContent)

      println("Scala-CLI project file created (project.scala)")

def createSbtBuildContent(): String =
  """ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "s2d-project",
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-library" % scalaVersion.value
    ),
    nativeConfig ~= { c =>
      c.withLinkingOptions(c.linkingOptions ++ Seq(
        "-Llibraries/SDL2/lib",
        "-Llibraries/glew/lib/Release/x64",
        "-Llibraries/STB/lib",
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

def createScalaCliBuildContent(projectPath: os.Path): String =
  val absoluteProjectPath = projectPath.toString.replace("\\", "\\\\")
  s"""//> using scala 3.3.6
//> using platform native

//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\include"
//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\STB\\\\include"
//> using nativeCompile "-I${absoluteProjectPath}\\\\libraries\\\\glew\\\\include"

//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\SDL2\\\\lib"
//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\STB\\\\lib"
//> using nativeLinking "-L${absoluteProjectPath}\\\\libraries\\\\glew\\\\lib\\\\Release\\\\x64"
//> using nativeLinking "-lSDL2"
//> using nativeLinking "-lSDL2main"
//> using nativeLinking "-lstb_image"
//> using nativeLinking "-lglew32"
//> using nativeLinking "-lopengl32"
//> using nativeLinking "-lglu32"
"""

def createMainTemplate(projectPath: os.Path): Unit =
  val mainScalaContent = """import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object Main {
  def main(args: Array[String]): Unit = {
    println("S2D Project Template")
    println("Starting S2D application...")

    // Initialize SDL2
    // TODO: Add your SDL2 initialization code here
    // TODO: Create window using your S2D library
    // TODO: Main game/application loop

    println("S2D application terminated.")
  }
}"""

  os.write(projectPath / "main.scala", mainScalaContent)

def copyRequiredDlls(projectPath: os.Path): Unit =
  val sdl2Dll = projectPath / "libraries" / "SDL2" / "bin" / "SDL2.dll"
  val glewDll = projectPath / "libraries" / "glew" / "bin" / "Release" / "x64" / "glew32.dll"

  println("Copying required DLLs...")

  if (os.exists(sdl2Dll)) {
    os.copy(sdl2Dll, projectPath / "SDL2.dll")
    println("SDL2.dll copied successfully")
  } else {
    println(s"Warning: SDL2.dll not found at ${sdl2Dll}")
  }

  if (os.exists(glewDll)) {
    os.copy(glewDll, projectPath / "glew32.dll")
    println("glew32.dll copied successfully")
  } else {
    println(s"Warning: glew32.dll not found at ${glewDll}")
  }