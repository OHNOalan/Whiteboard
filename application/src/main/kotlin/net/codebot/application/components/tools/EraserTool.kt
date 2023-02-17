package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.tools.styles.EraserStyles

class EraserTool(container: HBox, stylebar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/eraser.png",
    "file:src/main/assets/cursors/eraser.png",
    "Eraser",
    ToolIndex.ERASER,
) {
    private val radiusOffset: Double = 3.0
    public var lineWidth = 30.0
        set(value) {
            field = value
            canvasReference.context.lineWidth = value
            pointer.radius = value + radiusOffset
        }
    private val pointer = Circle(-100.0, -100.0, lineWidth + radiusOffset)
    override val stylesControl = EraserStyles(stylebar, this)

    init {
        pointer.fill = null
        pointer.stroke= Color.BLACK
        pointer.isPickOnBounds = false
    }

    override fun onSelectTool() {
        canvasReference.context.stroke = canvasReference.backgroundColour.value
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