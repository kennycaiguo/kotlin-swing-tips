package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {
    val label = JLabel(ImageIcon(javaClass.getResource("CRW_3857_JFR.jpg"))) // http://sozai-free.com/
    val vport = object : JViewport() {
      private val WEIGHT_MIXING = false
      private var isAjusting: Boolean = false
      override fun revalidate() {
        if (!WEIGHT_MIXING && isAjusting) {
          return
        }
        super.revalidate()
      }

      override fun setViewPosition(p: Point) {
        if (WEIGHT_MIXING) {
          super.setViewPosition(p)
        } else {
          isAjusting = true
          super.setViewPosition(p)
          isAjusting = false
        }
      }
    }
    vport.add(label)

    val scroll = JScrollPane() // new JScrollPane(label);
    scroll.setViewport(vport)

    val hsl1 = HandScrollListener()
    vport.addMouseMotionListener(hsl1)
    vport.addMouseListener(hsl1)

    val radio = JRadioButton("scrollRectToVisible", true)
    radio.addItemListener { e ->
      hsl1.withinRangeMode = e.getStateChange() == ItemEvent.SELECTED
    }

    val box = Box.createHorizontalBox()
    val bg = ButtonGroup()
    listOf(radio, JRadioButton("setViewPosition")).forEach {
      box.add(it)
      bg.add(it)
    }

    // // TEST:
    // MouseAdapter hsl2 = new DragScrollListener()
    // label.addMouseMotionListener(hsl2)
    // label.addMouseListener(hsl2)
    add(scroll)
    add(box, BorderLayout.NORTH)
    scroll.setPreferredSize(Dimension(320, 240))
  }
}

internal class HandScrollListener : MouseAdapter() {
  private val defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
  private val hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
  private val pp = Point()
  var withinRangeMode = true

  override fun mouseDragged(e: MouseEvent) {
    val vport = e.getComponent() as? JViewport ?: return
    val cp = e.getPoint()
    val vp = vport.getViewPosition() // = SwingUtilities.convertPoint(vport, 0, 0, label)
    vp.translate(pp.x - cp.x, pp.y - cp.y)
    if (withinRangeMode) {
      (SwingUtilities.getUnwrappedView(vport) as? JComponent)
          ?.scrollRectToVisible(Rectangle(vp, vport.getSize()))
    } else {
      vport.setViewPosition(vp)
    }
    pp.setLocation(cp)
  }

  override fun mousePressed(e: MouseEvent) {
    e.getComponent().setCursor(hndCursor)
    pp.setLocation(e.getPoint())
  }

  override fun mouseReleased(e: MouseEvent) {
    e.getComponent().setCursor(defCursor)
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
