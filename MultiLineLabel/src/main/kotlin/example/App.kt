package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.JTextComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

private const val DUMMY_TEXT = "Quartz glyph job vex'd cwm finks."

fun makeUI(): Component {
  val label1 = JTextPane()
  val attr = label1.getStyle(StyleContext.DEFAULT_STYLE)
  StyleConstants.setLineSpacing(attr, -.2f)
  label1.setParagraphAttributes(attr, true)
  label1.text = "JTextPane\n$DUMMY_TEXT"

  val label2 = JTextArea("JTextArea\n$DUMMY_TEXT")

  val cl = Thread.currentThread().contextClassLoader
  val icon = ImageIcon(cl.getResource("example/wi0124-32.png"))
  val label3 = JLabel("<html>JLabel+html<br>$DUMMY_TEXT")
  label3.icon = icon

  return JPanel(GridLayout(3, 1)).also {
    it.add(makeLeftIcon(label1, icon))
    it.add(makeLeftIcon(label2, icon))
    it.add(label3)
    it.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
    it.preferredSize = Dimension(320, 240)
  }
}

private fun makeLeftIcon(label: JTextComponent, icon: ImageIcon): Box {
  label.foreground = UIManager.getColor("Label.foreground")
  label.isOpaque = false
  label.isEditable = false
  label.isFocusable = false
  label.maximumSize = label.preferredSize
  label.minimumSize = label.preferredSize

  val l = JLabel(icon)
  l.cursor = Cursor.getDefaultCursor()

  val box = Box.createHorizontalBox()
  box.add(l)
  box.add(Box.createHorizontalStrut(2))
  box.add(label)
  box.add(Box.createHorizontalGlue())
  return box
}

fun main() {
  EventQueue.invokeLater {
    runCatching {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }.onFailure {
      it.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
    }
    JFrame().apply {
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      contentPane.add(makeUI())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
