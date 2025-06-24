import os.*
import org.eclipse.jgit.api.Git

object ProjectGenerator:
  def generate(): Unit =
    println("Starting S2D project template generation...")
    println()

    val projectName = UserInput.getString("Enter project name", "my-s2d-project") match
      case Some(name) => name
      case None => 
        println("Operation cancelled.")
        return

    val buildSystem = UserInput.getBuildSystem() match
      case Some(system) => system
      case None =>
        println("Operation cancelled.")
        return

    val projectPath = UserInput.getString("Enter project path", ".") match
      case Some(path) => path
      case None =>
        println("Operation cancelled.")
        return

    println(s"Creating project '$projectName' at '$projectPath'")
    println(s"Using build system: $buildSystem")
    println()

    createProjectStructure(projectName, projectPath, buildSystem)

  private def createProjectStructure(projectName: String, projectPath: String, buildSystem: String): Unit =
    try
      val basePath = os.Path(projectPath, os.pwd)
      val fullProjectPath = basePath / projectName

      println("Creating project directory...")
      os.makeDir.all(fullProjectPath)

      println("Downloading S2D libraries...")
      cloneLibraries(fullProjectPath)

      println("Setting up project files...")
      TemplateGenerator.createBuildFile(fullProjectPath, buildSystem)
      TemplateGenerator.createMainTemplate(fullProjectPath)
      copyRequiredNativeLibraries(fullProjectPath)

      println("Creating assets directory...")
      os.makeDir.all(fullProjectPath / "assets")

      println()
      println(s"Project '$projectName' created successfully!")
      println(s"Location: ${fullProjectPath.toString}")

      if !OSDetection.isWindows then
        println()
        println("  WARNING: Generated project requires manual configuration on non-Windows systems")
        println("   - Install SDL2, GLEW, and STB development libraries for your platform")
        println("   - Update the native linking paths in your build configuration")
        if OSDetection.isLinux then
          println("   - For Ubuntu/Debian: sudo apt-get install libsdl2-dev libglew-dev")
          println("   - For Fedora: sudo dnf install SDL2-devel glew-devel")
        else if OSDetection.isMac then
          println("   - For macOS: brew install sdl2 glew")

      println()
      println("To build and run your project:")
      buildSystem match
        case "sbt" =>
          println(s"  cd $projectName")
          println("  sbt run")
        case _ =>
          println(s"  cd $projectName")
          println("  scala-cli run .")

      println()
      println("Your project includes:")
      println("  - S2D library - automatically imported")
      if OSDetection.isWindows then
        println("  - SDL2, GLEW, and STB libraries - automatically configured")
        println("  - Required DLLs - copied to project root")
      else
        println("  - SDL2, GLEW, and STB libraries - manual setup required")
        println("  - Native libraries - install manually for your platform")
      println("  - Template main.scala with S2D Window creation")

    catch
      case e: Exception =>
        println(s"Error creating project: ${e.getMessage}")
        e.printStackTrace()

  private def cloneLibraries(projectPath: os.Path): Unit =
    val librariesUrl = "https://github.com/FinochioM/S2D_Libraries.git"
    val librariesPath = projectPath / "libraries"

    Git.cloneRepository()
      .setURI(librariesUrl)
      .setDirectory(librariesPath.toIO)
      .call()
      .close()

  private def copyRequiredNativeLibraries(projectPath: os.Path): Unit =
    if OSDetection.isWindows then
      copyRequiredDlls(projectPath)
    else
      println("Warning: Native library files (.so/.dylib) not copied - Windows DLLs don't work on this platform")
      println("You'll need to install SDL2, GLEW, and STB libraries for your system manually")

  private def copyRequiredDlls(projectPath: os.Path): Unit =
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