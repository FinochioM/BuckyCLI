
object OSDetection:
    val isWindows: Boolean = System.getProperty("os.name").toLowerCase.contains("win")
    val isLinux: Boolean = System.getProperty("os.name").toLowerCase.contains("linux")
    val isMac: Boolean = System.getProperty("os.name").toLowerCase.contains("mac")