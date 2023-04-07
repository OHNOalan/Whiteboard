package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import net.codebot.application.components.AppData
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.NodeData
import net.codebot.application.components.tools.styles.LineStyles
import kotlin.math.abs
import kotlin.math.min

class LineTool(container: HBox, stylebar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/line.png",
    "file:src/main/assets/cursors/line.png",
    "Line",
    ToolIndex.LINE,
) {
    override val stylesControl = LineStyles(stylebar, this)
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
        line.userData = NodeData(EntityIndex.SEGMENT, AppData.generateNodeId())
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
