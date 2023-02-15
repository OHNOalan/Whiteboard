package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.TextFlow

class TextTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/pen.png",
    "file:src/main/assets/cursors/pen.png",
    "Text",
    ToolIndex.TEXT,
) {
    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
//        val text = Text(e.x, e.y, "Hello")
        var text = TextField("Hello")
        text.font = Font("Helvetica", 24.0)
//        val button = Button("button")
//        button.translateX = 10.0
        pane.children.add(text)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        TODO("Not yet implemented")
    }

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {
//        val text = Text(e.x, e.y, "Hello")
//        var text = TextArea("Hello")
//        pane.children.add(text)
    }
}