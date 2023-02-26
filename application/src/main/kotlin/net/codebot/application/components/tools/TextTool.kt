package net.codebot.application.components.tools

import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.text.Font

class TextTool(container: HBox) : BaseTool(
    container,
    "file:src/main/assets/cursors/text.png",
    "file:src/main/assets/cursors/text.png",
    "Text",
    ToolIndex.TEXT,
) {
    override fun canvasMousePressed(e: MouseEvent) {
        val text = TextArea("Hello")
        text.font = Font("Helvetica", 24.0)
        text.translateX = e.x
        text.translateY = e.y
        text.prefWidth = 100.0
        text.prefHeight = 20.0
        text.style = "-fx-background-color: #000000"
        canvasReference.addDrawnNode(text)
    }
}
