package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox

// TODO get better images for this
class PenTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/pen.png",
    "file:src/main/assets/cursors/pen.png",
    "Pen",
    ToolIndex.PEN,
) {
    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext) {
        context.beginPath()
        context.lineTo(e.x, e.y)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext) {
        context.lineTo(e.x, e.y)
        context.stroke()
    }

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext) {
        context.lineTo(e.x, e.y)
        context.stroke()
        context.closePath()
    }
}