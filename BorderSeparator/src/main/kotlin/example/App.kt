package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val model = DefaultComboBoxModel<ComboItem>()
  model.addElement(ComboItem("1111"))
  model.addElement(ComboItem("1111222"))
  model.addElement(ComboItem("111122233"))
  model.addElement(ComboItem("444444", true))
  model.addElement(ComboItem("555"))
  model.addElement(ComboItem("6666666"))

  val combo1 = makeComboBox(model)
  val combo2 = makeComboBox(model)
  combo2.isEditable = true

  val box1 = Box.createVerticalBox()
  box1.border = BorderFactory.createTitledBorder("setEditable(false)")
  box1.add(combo1)

  val box2 = Box.createVerticalBox()
  box2.border = BorderFactory.createTitledBorder("setEditable(true)")
  box2.add(combo2)

  return JPanel(BorderLayout()).also {
    it.border = BorderFactory.createEmptyBorder(50, 5, 50, 5)
    it.add(box1, BorderLayout.NORTH)
    it.add(box2, BorderLayout.SOUTH)
    it.preferredSize = Dimension(320, 240)
  }
}

private fun makeComboBox(model: ComboBoxModel<ComboItem>) = object : JComboBox<ComboItem>(model) {
  override fun updateUI() {
    setRenderer(null)
    super.updateUI()
    val renderer = getRenderer()
    setRenderer { list, value, index, isSelected, cellHasFocus ->
      val c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      if (c is JComponent && value != null) {
        if (index != -1 && value.hasSeparator) {
          c.border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY)
        } else {
          c.border = BorderFactory.createEmptyBorder()
        }
      }
      c
    }
  }
}

data class ComboItem(val item: String, val hasSeparator: Boolean = false) {
  override fun toString() = item
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
