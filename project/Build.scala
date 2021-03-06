import sbt._
import Keys._

import org.sbtidea.SbtIdeaPlugin
import com.typesafe.sbt.SbtPgp

object Dependencies {
    val isScala_2_10 = Def.setting {
        scalaVersion.value.startsWith("2.10")
    }

    def scala_2_10(moduleID: ModuleID) =
        Def.setting { if (isScala_2_10.value) Seq(moduleID) else Seq.empty }

    def scala_2_11_+(moduleID: ModuleID) =
        Def.setting { if (!isScala_2_10.value) Seq(moduleID) else Seq.empty }

    val scalaio = {
        val namespace = "com.github.scala-incubator.io"
        val version = "0.4.3"
        Seq(namespace %% "scala-io-core" % version,
            namespace %% "scala-io-file" % version)
    }

    val breeze = "org.scalanlp" %% "breeze" % "0.10"

    val saddle = "org.scala-saddle" %% "saddle-core" % "1.3.3"

    val play_json = "com.typesafe.play" %% "play-json" % "2.4.0-M2"

    val specs2 = "org.specs2" %% "specs2" % "2.3.11" % Test

    val scopt = "com.github.scopt" %% "scopt" % "3.2.0"

    val joda_time = "joda-time" % "joda-time" % "2.6"

    val opencsv = "net.sf.opencsv" % "opencsv" % "2.3"

    val ical4j = "org.mnode.ical4j" % "ical4j" % "1.0.2"

    val reflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

    val paradise = "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full

    val quasiquotes = scala_2_10("org.scalamacros" %% "quasiquotes" % "2.0.0")

    val xml = scala_2_11_+("org.scala-lang.modules" %% "scala-xml" % "1.0.2")
}

object BokehBuild extends Build {
    override lazy val settings = super.settings ++ Seq(
        organization := "io.continuum.bokeh",
        version := "0.6-SNAPSHOT",
        description := "Scala bindings for Bokeh plotting library",
        homepage := Some(url("http://bokeh.pydata.org")),
        licenses := Seq("MIT-style" -> url("http://www.opensource.org/licenses/mit-license.php")),
        scalaVersion := "2.11.6",
        crossScalaVersions := Seq("2.10.5", "2.11.6"),
        scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature", "-Xlog-free-types"),
        scalacOptions += "-language:postfixOps,implicitConversions,higherKinds,experimental.macros",
        scalacOptions in (Compile, doc) := Seq("-groups", "-implicits"),
        addCompilerPlugin(Dependencies.paradise),
        shellPrompt := { state =>
            "continuum (%s)> ".format(Project.extract(state).currentProject.id)
        },
        cancelable := true
    )

    val runAll = inputKey[Unit]("Run all discovered main classes.")
    val upload = taskKey[Unit]("Upload scaladoc to S3.")

    lazy val publishSettings = Seq(
        publishTo := {
            val nexus = "https://oss.sonatype.org/"
            if (isSnapshot.value)
                Some("snapshots" at nexus + "content/repositories/snapshots")
            else
                Some("releases" at nexus + "service/local/staging/deploy/maven2")
        },
        publishMavenStyle := true,
        publishArtifact in Test := false,
        pomIncludeRepository := { _ => false },
        pomExtra := (
            <scm>
                <url>https://github.com/bokeh/bokeh-scala</url>
                <connection>scm:git:https://github.com/bokeh/bokeh-scala.git</connection>
            </scm>
            <developers>
                <developer>
                    <id>mattpap</id>
                    <name>Mateusz Paprocki</name>
                    <url>mateuszpaprocki.pl</url>
                </developer>
            </developers>
        ),
        credentials ++= {
            (for {
                username <- sys.env.get("SONATYPE_USERNAME")
                password <- sys.env.get("SONATYPE_PASSWORD")
            } yield {
                Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)
            }) orElse {
                val path = Path.userHome / ".sonatype" / "credentials"
                if (path.exists) Some(Credentials(path)) else None
            } toList
        }
    )

    lazy val commonSettings = Defaults.coreDefaultSettings ++ publishSettings ++ Seq(
        parallelExecution in Test := false,
        fork := true,
        runAll := {
            val args = Def.spaceDelimited("<args>").parsed
            val results = (discoveredMainClasses in Compile).value.sorted.flatMap { mainClass =>
                val classpath = Attributed.data((fullClasspath in Compile).value)
                val logger = streams.value.log

                val result = (runner in run).value.run(mainClass, classpath, args, logger)

                result match {
                    case Some(msg) => logger.error(s"$mainClass: $msg"); Some(mainClass)
                    case None      => logger.success(mainClass);         None
                }
            }

            if (results.nonEmpty) {
                val failures = results.mkString(", ")
                sys.error(s"failed to run: $failures")
            }
        }
    )

    lazy val ideaSettings = SbtIdeaPlugin.settings

    lazy val bokehPlugins = ideaSettings

    lazy val bokehSettings = commonSettings ++ bokehPlugins ++ Seq(
        libraryDependencies ++= {
            import Dependencies._
            scalaio ++ xml.value ++ Seq(breeze, joda_time, play_json, specs2)
        },
        upload := {
            val local = target in (Compile, doc) value
            val remote = s"s3://bokeh-scala/docs/${scalaBinaryVersion.value}/${version.value}"
            s"aws s3 sync $local $remote --delete --acl public-read" !
        },
        initialCommands in Compile := """
            import scala.reflect.runtime.{universe=>u,currentMirror=>cm}
            import scalax.io.JavaConverters._
            import scalax.file.Path
            import play.api.libs.json.Json
            import io.continuum.bokeh._
            """
    )

    lazy val bokehjsSettings = commonSettings ++ BokehJS.bokehjsSettings

    lazy val coreSettings = commonSettings ++ Seq(
        libraryDependencies ++= {
            import Dependencies._
            quasiquotes.value ++ Seq(reflect.value, play_json, specs2)
        }
    )

    lazy val sampledataSettings = commonSettings ++ Seq(
        libraryDependencies ++= {
            import Dependencies._
            scalaio ++ xml.value ++ Seq(opencsv, ical4j, saddle, specs2)
        }
    )

    lazy val examplesSettings = commonSettings ++ Seq(
        libraryDependencies ++= {
            import Dependencies._
            Seq(breeze, scopt, specs2)
        },
        javaOptions ++= {
            val netlib = "com.github.fommil.netlib"
            val linalg = Seq("BLAS", "LAPACK", "ARPACK")
            linalg.map(name => s"-D$netlib.$name=$netlib.F2j$name")
        }
    )

    lazy val allSettings = Seq(
        publishLocal := {},
        publish := {}
    )

    lazy val bokeh = project in file("bokeh") settings(bokehSettings: _*) dependsOn(core, bokehjs)
    lazy val bokehjs = project in file("bokehjs/bokehjs") settings(bokehjsSettings: _*)
    lazy val core = project in file("core") settings(coreSettings: _*)
    lazy val sampledata = project in file("sampledata") settings(sampledataSettings: _*) dependsOn(core)
    lazy val examples = project in file("examples") settings(examplesSettings: _*) dependsOn(bokeh, sampledata)
    lazy val all = project in file(".") disablePlugins(SbtPgp) settings(allSettings: _*) aggregate(bokeh, bokehjs, core, sampledata, examples)

    override def projects = Seq(bokeh, bokehjs, core, sampledata, examples, all)
}
