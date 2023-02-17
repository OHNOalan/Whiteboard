package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ColorPicker
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.AppSidebar
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.tools.styles.PenStyles

class PenTool(container: HBox, stylebar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/pen.png",
    "file:src/main/assets/cursors/pen.png",
    "Pen",
    ToolIndex.PEN,
) {
    private val radiusOffset: Double = 3.0
    var lineColor: Color = Color.BLACK
        set(value) {
            field = value
            canvasReference.context.stroke = value
            pointer.stroke = value
        }
    var lineWidth = 1.0
        set(value) {
            field = value
            canvasReference.context.lineWidth = value
            pointer.radius = value + radiusOffset
        }
    private val pointer = Circle(-100.0, -100.0, lineWidth + radiusOffset)
    override val stylesControl = PenStyles(stylebar, this)

    init {
        pointer.fill = null
        pointer.stroke = lineColor
        pointer.isPickOnBounds = false
    }

    override fun onSelectTool() {
        canvasReference.context.stroke = lineColor
        canvasReference.context.lineWidth = lineWidth
        canvasReference.pane.children.add(pointer)
    }

    override fun onDeselectTool() {
        canvasReference.pane.children.remove(pointer)
    }

    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.beginPath()
        context.lineTo(e.x, e.y)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.lineTo(e.x, e.y)
        context.stroke()
        pointer.centerX = e.x
        pointer.centerY = e.y
    }

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.lineTo(e.x, e.y)
        context.stroke()
        context.closePath()
    }

    override fun canvasMouseMoved(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        pointer.centerX = e.x
        pointer.centerY = e.y
    }
}