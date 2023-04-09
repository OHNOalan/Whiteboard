package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import net.codebot.application.components.AppData
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.NodeData
import net.codebot.application.components.AppTextEditor
import kotlin.math.max

class TextTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/text.png",
    "file:src/main/assets/cursors/text.png",
    "Text",
    ToolIndex.TEXT,
) {
    private lateinit var selectionRectangle: Rectangle
    private val selectionLineColor: Color = Color.DODGERBLUE
    private val lineWidth: Double = 2.0
    private var initX = 0.0
    private var initY = 0.0
    private fun onCreateSelection(x: Double, y: Double) {
        selectionRectangle =
            onCreateRectangle(x, y, selectionLineColor, strokeWidth = 1.0)
    }

    private fun onResizeSelection(x: Double, y: Double) {
        onResizeRectangle(selectionRectangle, x, y)
    }

    private fun onRemoveSelection() {
        onRemoveRectangle(selectionRectangle)
    }

    private fun onCreateRectangle(
        x: Double,
        y: Double,
        stroke: Color,
        strokeWidth: Double = lineWidth,
        fill: Color? = null
    ): Rectangle {
        val rectangle = Rectangle()
        rectangle.stroke = stroke
        rectangle.strokeWidth = strokeWidth
        rectangle.isPickOnBounds = false
        rectangle.fill = fill
        initX = x
        initY = y
        rectangle.translateX = x
        rectangle.translateY = y
        canvasReference.children.add(rectangle)
        return rectangle
    }

    private fun onResizeRectangle(rectangle: Rectangle, x: Double, y: Double) {
        if (x < initX) {
            rectangle.translateX = x
            rectangle.width = initX - x
        } else {
            rectangle.translateX = initX
            rectangle.width = x - initX
        }
        if (y < initY) {
            rectangle.translateY = y
            rectangle.height = initY - y
        } else {
            rectangle.translateY = initY
            rectangle.height = y - initY
        }
    }

    private fun onRemoveRectangle(rectangle: Rectangle) {
        canvasReference.children.remove(rectangle)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        initX = e.x
        initY = e.y
        onCreateSelection(e.x, e.y)
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        var width: Double
        var height: Double
        if (e.x > initX) {
            width = e.x - initX
        } else {
            width = initX - e.x
            initX = e.x
        }
        if (e.y > initY) {
            height = e.y - initY
        } else {
            height = initY - e.y
            initY = e.y
        }
        onRemoveSelection()
        val editor = AppTextEditor()
        editor.translateX = initX
        editor.translateY = initY
        editor.previousTranslateX = initX
        editor.previousTranslateY = initY
        editor.prefWidth = max(width, 180.0)
        editor.prefHeight = max(height, 50.0)
        editor.userData = NodeData(
            EntityIndex.TEXT,
            AppData.generateNodeId(),
            System.currentTimeMillis()
        )
        canvasReference.addDrawnNode(editor)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        onResizeSelection(e.x, e.y)
    }
}
