package example

import com.sun.java.swing.plaf.windows.WindowsSliderUI
import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.metal.MetalSliderUI
import kotlin.math.roundToInt

fun makeUI(): Component {
  val slider = makeSlider("Custom SnapToTicks")
  initSlider(slider)

  val list = listOf(makeSlider("Default SnapToTicks"), slider)

  val check = JCheckBox("JSlider.setMinorTickSpacing(5)")
  check.addActionListener { e ->
    val mts = if ((e.source as? JCheckBox)?.isSelected == true) 5 else 0
    list.forEach { it.minorTickSpacing = mts }
  }

  val box = Box.createVerticalBox()
  box.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  for (s in list) {
    box.add(s)
    box.add(Box.createVerticalStrut(10))
  }
  box.add(check)
  box.add(Box.createVerticalGlue())

  return JPanel(BorderLayout()).also {
    it.add(box)
    it.preferredSize = Dimension(320, 240)
  }
}

private fun makeSlider(title: String) = JSlider(0, 100, 50).also {
  it.border = BorderFactory.createTitledBorder(title)
  it.majorTickSpacing = 10
  it.snapToTicks = true
  it.paintTicks = true
  it.paintLabels = true
}

private fun initSlider(slider: JSlider) {
  slider.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "RIGHT_ARROW")
  slider.actionMap.put("RIGHT_ARROW", object : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
      val s = e.source as? JSlider ?: return
      s.value = s.value + s.majorTickSpacing
    }
  })
  slider.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "LEFT_ARROW")
  slider.actionMap.put("LEFT_ARROW", object : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
      val s = e.source as? JSlider ?: return
      s.value = s.value - s.majorTickSpacing
    }
  })
  slider.addMouseWheelListener { e ->
    val s = e.component as? JSlider ?: return@addMouseWheelListener
    val hasMinorTickSpacing = s.minorTickSpacing > 0
    val tickSpacing = if (hasMinorTickSpacing) s.minorTickSpacing else s.majorTickSpacing
    val v = s.value - e.wheelRotation * tickSpacing
    val m = s.model
    s.value = minOf(m.maximum, maxOf(v, m.minimum))
  }
  if (slider.ui is WindowsSliderUI) {
    slider.ui = WindowsSnapToTicksDragSliderUI(slider)
  } else {
    slider.ui = MetalSnapToTicksDragSliderUI()
  }
}

private class WindowsSnapToTicksDragSliderUI(slider: JSlider) : WindowsSliderUI(slider) {
  override fun createTrackListener(slider: JSlider): TrackListener {
    return object : TrackListener() {
      override fun mouseDragged(e: MouseEvent) {
        if (!slider.snapToTicks || slider.majorTickSpacing == 0) {
          super.mouseDragged(e)
          return
        }
        // case HORIZONTAL:
        val halfThumbWidth = thumbRect.width / 2
        val trackLength = trackRect.width
        val trackLeft = trackRect.x - halfThumbWidth
        val trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth
        val pos = e.x
        val snappedPos = when {
          pos <= trackLeft -> trackLeft
          pos >= trackRight -> trackRight
          else -> {
            offset = 0
            val possibleTickPositions = slider.maximum - slider.minimum
            val hasMinorTick = slider.minorTickSpacing > 0
            val tickSpacing = if (hasMinorTick) slider.minorTickSpacing else slider.majorTickSpacing
            val actualPixelsForOneTick = trackLength * tickSpacing / possibleTickPositions.toFloat()
            val px = pos - trackLeft
            ((px / actualPixelsForOneTick).roundToInt() * actualPixelsForOneTick).roundToInt() + trackLeft
          }
        }
        e.translatePoint(snappedPos - pos, 0)
        super.mouseDragged(e)
      }
    }
  }
}

private class MetalSnapToTicksDragSliderUI : MetalSliderUI() {
  override fun createTrackListener(slider: JSlider): TrackListener {
    return object : TrackListener() {
      override fun mouseDragged(e: MouseEvent) {
        if (!slider.snapToTicks || slider.majorTickSpacing == 0) {
          super.mouseDragged(e)
          return
        }
        // case HORIZONTAL:
        val halfThumbWidth = thumbRect.width / 2
        val trackLength = trackRect.width
        val trackLeft = trackRect.x - halfThumbWidth
        val trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth
        val pos = e.x
        val snappedPos = when {
          pos <= trackLeft -> trackLeft
          pos >= trackRight -> trackRight
          else -> {
            offset = 0
            val possibleTickPositions = slider.maximum - slider.minimum
            val hasMinorTick = slider.minorTickSpacing > 0
            val tickSpacing = if (hasMinorTick) slider.minorTickSpacing else slider.majorTickSpacing
            val actualPixelsForOneTick = trackLength * tickSpacing / possibleTickPositions.toFloat()
            val px = pos - trackLeft
            ((px / actualPixelsForOneTick).roundToInt() * actualPixelsForOneTick).roundToInt() + trackLeft
          }
        }
        e.translatePoint(snappedPos - pos, 0)
        super.mouseDragged(e)
      }
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
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      contentPane.add(makeUI())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
