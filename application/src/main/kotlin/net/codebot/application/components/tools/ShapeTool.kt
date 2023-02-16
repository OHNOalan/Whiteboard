package net.codebot.application.components.tools

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import net.codebot.application.components.AppCanvas

class ShapeTool(container: HBox) : BaseTool (
    container,
    "file:src/main/assets/cursors/shapes.png",
    "file:src/main/assets/cursors/shapes.png",
    "Shape",
    ToolIndex.SHAPE,
) {
    override fun onSelectTool(canvas: AppCanvas) {}

    override fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {
        val shape = Rectangle()
        shape.translateX = e.x
        shape.translateY = e.y
        shape.width = 100.0
        shape.height = 20.0
        pane.children.add(shape)
    }

    override fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {}

    override fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {}

}