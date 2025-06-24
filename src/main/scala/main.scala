@main
def main(args: String*): Unit =
  args.toList match
    case "--generate" :: _ =>
      AsciiArt.show()
      ProjectGenerator.generate()
    case "--help" :: _ =>
      Help.show()
    case "--cancel" :: _ =>
      println("Operation cancelled.")
    case Nil =>
      Help.show()
    case unknown =>
      println(s"Unknown command: ${unknown.mkString(" ")}")
      Help.show()

object AsciiArt:
  def show(): Unit =
    val asciiArt = """
    ╔═══════════════════════════════════════╗
    ║                                       ║
    ║        ███████╗██████╗ ██████╗        ║
    ║        ██╔════╝╚════██╗██╔══██╗       ║
    ║        ███████╗ █████╔╝██║  ██║       ║
    ║        ╚════██║██╔═══╝ ██║  ██║       ║
    ║        ███████║███████╗██████╔╝       ║
    ║        ╚══════╝╚══════╝╚═════╝        ║
    ║                                       ║
    ║         Scala 2D Native Library       ║
    ║               CLI Tool                ║
    ║                                       ║
    ╚═══════════════════════════════════════╝
    """
    println(asciiArt)

object Help:
  def show(): Unit =
    println("S2D - Scala 2D Native Library CLI Tool")
    println()
    println("Usage:")
    println("  s2d --generate    Generate a new S2D project template")
    println("  s2d --help        Show this help message")
    println()