package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.StrokeLineCap
import net.codebot.application.components.AppData
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.NodeData
import net.codebot.application.components.tools.styles.LineStyles

/**
 * The set of properties for the line tool.
 * @param container The container to add the tool to.
 * @param styleBar The style bar for this tool where all customization options are
 * displayed.
 */
class LineTool(container: HBox, styleBar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/line.png",
    "file:src/main/assets/cursors/line.png",
    "Line",
    ToolIndex.LINE,
) {
    override val stylesControl = LineStyles(styleBar, this)
    private lateinit var line: Line
    var lineColor: Color = Color.BLACK
    var lineWidth: Double = 1.0

    override fun canvasMousePressed(e: MouseEvent) {
        line = Line()
        line.strokeWidth = lineWidth
        line.strokeLineCap = StrokeLineCap.ROUND
        line.stroke = lineColor
        line.startX = e.x
        line.startY = e.y
        line.endX = e.x + 0.1
        line.endY = e.y + 0.1
        line.userData = NodeData(
            EntityIndex.SEGMENT,
            AppData.generateNodeId(),
            System.currentTimeMillis()
        )
        canvasReference.children.add(line)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        line.endX = e.x
        line.endY = e.y
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        canvasReference.children.remove(line)
        canvasReference.addDrawnNode(line)
    }
}
