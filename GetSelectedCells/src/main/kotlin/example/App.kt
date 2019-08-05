package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.EventObject
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor

class MainPanel : JPanel(GridBagLayout()) {
  init {
    val columnNames = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
    val data = arrayOf(
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true),
        arrayOf(true, false, true, false, true, true, false, true, true))

    val model = object : DefaultTableModel(data, columnNames) {
      override fun getColumnClass(column: Int) = java.lang.Boolean::class.java
    }

    val table = object : JTable(model) {
      override fun getPreferredScrollableViewportSize() = super.getPreferredSize()

      override fun updateUI() {
        setDefaultEditor(java.lang.Boolean::class.java, null)
        super.updateUI()
        setDefaultEditor(java.lang.Boolean::class.java, BooleanEditor())
      }
    }

    table.setCellSelectionEnabled(true)
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
    table.setAutoCreateRowSorter(true)
    table.setComponentPopupMenu(TablePopupMenu())

    val m = table.getColumnModel()
    (0 until m.getColumnCount()).forEach {
      val col = m.getColumn(it)
      col.setPreferredWidth(CELLSIZE)
      col.setResizable(false)
    }

    val scroll = JScrollPane(table)
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

    add(scroll)
    setPreferredSize(Dimension(320, 240))
  }

  companion object {
    private const val CELLSIZE = 24
  }
}

internal class TablePopupMenu : JPopupMenu() {
  private val select: JMenuItem
  private val clear: JMenuItem
  private val toggle: JMenuItem

  init {
    select = add("select")
    select.addActionListener {
      val table = getInvoker() as? JTable ?: return@addActionListener
      for (row in table.getSelectedRows()) {
        for (col in table.getSelectedColumns()) {
          table.setValueAt(true, row, col)
        }
      }
    }

    clear = add("clear")
    clear.addActionListener {
      val table = getInvoker() as? JTable ?: return@addActionListener
      for (row in table.getSelectedRows()) {
        for (col in table.getSelectedColumns()) {
          table.setValueAt(false, row, col)
        }
      }
    }

    toggle = add("toggle")
    toggle.addActionListener {
      val table = getInvoker() as? JTable ?: return@addActionListener
      for (row in table.getSelectedRows()) {
        for (col in table.getSelectedColumns()) {
          var b = table.getValueAt(row, col) as? Boolean ?: continue
          table.setValueAt(!b, row, col)
        }
      }
    }
  }

  override fun show(c: Component, x: Int, y: Int) {
    if (c is JTable) {
      val isSelected = c.getSelectedRowCount() > 0
      select.setEnabled(isSelected)
      clear.setEnabled(isSelected)
      toggle.setEnabled(isSelected)
      super.show(c, x, y)
    }
  }
}

internal class BooleanEditor : AbstractCellEditor(), TableCellEditor {
  protected val renderer: Container = object : JPanel(GridBagLayout()) {
    protected var listener: MouseListener? = null

    override fun updateUI() {
      removeMouseListener(listener)
      super.updateUI()
      listener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
          fireEditingStopped()
        }
      }
      addMouseListener(listener)
    }
  }
  protected val checkBox: JCheckBox = object : JCheckBox() {
    var handler: CheckBoxHandler? = null

    override fun updateUI() {
      removeActionListener(handler)
      removeMouseListener(handler)
      super.updateUI()
      setBorder(UIManager.getBorder("Table.noFocusBorder"))
      setOpaque(false)
      setFocusable(false)
      setRolloverEnabled(false)
      handler = CheckBoxHandler()
      addActionListener(handler)
      addMouseListener(handler)
    }
  }

  override fun getTableCellEditorComponent(
    table: JTable,
    value: Any?,
    isSelected: Boolean,
    row: Int,
    column: Int
  ): Component {
    checkBox.setSelected(value == true)
    renderer.add(checkBox)
    return renderer
  }

  override fun getCellEditorValue() = checkBox.isSelected()

  override fun isCellEditable(e: EventObject) = (e as? MouseEvent)
      ?.takeUnless { e.isShiftDown() || e.isControlDown() }?.let { true } ?: super.isCellEditable(e)

  private inner class CheckBoxHandler : MouseAdapter(), ActionListener {
    override fun actionPerformed(e: ActionEvent) {
      fireEditingStopped()
    }

    override fun mousePressed(e: MouseEvent) {
      val t = SwingUtilities.getAncestorOfClass(JTable::class.java, e.getComponent()) as? JTable ?: return
      if (checkBox.getModel().isPressed() && t.isRowSelected(t.getEditingRow()) && e.isControlDown()) {
        renderer.setBackground(t.getBackground())
      } else {
        renderer.setBackground(t.getSelectionBackground())
      }
    }

    override fun mouseExited(e: MouseEvent) {
      // SwingUtilities.getAncestorOfClass(JTable::class.java, e.getComponent())
      //     ?.takeIf { it is JTable }?.let { it as JTable }
      //     ?.takeIf { it.isEditing() }?.also { it.removeEditor() }
      SwingUtilities.getAncestorOfClass(JTable::class.java, e.getComponent())
          ?.takeIf { it is JTable && it.isEditing() }
          ?.also { (it as JTable).removeEditor() }
      // (SwingUtilities.getAncestorOfClass(JTable::class.java, e.getComponent()) as? JTable)
      //     ?.takeIf { it.isEditing() }
      //     ?.also { it.removeEditor() }
      // val t = SwingUtilities.getAncestorOfClass(JTable::class.java, e.getComponent()) as? JTable ?: return
      // if (t.isEditing()) {
      //   t.removeEditor()
      // }
    }
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
