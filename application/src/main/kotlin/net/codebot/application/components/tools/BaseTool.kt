package net.codebot.application.components.tools

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.tools.styles.BaseStyles

/**
 * The base tool which all tools inherit from.
 * @param container The container to add the tool to.
 * @param imageUrl The path to the image for the tool's icon.
 * @param cursorImageUrl The path to the image for the cursor when the tool is selected.
 * @param buttonText The name of the tool.
 * @property toolId The index of the tool in the list of all tools.
 */
abstract class BaseTool(
    container: HBox,
    imageUrl: String,
    cursorImageUrl: String,
    buttonText: String,
    private val toolId: Int
) {
    private val cursorImage: Image = Image(cursorImageUrl, 32.0, 32.0, true, true)
    protected lateinit var canvasReference: AppCanvas
    protected open val stylesControl: BaseStyles? = null

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

    /**
     * Sets this tool as the current tool.
     */
    fun selectTool() {
        canvasReference.setTool(toolId, cursorImage)
        stylesControl?.create()
        onSelectTool()
    }

    /**
     * Unsets this tool as the current tool.
     */
    fun deselectTool() {
        stylesControl?.destroy()
        onDeselectTool()
    }

    /**
     * The callback for when this tool is selected.
     */
    protected open fun onSelectTool() {}

    /**
     * The callback for when this tool is deselected.
     */
    protected open fun onDeselectTool() {}

    /**
     * Saves the canvas for reference later.
     * @param canvas The main canvas of the app.
     */
    fun registerCanvas(canvas: AppCanvas) {
        canvasReference = canvas
    }

    /**
     * The callback for when this tool is selected and the mouse is clicked.
     */
    open fun canvasMousePressed(e: MouseEvent) {}

    /**
     * The callback for when this tool is selected and the mouse is moved while being
     * held down.
     */
    open fun canvasMouseDragged(e: MouseEvent) {}

    /**
     * The callback for when this tool is selected and the mouse is released.
     */
    open fun canvasMouseReleased(e: MouseEvent) {}

    /**
     * The callback for when this tool is selected and the mouse is moved but not
     * held down.
     */
    open fun canvasMouseMoved(e: MouseEvent) {}
}