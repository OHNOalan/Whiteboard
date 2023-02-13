package net.codebot.application.components.tools

import javafx.event.EventHandler
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import net.codebot.application.components.AppCanvas

abstract class BaseTool(container: HBox, imageUrl: String, cursorImageUrl: String, buttonText: String, private val toolId: Int) {
    private var canvasReference : AppCanvas? = null
    private var cursorImage : Image = Image(cursorImageUrl)

    init {
        val image = ImageView(Image(imageUrl, 80.0, 80.0, true, true))
        image.maxHeight(90.0)
        val button = Button(buttonText, image)
        button.minHeight(40.0)
        button.prefHeight(90.0)
        button.onMouseClicked = EventHandler {
            selectTool()
        }
        container.children.add(button)
    }

    fun selectTool() {
        canvasReference?.setTool(toolId, cursorImage)
    }

    fun registerCanvas(canvas: AppCanvas) {
        canvasReference = canvas
    }

    abstract fun canvasMousePressed(e: MouseEvent, context: GraphicsContext)
    abstract fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext)
    abstract fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext)
}