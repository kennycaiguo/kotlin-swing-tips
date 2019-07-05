package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val p = JPanel(GridLayout(0, 1, 0, 2))

  val tabbedPane1 = object : JTabbedPane() {
    override fun removeTabAt(index: Int) {
      if (getTabCount() > 0) {
        setSelectedIndex(0)
        super.removeTabAt(index)
        setSelectedIndex(index - 1)
      } else {
        super.removeTabAt(index)
      }
    }
  }

  val tabbedPane2 = object : JTabbedPane() {
    private fun getScrollableViewport(): Component? {
      var cmp: Component? = null
      for (c in getComponents()) {
        if ("TabbedPane.scrollableViewport" == c.getName()) {
          cmp = c
          break
        }
      }
      return cmp
    }

    private fun resetViewportPosition(idx: Int) {
      if (getTabCount() <= 0) {
        return
      }
      val viewport = getScrollableViewport() as? JViewport ?: return
      (viewport.getView() as? JComponent)?.scrollRectToVisible(getBoundsAt(idx))
    }

    override fun removeTabAt(index: Int) {
      if (getTabCount() > 0) {
        resetViewportPosition(0)
        super.removeTabAt(index)
        resetViewportPosition(index - 1)
      } else {
        super.removeTabAt(index)
      }
    }
  }

  val list = listOf(JTabbedPane(), tabbedPane1, tabbedPane2)
  list.forEach {
    it.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT)
    it.addTab("00000000", JLabel("0"))
    it.addTab("11111111", JLabel("1"))
    it.addTab("22222222", JLabel("2"))
    it.addTab("33333333", JLabel("3"))
    it.addTab("44444444", JLabel("4"))
    it.addTab("55555555", JLabel("5"))
    it.addTab("66666666", JLabel("6"))
    it.addTab("77777777", JLabel("7"))
    it.addTab("88888888", JLabel("8"))
    it.addTab("99999999", JLabel("9"))
    it.setSelectedIndex(it.getTabCount() - 1)
    // TEST: EventQueue.invokeLater { it.setSelectedIndex(tabs.getTabCount() - 1) };
    p.add(it)
  }

  val button = JButton("Remove")
  button.addActionListener {
    list.filter { it.getTabCount() > 0 }.forEach {
      it.removeTabAt(it.getTabCount() - 1)
    }
  }

  return JPanel(BorderLayout()).also {
    it.add(p)
    it.add(button, BorderLayout.SOUTH)
    it.setPreferredSize(Dimension(320, 240))
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
      getContentPane().add(makeUI())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
