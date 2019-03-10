package co.vitaler.sbt.updateline

import java.io.{ File, FileInputStream }
import java.nio.file.{ Files, Paths, StandardCopyOption }

import co.vitaler.sbt.updateline.UpdateLinePlugin.autoImport.UpdateLine
import org.specs2.control.Executable.StringProcessLogger
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import sbt._
import sbtrelease.Vcs

import scala.io.Source

class UpdateTest extends Specification with Mockito with TempFile {
  private val example = getClass.getResource("/example.md").toURI

  "Update" should {
    "match lines and update them" in {
      "multiple matches" in {
        val logger = new StringProcessLogger
        val file: File = temporaryCopy(example)
        Update.apply(UpdateLine(file, _.contains("// Latest release"), v => s"replaced: $v", updateVcs = false), "1.0.0", logger, None)
        Source.fromFile(file).getLines().count(_.matches("replaced: 1\\.0\\.0")) must beEqualTo(2)
      }

      "single match" in {
        val logger = new StringProcessLogger
        val file: File = temporaryCopy(example)
        Update.apply(UpdateLine(file, _.matches(".*// Latest release - 1"), v => s"replaced: $v", updateVcs = false), "1.0.0", logger, None)
        Source.fromFile(file).getLines().count(_.matches("replaced: 1\\.0\\.0")) must beEqualTo(1)
      }
    }

    "work with VCS" in {
      "fail if no VCS is available when needed" in {
        val logger = new StringProcessLogger
        val file: File = temporaryCopy(example)
        Update.apply(UpdateLine(file, _ => true, _ => "blah"), "1.0.0", logger, None) must throwA[RuntimeException]
      }

      "add the modified files to the VCS" in {
        val logger = new StringProcessLogger
        val file: File = temporaryCopy(example)

        val vcs = mock[Vcs]
        vcs.add(any[String]) returns new ProcessBuilder("true")

        Update.apply(UpdateLine(file, _ => true, _ => "blah"), "1.0.0", logger, Some(vcs))
        there was one(vcs).add(any[String])
      }
    }
  }
}
