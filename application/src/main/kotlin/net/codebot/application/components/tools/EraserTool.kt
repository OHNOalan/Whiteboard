package net.codebot.application.components.tools

import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
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
    private val radiusOffset: Double = 1.0
    var lineWidth = 30.0
        set(value) {
            field = value
            pointer.radius = value + radiusOffset
        }
    private val pointer = Circle(-100.0, -100.0, lineWidth + radiusOffset)
    override val stylesControl = EraserStyles(stylebar, this)

    init {
        pointer.fill = null
        pointer.stroke = Color.BLACK
        pointer.isPickOnBounds = false
    }

    private fun eraseNodes() {
        val toBeErased = mutableListOf<Node>()
        for (node in canvasReference.children) {
            if (node != pointer && node.boundsInParent.intersects(pointer.boundsInParent)) {
                toBeErased.add(node)
            }
        }
        for (node in toBeErased) {
            canvasReference.removeDrawnNode(node)
        }
    }

    override fun onSelectTool() {
        canvasReference.children.add(pointer)
    }

    override fun onDeselectTool() {
        canvasReference.children.remove(pointer)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        eraseNodes()
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        eraseNodes()
        pointer.centerX = e.x
        pointer.centerY = e.y
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        eraseNodes()
    }

    override fun canvasMouseMoved(e: MouseEvent) {
        pointer.centerX = e.x
        pointer.centerY = e.y
    }
}
