package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import net.codebot.application.components.*
import net.codebot.application.components.tools.styles.TextStyles
import kotlin.math.max

/**
 * The set of properties for the text tool.
 * @param container The container to add the tool to.
 * @param styleBar The style bar for this tool where all customization options are
 * displayed.
 */
class TextTool(container: HBox, styleBar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/text.png",
    "file:src/main/assets/cursors/text.png",
    "Text",
    ToolIndex.TEXT,
) {
    override val stylesControl = TextStyles(styleBar)
    private lateinit var selectionRectangle: Rectangle
    private val selectionLineColor: Color = Color.DODGERBLUE
    private var initX = 0.0
    private var initY = 0.0

    /**
     * Creates a bounding rectangle to use while the mouse is being dragged.
     * Shows the area that the text tool will occupy.
     * @return A rectangle object to display while the mouse is being dragged.
     */
    private fun createBoundingRectangle(
        x: Double,
        y: Double,
        stroke: Color,
    ): Rectangle {
        val rectangle = Rectangle()
        rectangle.stroke = stroke
        rectangle.strokeWidth = 1.0
        rectangle.isPickOnBounds = false
        rectangle.fill = null
        initX = x
        initY = y
        rectangle.translateX = x
        rectangle.translateY = y
        canvasReference.children.add(rectangle)
        return rectangle
    }

    /**
     * Removes the bounding rectangle of the text box once the mouse is released.
     * @see createBoundingRectangle
     */
    private fun resizeBoundingRectangle(rectangle: Rectangle, x: Double, y: Double) {
        if (x < initX) {
            rectangle.translateX = x
            rectangle.width = initX - x
        } else {
            rectangle.translateX = initX
            rectangle.width = x - initX
        }
        if (y < initY) {
            rectangle.translateY = y
            rectangle.height = initY - y
        } else {
            rectangle.translateY = initY
            rectangle.height = y - initY
        }
    }

    /**
     * Removes the bounding rectangle of the text box.
     * @see createBoundingRectangle
     */
    private fun removeBoundingRectangle(rectangle: Rectangle) {
        canvasReference.children.remove(rectangle)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        initX = e.x
        initY = e.y
        selectionRectangle = createBoundingRectangle(e.x, e.y, selectionLineColor)
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        val width: Double
        val height: Double
        if (e.x > initX) {
            width = e.x - initX
        } else {
            width = initX - e.x
            initX = e.x
        }
        if (e.y > initY) {
            height = e.y - initY
        } else {
            height = initY - e.y
            initY = e.y
        }
        removeBoundingRectangle(selectionRectangle)
        val editor = AppTextEditor()
        editor.translateX = initX
        editor.translateY = initY
        editor.previousTranslateX = initX
        editor.previousTranslateY = initY
        editor.prefWidth = max(width, 180.0)
        editor.prefHeight = max(height, 50.0)
        editor.userData = NodeData(
            EntityIndex.TEXT,
            AppData.generateNodeId(),
            System.currentTimeMillis()
        )
        canvasReference.addDrawnNode(editor)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        resizeBoundingRectangle(selectionRectangle, e.x, e.y)
    }
}
