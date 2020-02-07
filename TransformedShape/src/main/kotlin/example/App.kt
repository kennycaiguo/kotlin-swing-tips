package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.HierarchyEvent
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {
    add(FontRotateAnimation("A"))
    preferredSize = Dimension(320, 240)
  }
}

class FontRotateAnimation(str: String) : JComponent() {
  private var rotate = 0.0
  private var shape: Shape
  private val animator = Timer(10, null)
  override fun paintComponent(g: Graphics) {
    val g2 = g.create() as? Graphics2D ?: return
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.paint = Color.BLACK
    g2.fill(shape)
    g2.dispose()
  }

  init {
    addHierarchyListener { e ->
      if (e.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong() != 0L && !e.component.isDisplayable) {
        animator.stop()
      }
    }
    val font = Font(Font.SERIF, Font.PLAIN, 200)
    val frc = FontRenderContext(null, true, true)
    val outline = TextLayout(str, font, frc).getOutline(null)
    shape = outline
    animator.addActionListener {
      repaint(shape.bounds) // clear prev
      val b = outline.bounds2D
      val at = AffineTransform.getRotateInstance(Math.toRadians(rotate), b.centerX, b.centerY)
      val cx = width / 2.0 - b.centerX
      val cy = height / 2.0 - b.centerY
      val toCenterAtf = AffineTransform.getTranslateInstance(cx, cy)
      val s1 = at.createTransformedShape(outline)
      shape = toCenterAtf.createTransformedShape(s1)
      repaint(shape.bounds)
      // rotate = rotate >= 360 ? 0 : rotate + 2;
      rotate = (rotate + 2.0) % 360.0
    }
    animator.start()
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
