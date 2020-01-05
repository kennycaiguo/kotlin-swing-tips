package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val log = JTextArea()
  val p = JPanel(BorderLayout())
  p.add(JScrollPane(log))
  p.preferredSize = Dimension(320, 240)

  val tk = Toolkit.getDefaultToolkit()
  val fmt = "  Frame.MAXIMIZED_%s: %s%n"
  log.append("Toolkit#isFrameStateSupported(int)\n")
  log.append(String.format(fmt, "HORIZ", tk.isFrameStateSupported(Frame.MAXIMIZED_HORIZ)))
  log.append(String.format(fmt, "VERT", tk.isFrameStateSupported(Frame.MAXIMIZED_VERT)))
  log.append(String.format(fmt, "BOTH", tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH)))

  EventQueue.invokeLater {
    val c = p.topLevelAncestor
    if (c is JFrame) {
      c.addWindowStateListener { e ->
        val ws = when (e.newState) {
          Frame.NORMAL -> "NORMAL"
          Frame.ICONIFIED -> "ICONIFIED"
          Frame.MAXIMIZED_HORIZ -> "MAXIMIZED_HORIZ"
          Frame.MAXIMIZED_VERT -> "MAXIMIZED_VERT"
          Frame.MAXIMIZED_BOTH -> "MAXIMIZED_BOTH"
          else -> "ERROR"
        }
        log.append(String.format("WindowStateListener: %s%n", ws))
      }

      c.addWindowListener(object : WindowListener {
        override fun windowOpened(e: WindowEvent) {
          log.append("windowOpened\n")
        }

        override fun windowClosing(e: WindowEvent) {
          log.append("windowClosing\n")
        }

        override fun windowClosed(e: WindowEvent) {
          log.append("windowClosed\n")
        }

        override fun windowIconified(e: WindowEvent) {
          log.append("windowIconified\n")
        }

        override fun windowDeiconified(e: WindowEvent) {
          log.append("windowDeiconified\n")
        }

        override fun windowActivated(e: WindowEvent) {
          log.append("windowActivated\n")
        }

        override fun windowDeactivated(e: WindowEvent) {
          log.append("windowDeactivated\n")
        }
      })
    }
  }
  return p
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
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(makeUI())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
