package example

import java.awt.*
import java.util.Optional
import javax.swing.*

class MainPanel : JPanel(BorderLayout()) {
  private val spinner = JSpinner()

  init {
    val lv = UIManager.get("Tree.timeFactor") as Number
    spinner.setModel(SpinnerNumberModel(lv, 0L, 5000L, 500L))
    UIManager.put("List.timeFactor", 5000L)

    val model = arrayOf("a", "aa", "b", "bbb", "bbc")
    val combo = JComboBox<String>(model)
    combo.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM")

    val p = JPanel().apply {
      add(spinner)
      add(combo)
    }

    val tabbedPane = JTabbedPane().apply {
      add("ComboBox.timeFactor", p)
      add("List.timeFactor", JScrollPane(JList<String>(model)))
      add("Table.timeFactor(JFileChooser)", JFileChooser())
      add("Tree.timeFactor", JScrollPane(JTree()))
    }

    add(tabbedPane)
    setPreferredSize(Dimension(320, 240))
  }

  override fun updateUI() {
    val lv = Optional.ofNullable(spinner)
        .map { it.getModel().getValue() }
        .orElse(1000L)
    // Unnecessary safe call on a non-null receiver of type JSpinner
    // val lv = spinner
    //     ?.let { it.getModel().getValue() }
    //     ?: 1000L
    // NullPointerException
    // val lv = spinner.getModel().getValue()
    UIManager.put("ComboBox.timeFactor", lv)
    UIManager.put("List.timeFactor", lv)
    UIManager.put("Table.timeFactor", lv)
    UIManager.put("Tree.timeFactor", lv)
    super.updateUI()
  }
}

internal object LookAndFeelUtil {
  private var lookAndFeel = UIManager.getLookAndFeel().javaClass.getName()

  fun createLookAndFeelMenu(): JMenu {
    val menu = JMenu("LookAndFeel")
    val lafRadioGroup = ButtonGroup()
    for (lafInfo in UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup))
    }
    return menu
  }

  private fun createLookAndFeelItem(lafName: String, lafClassName: String, lafRadioGroup: ButtonGroup): JRadioButtonMenuItem {
    val lafItem = JRadioButtonMenuItem(lafName, lafClassName == lookAndFeel)
    lafItem.setActionCommand(lafClassName)
    lafItem.setHideActionText(true)
    lafItem.addActionListener({
      val m = lafRadioGroup.getSelection()
      try {
        setLookAndFeel(m.getActionCommand())
      } catch (ex: ClassNotFoundException) {
        ex.printStackTrace()
      } catch (ex: InstantiationException) {
        ex.printStackTrace()
      } catch (ex: IllegalAccessException) {
        ex.printStackTrace()
      } catch (ex: UnsupportedLookAndFeelException) {
        ex.printStackTrace()
      }
    })
    lafRadioGroup.add(lafItem)
    return lafItem
  }

  @Throws(ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class, UnsupportedLookAndFeelException::class)
  private fun setLookAndFeel(lookAndFeel: String) {
    val oldLookAndFeel = LookAndFeelUtil.lookAndFeel
    if (oldLookAndFeel != lookAndFeel) {
      UIManager.setLookAndFeel(lookAndFeel)
      LookAndFeelUtil.lookAndFeel = lookAndFeel
      updateLookAndFeel()
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private fun updateLookAndFeel() {
    for (window in Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window)
    }
  }
}/* Singleton */

fun main() {
  EventQueue.invokeLater(object : Runnable {
    override fun run() {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      } catch (ex: ClassNotFoundException) {
        ex.printStackTrace()
      } catch (ex: InstantiationException) {
        ex.printStackTrace()
      } catch (ex: IllegalAccessException) {
        ex.printStackTrace()
      } catch (ex: UnsupportedLookAndFeelException) {
        ex.printStackTrace()
      }
      val mb = JMenuBar()
      mb.add(LookAndFeelUtil.createLookAndFeelMenu())

      JFrame().apply {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        getContentPane().add(MainPanel())
        setJMenuBar(mb)
        pack()
        setLocationRelativeTo(null)
        setVisible(true)
      }
    }
  })
}
