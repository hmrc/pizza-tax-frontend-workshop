import java.io.PrintWriter
import java.nio.file.Paths

import com.typesafe.sbt.web._
import com.typesafe.sbt.web.incremental._
import sbt.Keys._
import sbt._
import xsbti.{Position, Problem, Severity}

import scala.io.Source
import sbt.internal.util.ManagedLogger
import xsbti.Reporter
import scala.sys.process.ProcessLogger
import scala.io.AnsiColor

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
      val entries = SettingKey[Seq[String]](
        "webpackEntries",
        "The entry point pseudo-paths. If the path starts with `assets:` it will be resolved in assests source directory, if the path starts with `webjar:` it will be resolved in the webjars/lib target directory."
      )
      val outputFileName =
        SettingKey[String]("outputFileName", "The name of the webpack output file, default to application.min.js")
    }
  }

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport.WebpackKeys
  import SbtNpm.autoImport.NpmKeys

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Assets)(
      Seq(
        WebpackKeys.binary in WebpackKeys.webpack := (Assets / sourceDirectory).value / "node_modules" / ".bin" / "webpack",
        WebpackKeys.configFile in WebpackKeys.webpack := (Assets / sourceDirectory).value / "webpack.config.js",
        WebpackKeys.sourceDirs in WebpackKeys.webpack := Seq((Assets / sourceDirectory).value),
        WebpackKeys.nodeModulesPath := new File("./node_modules"),
        WebpackKeys.webpack in Assets := task
          .dependsOn(WebKeys.webModules in Assets)
          //.dependsOn(NpmKeys.npmInstall in Assets)
          .value,
        // Because sbt-webpack might compile JS and output into the same file.
        // Therefore, we need to deduplicate the files by choosing the one in the target directory.
        // Otherwise, the "duplicate mappings" error would occur.
        excludeFilter in WebpackKeys.webpack := HiddenFileFilter ||
          new FileFilter {
            override def accept(file: File): Boolean = {
              val path = file.getAbsolutePath()
              path.contains("/node_modules/") ||
              path.contains("/target/") ||
              path.contains("/build/")
            }
          },
        includeFilter in WebpackKeys.webpack := "*.js" || "*.ts",
        resourceManaged in WebpackKeys.webpack := webTarget.value / "webpack" / "build",
        managedResourceDirectories in Assets += (resourceManaged in WebpackKeys.webpack in Assets).value,
        resourceGenerators in Assets += WebpackKeys.webpack in Assets,
        deduplicators in Assets += {
          val targetDir = (resourceManaged in WebpackKeys.webpack in Assets).value
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

    val baseDir: File = (sourceDirectory in Assets).value
    val targetDir: File = (resourceManaged in WebpackKeys.webpack in Assets).value
    val logger: ManagedLogger = (streams in Assets).value.log
    val nodeModulesLocation: File = (WebpackKeys.nodeModulesPath in WebpackKeys.webpack).value
    val webpackSourceDirs: Seq[File] = (WebpackKeys.sourceDirs in WebpackKeys.webpack).value
    val webpackReporter: Reporter = (reporter in Assets).value
    val webpackBinaryLocation: File = (WebpackKeys.binary in WebpackKeys.webpack).value
    val webpackConfigFileLocation: File = (WebpackKeys.configFile in WebpackKeys.webpack).value
    val webpackEntries: Seq[String] = (WebpackKeys.entries in WebpackKeys.webpack).value
    val webpackOutputFileName: String = (WebpackKeys.outputFileName in WebpackKeys.webpack).value
    val webpackTargetDir: File = (resourceManaged in WebpackKeys.webpack).value
    val assetsWebJarsLocation: File = (webJarsDirectory in Assets).value

    val sources: Seq[File] = webpackSourceDirs
      .flatMap { sourceDir =>
        (sourceDir ** ((includeFilter in WebpackKeys.webpack).value -- (excludeFilter in WebpackKeys.webpack).value)).get
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
          "sbt-webpack",
          f.getCanonicalPath,
          baseDir.getAbsolutePath,
          globalHash
        ).mkString("--")
      )
    }

    val results = incremental.syncIncremental((streams in Assets).value.cacheDirectory / "run", sources) {
      modifiedSources =>
        val startInstant = System.currentTimeMillis
        val projectRoot = baseDirectory.value

        if (modifiedSources.nonEmpty) {
          logger.info(s"""
                         |[sbt-webpack] Detected ${modifiedSources.size} changed files:
                         |[sbt-webpack]\t${modifiedSources.map(f => f.relativeTo(baseDir).getOrElse(f).toString()).mkString("\n[sbt-webpack]\t")}
           """.stripMargin.trim)

          val compiler = new Webpack.Compiler(
            webpackBinaryLocation,
            webpackConfigFileLocation,
            webpackEntries,
            webpackOutputFileName,
            webpackTargetDir,
            assetsWebJarsLocation,
            baseDir,
            targetDir,
            logger,
            nodeModulesLocation
          )

          // Compile all modified sources at once
          val result = compiler.compile()

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

          createdFiles
            .map(f => f.relativeTo(projectRoot).getOrElse(f))
            .sorted
            .foreach { s =>
              logger.info(
                s"[sbt-webpack] Generated ${AnsiColor.MAGENTA}${s.getParent()}/${AnsiColor.BOLD}${s
                  .getName()}${AnsiColor.RESET} in ${endInstant - startInstant} ms"
              )
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

    class Shell {
      def execute(cmd: String, cwd: File, envs: (String, String)*): (Int, Seq[String]) = {
        var output = Vector.empty[String]
        val exitCode = Process(cmd, cwd, envs: _*).!(ProcessLogger(s => output = output.:+(s)))
        (exitCode, output)
      }
    }

    class Compiler(
      binary: File,
      configFile: File,
      entries: Seq[String],
      outputFileName: String,
      outputDirectory: File,
      webjarsDirectory: File,
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

      def compile(): CompilationResult = {
        import sbt._

        val entriesEnvs = entries.zipWithIndex.map {
          case (path, index) =>
            val absolutePath =
              if (path.startsWith("assets:"))
                baseDir.toPath.resolve(path.drop(7)).toAbsolutePath
              else if (path.startsWith("webjar:"))
                webjarsDirectory.toPath.resolve(path.drop(7)).toAbsolutePath
              else path
            s"""--env entry.$index=$absolutePath"""
        }

        val cmd = (Seq(
          binary.getCanonicalPath,
          "--config",
          configFile.getAbsolutePath(),
          s"""--env output.path=${outputDirectory.getAbsolutePath()}""",
          s"""--env output.filename=$outputFileName""",
          s"""--env webjars.path=${webjarsDirectory.getAbsolutePath()}"""
        ) ++ entriesEnvs).mkString(" ")

        logger.info(s"[sbt-webpack] Running command ${AnsiColor.CYAN}$cmd${AnsiColor.RESET}")

        val (exitCode, output) =
          shell.execute(cmd, baseDir, "NODE_PATH" -> nodeModules.getCanonicalPath)

        val success = exitCode == 0

        val regex1 = """^(\[\d+\]|\|)\s(.+?)\s.*""".r

        def parseOutputLine(line: String): String =
          line.trim match {
            case regex1(_, s) => s
            case s            => s
          }

        if (success) {
          val processedFiles: Set[String] =
            output
              .filter(s => s.contains("[built]") && !s.contains("multi") && !s.contains("(webpack)"))
              .map(parseOutputLine)
              .toSet

          logger.info(
            processedFiles.mkString("[sbt-webpack] Processed files:\n[sbt-webpack]\t", "\n[sbt-webpack]\t", "\n")
          )

          CompilationResult(
            success = exitCode == 0,
            entries =
              if (exitCode == 0)
                Seq(
                  CompilationEntry(
                    inputFile = configFile,
                    filesRead = processedFiles.map(path => baseDir.toPath().resolve(path).toFile()),
                    filesWritten = Set(outputDirectory.toPath().resolve(outputFileName).toFile())
                  )
                )
              else Seq.empty
          )
        } else CompilationResult(success = true, entries = Seq.empty)
      }
    }
  }
}
