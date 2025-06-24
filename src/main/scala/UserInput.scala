object UserInput:
  def getString(prompt: String, defaultValue: String): Option[String] =
    print(s"$prompt [$defaultValue] (or 'cancel' to exit): ")
    val input = scala.io.StdIn.readLine()
    if input.trim.toLowerCase == "cancel" then None
    else Some(if input.trim.isEmpty then defaultValue else input.trim)

  def getBuildSystem(): Option[String] =
    println("Choose build system:")
    println("  1. scala-cli (recommended)")
    println("  2. sbt")
    println("  0. cancel")
    print("Enter choice [1]: ")

    val choice = scala.io.StdIn.readLine()
    choice.trim.toLowerCase match
      case "0" | "cancel" => None
      case "2" => Some("sbt")
      case _ => Some("scala-cli")