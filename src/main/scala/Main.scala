import java.io.File
import java.nio.file.Files
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence
import java.awt._
import java.awt.event._
import java.awt.datatransfer.DataFlavor
import javax.swing._
import javax.swing.TransferHandler.TransferSupport
import scala.jdk.CollectionConverters._

object Main {

  def transposeMidi(file: File, transposeValue: Int): File = {
    val sequence = MidiSystem.getSequence(file)

    modifySequence(sequence, transposeValue)

    val (baseName, extension) = {
      val s = file.getName.split("\\.")
      (s.init.mkString("."), s.last)
    }
    val outFileName = s"$baseName${if (transposeValue >= 0) "+" else ""}$transposeValue.$extension"
    val outFile = new File(file.getParent, outFileName)
    println(outFile)
    Files.createFile(outFile.toPath)
    MidiSystem.write(sequence, 0, outFile)

    println("succeed.")

    outFile
  }

  def modifySequence(sequence: Sequence, transpose: Int): Unit = {
    sequence.getTracks().foreach { track =>
      for (i <- 0 until track.size) {
        val midiEvent = track.get(i)
        val message = midiEvent.getMessage
        message match {
          case m: javax.sound.midi.ShortMessage =>
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

  def main(args: Array[String]): Unit = {
    // ファイルの状態を保持する変数
    var selectedFile: Option[File] = None

    // メインフレーム
    val frame = new JFrame("MIDI Transposer")
    frame.setSize(400, 200)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setLayout(new BorderLayout())

    // ファイル選択ボタンとファイル表示ラベル
    val fileChooserButton = new JButton("Select MIDI File")
    val fileLabel = new JLabel("No file selected")

    // ドロップエリアを作成
    val dropPanel = new JPanel {
      setPreferredSize(new Dimension(400, 100))
      setBorder(BorderFactory.createTitledBorder("Drag and drop your MIDI file here"))

      // ドラッグアンドドロップの設定
      setTransferHandler(new TransferHandler {
        override def canImport(support: TransferSupport): Boolean = {
          support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        }

        override def importData(support: TransferSupport): Boolean = {
          if (canImport(support)) {
            val files = support.getTransferable
              .getTransferData(DataFlavor.javaFileListFlavor)
              .asInstanceOf[java.util.List[File]]
              .asScala
            files.headOption.foreach { file =>
              selectedFile = Some(file)
              fileLabel.setText(s"Selected: ${file.getName}")
            }
            true
          } else {
            false
          }
        }
      })
    }

    // -6 から 6 までの移調ボタンパネル（0は除外）
    val buttonPanel = new JPanel()
    buttonPanel.setLayout(new GridLayout(2, 6, 5, 5))
    for (transposeValue <- (-6 to 6).filter(_ != 0)) {
      val button = new JButton(transposeValue.toString)
      button.addActionListener((_: ActionEvent) => {
        selectedFile match {
          case Some(file) =>
            val newFile = transposeMidi(file, transposeValue)
            JOptionPane.showMessageDialog(
              frame,
              s"Saved transposed file: ${newFile.getName}",
              "File Saved",
              JOptionPane.INFORMATION_MESSAGE
            )
          case None =>
            JOptionPane.showMessageDialog(
              frame,
              "No file selected. Please select a MIDI file.",
              "Error",
              JOptionPane.ERROR_MESSAGE
            )
        }
      })
      buttonPanel.add(button)
    }

    // ファイル選択ボタンのアクション
    fileChooserButton.addActionListener((_: ActionEvent) => {
      val chooser = new JFileChooser()
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
      chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MIDI Files", "MID"))

      if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        selectedFile = Some(chooser.getSelectedFile)
        fileLabel.setText(s"Selected: ${chooser.getSelectedFile.getName}")
      }
    })

    // コンポーネントをフレームに追加
    val topPanel = new JPanel(new BorderLayout())
    topPanel.add(fileChooserButton, BorderLayout.WEST)
    topPanel.add(fileLabel, BorderLayout.CENTER)

    frame.add(topPanel, BorderLayout.NORTH)
    frame.add(dropPanel, BorderLayout.CENTER) // ドロップパネルを追加
    frame.add(buttonPanel, BorderLayout.SOUTH)

    // アプリの初期ファイル設定
    if (args.nonEmpty) {
      selectedFile = Some(new File(args(0)))
      fileLabel.setText(s"Selected: ${selectedFile.get.getName}")
    }

    frame.setVisible(true)
  }
}
