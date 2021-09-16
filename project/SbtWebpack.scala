import java.io.PrintWriter
import java.nio.file.Paths

import com.typesafe.sbt.web._
import com.typesafe.sbt.web.incremental._
import sbt.Keys._
import sbt._
import xsbti.{Position, Problem, Severity}

import scala.io.Source

/**
  * Runs `webpack` command in assets.
  */
object SbtWebpack extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  object autoImport {
    object WebpackKeys {
      val webpack = TaskKey[Seq[File]]("webpack", "Run webpack")
      val binary = SettingKey[File]("webpackBinary", "The location of webpack binary")
      val configFile = SettingKey[File]("webpackConfigFile", "The location of webpack.config.js")
      val nodeModulesPath = TaskKey[File]("webpackNodeModules", "The location of the node_modules.")
      val sourceDirs = SettingKey[Seq[File]]("webpackSourceDirs", "The directories that contains source files.")
    }
  }

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport.WebpackKeys._
  import SbtNpm.autoImport.NpmKeys

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Assets)(
      Seq(
        binary in webpack := (Assets / sourceDirectory).value / "node_modules" / ".bin" / "webpack",
        configFile in webpack := (Assets / sourceDirectory).value / "webpack.config.js",
        sourceDirs in webpack := Seq((Assets / sourceDirectory).value),
        excludeFilter in webpack := HiddenFileFilter,
        includeFilter in webpack := "*.js" | "*.ts",
        nodeModulesPath := new File("./node_modules"),
        resourceManaged in webpack := webTarget.value / "webpack" / "build",
        managedResourceDirectories in Assets += (resourceManaged in webpack in Assets).value,
        resourceGenerators in Assets += webpack in Assets,
        webpack in Assets := task
          .dependsOn(WebKeys.webModules in Assets)
          //.dependsOn(NpmKeys.npmInstall in Assets)
          .value,
        // Because sbt-webpack might compile JS and output into the same file.
        // Therefore, we need to deduplicate the files by choosing the one in the target directory.
        // Otherwise, the "duplicate mappings" error would occur.
        deduplicators in Assets += {
          val targetDir = (resourceManaged in webpack in Assets).value
          val targetDirAbsolutePath = targetDir.getAbsolutePath

          { files: Seq[File] => files.find(_.getAbsolutePath.startsWith(targetDirAbsolutePath)) }
        }
      )
    )

  private[this] def readAndClose(file: File): String = {
    val s = Source.fromFile(file)

    try s.mkString
    finally s.close()
  }

  lazy val task = Def.task {
    val baseDir = (sourceDirectory in Assets).value
    val targetDir = (resourceManaged in webpack in Assets).value
    val logger = (streams in Assets).value.log
    val nodeModulesLocation = (nodeModulesPath in webpack).value
    val webpackSourceDirs = (sourceDirs in webpack).value
    val webpackReporter = (reporter in Assets).value
    val webpackBinaryLocation = (binary in webpack).value
    val webpackConfigFileLocation = (configFile in webpack).value

    val sources = webpackSourceDirs
      .flatMap { sourceDir =>
        (sourceDir ** ((includeFilter in webpack).value -- (excludeFilter in webpack).value)).get
      }
      .filter(_.isFile)

    val globalHash = new String(
      Hash(
        Seq(
          readAndClose(webpackConfigFileLocation),
          state.value.currentCommand.map(_.commandLine).getOrElse(""),
          sys.env.toList.sorted.toString
        ).mkString("--")
      )
    )

    val fileHasherIncludingOptions = OpInputHasher[File] { f =>
      OpInputHash.hashString(
        Seq(
          "sbt-webpack-0.6.0",
          f.getCanonicalPath,
          baseDir.getAbsolutePath,
          globalHash
        ).mkString("--")
      )
    }

    val results = incremental.syncIncremental((streams in Assets).value.cacheDirectory / "run", sources) {
      modifiedSources =>
        val startInstant = System.currentTimeMillis

        if (modifiedSources.nonEmpty) {
          logger.info(s"""
                         |[sbt-webpack] Detected ${modifiedSources.size} changed files in:
                         |${webpackSourceDirs.map(d => s"- ${d.getCanonicalPath}").mkString("\n")}
           """.stripMargin.trim)

          val compiler = new Webpack.Compiler(
            webpackBinaryLocation,
            webpackConfigFileLocation,
            baseDir,
            targetDir,
            logger,
            nodeModulesLocation
          )

          // Compile all modified sources at once
          val result = compiler
            .compile(
              (resourceManaged in webpack).value.toPath(),
              (webJarsDirectory in Assets).value.toPath()
            )

          // Report compilation problems
          CompileProblems.report(
            reporter = webpackReporter,
            problems =
              if (!result.success)
                Seq(new Problem {
                  override def category() = ""

                  override def severity() = Severity.Error

                  override def message() = ""

                  override def position() =
                    new Position {
                      override def line() = java.util.Optional.empty()

                      override def lineContent() = ""

                      override def offset() = java.util.Optional.empty()

                      override def pointer() = java.util.Optional.empty()

                      override def pointerSpace() = java.util.Optional.empty()

                      override def sourcePath() = java.util.Optional.empty()

                      override def sourceFile() = java.util.Optional.empty()
                    }
                })
              else Seq.empty
          )

          val opResults = result.entries
            .filter { entry =>
              // Webpack might generate extra files from extra input files. We can't track those input files.
              modifiedSources.exists(f => f.getCanonicalPath == entry.inputFile.getCanonicalPath)
            }
            .map { entry =>
              entry.inputFile -> OpSuccess(entry.filesRead, entry.filesWritten)
            }
            .toMap

          // The below is important for excluding unrelated files in the next recompilation.
          val resultInputFilePaths = result.entries.map(_.inputFile.getCanonicalPath)
          val unrelatedOpResults = modifiedSources
            .filterNot(file => resultInputFilePaths.contains(file.getCanonicalPath))
            .map { file =>
              file -> OpSuccess(Set(file), Set.empty)
            }
            .toMap

          val createdFiles = result.entries.flatMap(_.filesWritten).distinct
          val endInstant = System.currentTimeMillis

          logger.info(
            s"[sbt-webpack] Finished compilation in ${endInstant - startInstant} ms"
          )
          createdFiles
            .map(_.toString)
            .sorted
            .foreach { s =>
              logger.info(s"[sbt-webpack] - $s")
            }

          (opResults ++ unrelatedOpResults, createdFiles)
        } else {
          logger.info(s"[sbt-webpack] No changes to compile")
          (Map.empty, Seq.empty)
        }

    }(fileHasherIncludingOptions)

    // Return the dependencies
    (results._1 ++ results._2.toSet).toSeq

  }

  object Webpack {

    import java.io.{File, FileOutputStream, PrintWriter}
    import java.nio.file.{Files, Path}
    import sbt.internal.util.ManagedLogger
    import scala.io.Source
    import play.sbt.PlayRunHook
    import sbt.internal.util.ConsoleLogger
    import scala.util.Properties
    import scala.sys.process.Process

    case class CompilationResult(success: Boolean, entries: Seq[CompilationEntry])
    case class CompilationEntry(inputFile: File, filesRead: Set[File], filesWritten: Set[File])
    case class Input(name: String, path: Path)

    class Shell {
      def execute(cmd: String, cwd: File, envs: (String, String)*): Int =
        Process(cmd, cwd, envs: _*).!
    }

    class Compiler(
      binary: File,
      configFile: File,
      baseDir: File,
      targetDir: File,
      logger: ManagedLogger,
      nodeModules: File,
      shell: Shell = new Shell
    ) {

      def getFile(path: String): File =
        if (path.startsWith("/"))
          new File(path)
        else
          targetDir.toPath.resolve(path).toFile.getCanonicalFile

      def compile(outputDirectory: Path, webjarsDirectory: Path): CompilationResult = {
        import sbt._

        val cmd = Seq(
          binary.getCanonicalPath,
          "--config",
          configFile.getAbsolutePath(),
          "--env",
          "prod",
          "--env",
          s"""outputDir=${outputDirectory.toAbsolutePath()}""",
          "--env",
          s"""webjarsDir=${webjarsDirectory.toAbsolutePath()}"""
        ).mkString(" ")

        logger.info(cmd)
        val exitCode = shell.execute(cmd, baseDir, "NODE_PATH" -> nodeModules.getCanonicalPath)
        logger.info(s"[sbt-webpack] Exited with $exitCode")
        val success = exitCode == 0

        CompilationResult(
          success = success,
          entries = Seq.empty
        )
      }

      def getTransitivity(filesRead: Set[String], filesReadMap: Map[String, Set[String]]): Set[String] = {
        val newFileReads = filesRead
          .flatMap { read =>
            Set(read) ++ filesReadMap.getOrElse(read, Set.empty)
          }

        if (newFileReads.size != filesRead.size)
          getTransitivity(newFileReads, filesReadMap)
        else
          newFileReads
      }
    }
  }
}
