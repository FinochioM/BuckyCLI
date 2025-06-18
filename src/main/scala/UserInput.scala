object UserInput:
  def getString(prompt: String, defaultValue: String): String =
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