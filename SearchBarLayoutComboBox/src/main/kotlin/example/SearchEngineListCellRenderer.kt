package example

import java.awt.Component
import javax.swing.* // ktlint-disable no-wildcard-imports

class SearchEngineListCellRenderer<E : SearchEngine> : ListCellRenderer<E> {
  private val renderer = DefaultListCellRenderer()

  override fun getListCellRendererComponent(
    list: JList<out E>,
    value: E?,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    val c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
    if (c is JLabel && value != null) {
      c.icon = value.favicon
      c.toolTipText = value.url
    }
    return c
  }
}
