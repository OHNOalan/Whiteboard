package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import net.codebot.application.components.AppCanvas

class TextTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/text.png",
    "file:src/main/assets/cursors/text.png",
    "Text",
    ToolIndex.TEXT,
) {
    override fun onSelectTool(canvas: AppCanvas) {}

    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        val text = TextArea("Hello")
        text.font = Font("Helvetica", 24.0)
        text.translateX = e.x
        text.translateY = e.y
        text.prefWidth = 100.0
        text.prefHeight = 20.0
        text.style = "-fx-background-color: #000000"
        pane.children.add(text)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {}

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {}
}