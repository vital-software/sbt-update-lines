package co.vitaler.sbt.updateline

import java.io.{ File, FileInputStream }
import java.net.URI
import java.nio.file.{ Files, Paths, StandardCopyOption }

trait TempFile {
  /**
   * Copies the given resource URI to a temporary file
   *
   * @return The temporary File, which will be deleted on exit
   */
  protected def temporaryCopy(uri: URI): File = {
    val parts: Array[String] = Paths.get(uri).getFileName.toString.split("\\.")

    val temp: File = File.createTempFile(parts.head, "." + parts.last)
    temp.deleteOnExit()

    Files.copy(
      new FileInputStream(new File(uri)),
      temp.toPath,
      StandardCopyOption.REPLACE_EXISTING
    )

    temp
  }
}
