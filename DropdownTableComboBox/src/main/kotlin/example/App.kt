package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.basic.BasicComboPopup
import javax.swing.plaf.metal.MetalComboBoxUI
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

class MainPanel : JPanel(BorderLayout()) {
  init {
    val aSeries = listOf(
      listOf("A1", 594, 841),
      listOf("A2", 420, 594),
      listOf("A3", 297, 420),
      listOf("A4", 210, 297),
      listOf("A5", 148, 210),
      listOf("A6", 105, 148)
    )

    val columns = arrayOf("A series", "width", "height")

    val wtf = JTextField(5)
    wtf.setEditable(false)

    val htf = JTextField(5)
    htf.setEditable(false)

    val model = object : DefaultTableModel(null, columns) {
      override fun getColumnClass(column: Int) =
        if (column == 1 || column == 2) Integer::class.java else String::class.java

      override fun isCellEditable(row: Int, column: Int) = false
    }

    val combo = DropdownTableComboBox(aSeries, model)
    combo.addItemListener { e ->
      if (e.getStateChange() == ItemEvent.SELECTED) {
        val rowData = combo.getSelectedRow()
        wtf.setText(rowData[1].toString())
        htf.setText(rowData[2].toString())
      }
    }
    val renderer = combo.getRenderer()
    combo.setRenderer { list, value, index, isSelected, cellHasFocus ->
      val c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      if (isSelected) {
        c.setBackground(list.getSelectionBackground())
        c.setForeground(list.getSelectionForeground())
      } else {
        c.setBackground(list.getBackground())
        c.setForeground(list.getForeground())
      }
      (c as? JLabel)?.also {
        it.setOpaque(true)
        it.setText(value?.get(0)?.toString() ?: "")
      }
      c
    }

    EventQueue.invokeLater { combo.setSelectedIndex(3) }

    val box = Box.createHorizontalBox().also {
      it.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
      it.add(combo)
      it.add(Box.createHorizontalStrut(15))
      it.add(JLabel("width: "))
      it.add(wtf)
      it.add(Box.createHorizontalStrut(5))
      it.add(JLabel("height: "))
      it.add(htf)
      it.add(Box.createHorizontalGlue())
    }

    add(box, BorderLayout.NORTH)
    setPreferredSize(Dimension(320, 240))
  }
}

class DropdownTableComboBox<E : List<Any>>(private val list: List<E>, model: DefaultTableModel) : JComboBox<E>() {
  @Transient
  private val highlighter = HighlightListener()
  private val table = object : JTable() {
    override fun prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component {
      val c = super.prepareRenderer(renderer, row, column)
      c.setForeground(Color.BLACK)
      c.setBackground(when {
        highlighter.isHighlightedRow(row) -> Color(0xFF_C8_C8)
        isRowSelected(row) -> Color.CYAN
        else -> Color.WHITE
      })
      return c
    }

    override fun updateUI() {
      removeMouseListener(highlighter)
      removeMouseMotionListener(highlighter)
      super.updateUI()
      addMouseListener(highlighter)
      addMouseMotionListener(highlighter)
      getTableHeader().setReorderingAllowed(false)
    }
  }

  fun getSelectedRow() = list[getSelectedIndex()]

  init {
    table.setModel(model)
    list.forEach { this.addItem(it) }
    list.forEach { v -> model.addRow(v.toTypedArray()) }
  }

  override fun updateUI() {
    super.updateUI()
    EventQueue.invokeLater {
      setUI(object : MetalComboBoxUI() {
        override fun createPopup() = ComboTablePopup(comboBox, table)
      })
      setEditable(false)
    }
  }
}

class ComboTablePopup(combo: JComboBox<*>, private val table: JTable) : BasicComboPopup(combo) {
  private val scroll: JScrollPane

  init {
    val sm = table.getSelectionModel()
    sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    sm.addListSelectionListener { combo.setSelectedIndex(table.getSelectedRow()) }

    combo.addItemListener { e ->
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setRowSelection(combo.getSelectedIndex())
      }
    }

    table.addMouseListener(object : MouseAdapter() {
      override fun mousePressed(e: MouseEvent) {
        combo.setSelectedIndex(table.rowAtPoint(e.getPoint()))
        setVisible(false)
      }
    })

    scroll = JScrollPane(table)
    setBorder(BorderFactory.createEmptyBorder())
  }

  override fun show() {
    if (isEnabled()) {
      val ins = scroll.getInsets()
      val tableHt = table.getPreferredSize().height
      val headerHt = table.getTableHeader().getPreferredSize().height
      scroll.setPreferredSize(Dimension(240, tableHt + headerHt + ins.top + ins.bottom))
      super.removeAll()
      super.add(scroll)
      setRowSelection(comboBox.getSelectedIndex())
      super.show(comboBox, 0, comboBox.getBounds().height)
    }
  }

  private fun setRowSelection(index: Int) {
    if (index != -1) {
      table.setRowSelectionInterval(index, index)
      table.scrollRectToVisible(table.getCellRect(index, 0, true))
    }
  }
}

class HighlightListener : MouseAdapter() {
  private var viewRowIndex = -1

  fun isHighlightedRow(row: Int) = this.viewRowIndex == row

  private fun setHighlightedTableCell(e: MouseEvent) {
    (e.getComponent() as? JTable)?.also {
      viewRowIndex = it.rowAtPoint(e.getPoint())
      it.repaint()
    }
  }

  override fun mouseMoved(e: MouseEvent) {
    setHighlightedTableCell(e)
  }

  override fun mouseDragged(e: MouseEvent) {
    setHighlightedTableCell(e)
  }

  override fun mouseExited(e: MouseEvent) {
    viewRowIndex = -1
    e.getComponent().repaint()
  }
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
      getContentPane().add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
