package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ColorPicker
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import net.codebot.application.components.AppCanvas

// TODO get better images for this
class PenTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/pen.png",
    "file:src/main/assets/cursors/pen.png",
    "Pen",
    ToolIndex.PEN,
) {
    var lineColor = ColorPicker(Color.BLACK)

    override fun onSelectTool(canvas: AppCanvas) {
        canvas.context.stroke = lineColor.value
        canvas.context.lineWidth = 1.0
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