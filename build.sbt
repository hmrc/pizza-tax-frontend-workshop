import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexes matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimum := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val compileDeps = Seq(
  "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % "5.12.0",
  "uk.gov.hmrc"                  %% "auth-client"                % "5.7.0-play-28",
  "uk.gov.hmrc"                  %% "play-fsm"                   % "0.86.0-play-28",
  "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % "0.53.0",
  "uk.gov.hmrc"                  %% "json-encryption"            % "4.10.0-play-28",
  "uk.gov.hmrc"                  %% "play-frontend-govuk"        % "1.0.0-play-28",
  "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "1.2.0-play-28",
  "com.googlecode.libphonenumber" % "libphonenumber"             % "8.12.31",
  "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.12.5"
)

def testDeps(scope: String) =
  Seq(
    "org.scalatest"       %% "scalatest"    % "3.2.8"  % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
  )

lazy val itDeps = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"  % IntegrationTest,
  "com.github.tomakehurst"  % "wiremock-jre8"      % "2.27.2" % IntegrationTest
)

lazy val root = (project in file("."))
  .settings(
    name := "pizza-tax-frontend",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 12345,
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.{components => hmrcComponents}"
    ),
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it") ++ itDeps,
    publishingSettings,
    scoverageSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    majorVersion := 0,
    javaOptions in Compile += "-Djava.locale.providers=CLDR,JRE"
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    scalafmtOnCompile in IntegrationTest := true
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .enablePlugins(PlayScala, SbtDistributablesPlugin)

inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
