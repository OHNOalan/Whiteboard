package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import net.codebot.application.components.AppCanvas

// TODO get better images for this
class EraserTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/eraser.png",
    "file:src/main/assets/cursors/eraser.png",
    "Eraser",
    ToolIndex.ERASER,
) {
    override fun onSelectTool(canvas: AppCanvas) {
        canvas.context.stroke = canvas.backgroundColour.value
        canvas.context.lineWidth = 10.0
    }

    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.beginPath()
        context.lineTo(e.x, e.y)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.lineTo(e.x, e.y)
        context.stroke()
    }

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        context.lineTo(e.x, e.y)
        context.stroke()
        context.closePath()
    }
}