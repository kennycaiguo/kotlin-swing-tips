package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val p = JPanel(BorderLayout(10, 10))
  val locale = p.locale
  val swatches = JCheckBox(UIManager.getString("ColorChooser.swatchesNameText", locale))
  val hsv = JCheckBox(UIManager.getString("ColorChooser.hsvNameText", locale))
  val hsl = JCheckBox(UIManager.getString("ColorChooser.hslNameText", locale))
  val rgb = JCheckBox(UIManager.getString("ColorChooser.rgbNameText", locale))
  val cmyk = JCheckBox(UIManager.getString("ColorChooser.cmykNameText", locale))
  val list = listOf(swatches, hsv, hsl, rgb, cmyk)

  val button = JButton("open JColorChooser")
  button.addActionListener {
    val selected = list.filter { it.isSelected }.map { it.text }
    val color = if (selected.isEmpty()) { // use default JColorChooser
      JColorChooser.showDialog(p.rootPane, "JColorChooser", null)
    } else {
      showColorChooserDialog(p.rootPane, "JColorChooser", selected)
    }
    println(color)
  }

  val box = Box.createVerticalBox()
  for (b in list) {
    box.add(b)
    box.add(Box.createVerticalStrut(5))
  }

  p.add(box, BorderLayout.NORTH)
  p.add(button, BorderLayout.SOUTH)
  p.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  p.preferredSize = Dimension(320, 240)
  return p
}

fun showColorChooserDialog(p: Component, title: String, selected: List<String>): Color? {
  val cc = JColorChooser()
  for (pnl in cc.chooserPanels) {
    if (!selected.contains(pnl.displayName)) {
      cc.removeChooserPanel(pnl)
    }
  }
  val dialog = JColorChooser.createDialog(p, title, true, cc, null, null)
  dialog.addComponentListener(object : ComponentAdapter() {
    override fun componentHidden(e: ComponentEvent) {
      (e.component as? Window)?.dispose()
    }
  })
  dialog.isVisible = true // blocks until user brings dialog down...
  return cc.color
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
