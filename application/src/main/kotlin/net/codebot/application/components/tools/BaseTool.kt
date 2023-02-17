package net.codebot.application.components.tools

import net.codebot.application.components.tools.Text
import javafx.event.EventHandler
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.tools.styles.BaseStyles

abstract class BaseTool(container: HBox, imageUrl: String, cursorImageUrl: String, buttonText: String, private val toolId: Int) {
    private var cursorImage : Image = Image(cursorImageUrl, 32.0, 32.0, true, true)
    protected lateinit var canvasReference : AppCanvas
    protected open val stylesControl : BaseStyles? = null

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
        canvasReference.setTool(toolId, cursorImage)
        stylesControl?.create()
        onSelectTool()
    }

    fun deselectTool() {
        stylesControl?.destroy()
        onDeselectTool()
    }

    protected open fun onSelectTool() {}

    protected open fun onDeselectTool() {}

    fun registerCanvas(canvas: AppCanvas) {
        canvasReference = canvas
    }

    open fun canvasMousePressed(e: MouseEvent, context: GraphicsContext, pane: Pane) {}
    open fun canvasMouseDragged(e: MouseEvent, context: GraphicsContext, pane: Pane) {}
    open fun canvasMouseReleased(e: MouseEvent, context: GraphicsContext, pane: Pane) {}
    open fun canvasMouseMoved(e: MouseEvent, context: GraphicsContext, pane: Pane) {}
}