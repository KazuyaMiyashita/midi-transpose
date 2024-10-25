import java.io.File
import java.nio.file.Files
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence
import scala.jdk.CollectionConverters.*

object Main {

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("usage: [filename] [transpose (-6 ~ 6)]")
      throw new RuntimeException
    }
    val filename = args(0)
    val transpose = args(1).toInt

    val file = new File(filename)

    val sequence = MidiSystem.getSequence(file)

    modifySequence(sequence, transpose)

    val (baseName, extension) = {
      val s = file.getName.split("\\.")
      (s.init.mkString("."), s.last)
    }
    val outFileName = s"$baseName${if (transpose >= 0) "+" else ""}$transpose.$extension"
    val outFile = new File(file.getParent, outFileName)
    println(outFile)
    Files.createFile(outFile.toPath)
    MidiSystem.write(sequence, 0, outFile)

    println("succeed.")

  }

  def modifySequence(sequence: Sequence, transpose: Int): Unit = {
    sequence.getTracks().foreach { track =>
      for (i <- 0 until track.size) {
        val midiEvent = track.get(i)
        val message = midiEvent.getMessage
        message match {
          case m: javax.sound.midi.ShortMessage =>
            // println(f"short: ${m.getChannel}, ${m.getCommand}%2X, ${m.getData1}%2X, ${m.getData2}%2X")
            val channel = m.getChannel
            val command = m.getCommand
            if (channel != 9 && (command == 0x80 || command == 0x90)) {
              val noteNumber = m.getData1
              m.setMessage(command, channel, noteNumber + transpose, m.getData2)
            }
          case _ => ()
        }
      }
    }
  }

}
