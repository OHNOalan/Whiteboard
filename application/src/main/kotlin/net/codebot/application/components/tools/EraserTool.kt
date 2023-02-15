package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane

// TODO get better images for this
class EraserTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/eraser.png",
    "file:src/main/assets/cursors/eraser.png",
    "Eraser",
    ToolIndex.ERASER,
) {
    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        // TODO implement this
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        // TODO implement this
    }

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        // TODO implement this
    }
}