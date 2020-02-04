package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

private const val MARK = "\u00a7" // "��"

fun makeUI(): Component {
  val tree = object : JTree(makeModel()) {
    override fun updateUI() {
      setCellRenderer(null)
      super.updateUI()
      // setCellRenderer(ChapterNumberingTreeCellRenderer())
      val r = getCellRenderer()
      setCellRenderer { tree, value, selected, expanded, leaf, row, hasFocus ->
        val c = r.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
        if (value is DefaultMutableTreeNode) {
          val tn = value.path
          val s = (1 until tn.size) // ignore the root node by skipping index 0
            .map { 1 + tn[it - 1].getIndex(tn[it]) }
            .joinToString(".")
          (c as? JLabel)?.text = "$MARK$s $value"
        }
        return@setCellRenderer c
      }
      isRootVisible = false
    }
  }
  val p = JPanel(BorderLayout())
  p.add(JScrollPane(tree))
  p.preferredSize = Dimension(320, 240)
  return p
}

private fun makeModel(): TreeModel {
  val root = DefaultMutableTreeNode("root")
  root.add(DefaultMutableTreeNode("Introduction"))
  root.add(makePart())
  root.add(makePart())
  return DefaultTreeModel(root)
}

private fun makePart(): DefaultMutableTreeNode {
  val c1 = DefaultMutableTreeNode("Chapter")
  c1.add(DefaultMutableTreeNode("Section"))
  c1.add(DefaultMutableTreeNode("Section"))
  c1.add(DefaultMutableTreeNode("Section"))
  val c2 = DefaultMutableTreeNode("Chapter")
  c2.add(DefaultMutableTreeNode("aaa aaa aaa"))
  c2.add(DefaultMutableTreeNode("bb bb"))
  c2.add(DefaultMutableTreeNode("cc"))
  val p1 = DefaultMutableTreeNode("Part")
  p1.add(c1)
  p1.add(c2)
  return p1
}

// class ChapterNumberingTreeCellRenderer : DefaultTreeCellRenderer() {
//   override fun getTreeCellRendererComponent(
//     tree: JTree,
//     value: Any?,
//     selected: Boolean,
//     expanded: Boolean,
//     leaf: Boolean,
//     row: Int,
//     hasFocus: Boolean
//   ): Component {
//     val c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
//     if (value is DefaultMutableTreeNode) {
//       val tn = value.path
//       val s = (1 until  tn.size) // ignore the root node by skipping index 0
//         .map { 1 + tn[it - 1].getIndex(tn[it]) }
//         .joinToString(".")
//       (c as? JLabel)?.text = "$MARK$s $value"
//     }
//     return c
//   }
// }

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
