import com.jsuereth.sbtpgp.PgpKeys.publishSignedConfiguration

ThisBuild / version := "0.1.1"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "io.github.finochiom"
ThisBuild / versionScheme := Some("early-semver")

lazy val root = (project in file("."))
  .settings(
    name := "s2d-cli",
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit" % "6.7.0.202309050840-r",
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "com.lihaoyi" %% "upickle" % "4.2.1"
    ),

    homepage := Some(url("https://github.com/FinochioM/S2D_CLI")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/FinochioM/S2D_CLI"),
        "scm:git@github.com:FinochioM/S2D_CLI.git"
      )
    ),

    pgpSigningKey := Some("FD3BD1C64C106A9B"),

    developers := List(
      Developer(
        id = "FinochioM",
        name = "Matias Finochio",
        email = "matias.finochio@davinci.edu.ar",
        url = url("https://github.com/FinochioM")
      )
    ),

    description := "The S2D CLI tool for creating and managing S2D projects",
    licenses := Seq("zlib" -> url("https://zlib.net/zlib_license.html")),

    publishMavenStyle := true,
    publishTo := Some(Resolver.file("local-repo", file(sys.props("user.home") + "/.m2/repository"))),

    Compile / packageDoc / publishArtifact := true,
    Compile / packageSrc / publishArtifact := true,
    Test / publishArtifact := false
  )