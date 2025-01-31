package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.event.HyperlinkEvent

fun makeUI(): Component {
  val m = DefaultListModel<SiteItem>()
  m.addElement(SiteItem("aterai", listOf("https://ateraimemo.com", "https://github.com/aterai")))
  m.addElement(SiteItem("example", listOf("http://www.example.com", "https://www.example.com")))

  val list = JList(m)
  list.fixedCellHeight = 120
  list.cellRenderer = SiteListItemRenderer()

  val ml = object : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
      val pt = e.point
      val index = list.locationToIndex(pt)
      if (index >= 0) {
        val item = list.model.getElementAt(index)
        val c = list.cellRenderer.getListCellRendererComponent(list, item, index, false, false)
        if (c is JEditorPane) {
          val r = list.getCellBounds(index, index)
          c.setBounds(r)
          val me = SwingUtilities.convertMouseEvent(list, e, c)
          me.translatePoint(pt.x - r.x - me.x, pt.y - r.y - me.y)
          c.dispatchEvent(me)
        }
      }
    }
  }
  list.addMouseListener(ml)

  return JPanel(BorderLayout()).also {
    it.add(JScrollPane(list))
    it.preferredSize = Dimension(320, 240)
  }
}

private data class SiteItem(val name: String, val link: List<String>)

private class SiteListItemRenderer : JEditorPane(), ListCellRenderer<SiteItem> {
  init {
    this.contentType = "text/html"
    this.isEditable = false
    addHyperlinkListener { e ->
      if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        println("You click the link with the URL " + e.url)
        // UIManager.getLookAndFeel().provideErrorFeedback(e.source as? Component)
      }
    }
  }

  override fun getListCellRendererComponent(
    list: JList<out SiteItem>,
    item: SiteItem,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    val buf = StringBuilder(100)
    buf.append("<html><h1>${item.name}</h1><table>")
    for (c in item.link.indices) {
      val url = item.link[c]
      buf.append("<tr><td><a href='$url'>$url</a></td></tr>")
    }
    buf.append("</table></html>")
    this.text = buf.toString()
    background = if (isSelected) Color.LIGHT_GRAY else Color.WHITE
    return this
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
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      contentPane.add(makeUI())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
