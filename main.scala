@main
def main(args: String*): Unit =
  args.toList match
    case "--generate" :: _ =>
      showAsciiArt()
      generateTemplate()
    case "--help" :: _ =>
      showHelp()
    case Nil | Nil =>
      showHelp()
    case unknown =>
      println(s"Unknown argument: ${unknown.mkString(" ")}")
      showHelp()

def showAsciiArt(): Unit =
  val asciiArt =
    """
      |  ╔═══════════════════════════════════════╗
      |  ║                                       ║
      |  ║        ███████╗██████╗ ██████╗        ║
      |  ║        ██╔════╝╚════██╗██╔══██╗       ║
      |  ║        ███████╗ █████╔╝██║  ██║       ║
      |  ║        ╚════██║██╔═══╝ ██║  ██║       ║
      |  ║        ███████║███████╗██████╔╝       ║
      |  ║        ╚══════╝╚══════╝╚═════╝        ║
      |  ║                                       ║
      |  ║         Scala 2D Native Library       ║
      |  ║             CLI Application           ║
      |  ║                                       ║
      |  ╚═══════════════════════════════════════╝
      |""".stripMargin
  println(asciiArt)

def showHelp(): Unit =
  println(
    """
      |Usage: scala Main.scala [options]
      |
      |Options:
      |  --generate   Generate a new template
      |  --help       Show this help message
      |
      |Examples:
      |  scala Main.scala --generate
      |  scala Main.scala --help
      |""".stripMargin
  )

def generateTemplate(): Unit =
  println("Starting S2D project template generation...")
  println()

  // Get project information from user
  val projectName = getUserInput("Enter project name", "my-s2d-project")
  val buildSystem = getBuildSystem
  val projectPath = getUserInput("Enter project path", ".")

  println(s"Creating project '$projectName' at '$projectPath'")
  println(s"Using build system: $buildSystem")

  // TODO: Implement actual template generation
  println("⚠Template generation not yet implemented")

def getUserInput(prompt: String, defaultValue: String): String =
  print(s"$prompt [$defaultValue]: ")
  val input = scala.io.StdIn.readLine()
  if input.trim.isEmpty then defaultValue else input.trim

def getBuildSystem: String =
  println("Choose build system:")
  println("1. scala-cli (recommended)")
  println("2. sbt")
  print("Enter choice [1]: ")

  val choice = scala.io.StdIn.readLine()
  choice.trim match
    case "2" => "sbt"
    case _ => "scala-cli"