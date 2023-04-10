package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Rectangle
import net.codebot.application.components.AppData
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.NodeData
import net.codebot.application.components.tools.styles.ShapeStyles
import kotlin.math.abs
import kotlin.math.min

/**
 * The set of properties for the shape tool.
 * @param container The container to add the tool to.
 * @param styleBar The style bar for this tool where all customization options are
 * displayed.
 */
class ShapeTool(container: HBox, styleBar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/shapes.png",
    "file:src/main/assets/cursors/shapes.png",
    "Shape",
    ToolIndex.SHAPE,
) {
    override val stylesControl = ShapeStyles(styleBar, this)
    private lateinit var rectangle: Rectangle
    private lateinit var ellipse: Ellipse
    private var startX = 0.0
    private var startY = 0.0
    var lineColor: Color = Color.BLACK
    var fillShape = false
    var onCreateShape: (Double, Double) -> Unit = { x, y -> onCreateRectangle(x, y) }
    var onMoveShape: (Double, Double) -> Unit = { x, y -> onMoveRectangle(x, y) }
    var onReleaseShape: () -> Unit = { onReleaseRectangle() }

    /**
     * Called when the mouse is clicked and the rectangle/square shapes are selected.
     * Creates a rectangle object at with the starting corner fixed at the given
     * coordinates.
     */
    fun onCreateRectangle(x: Double, y: Double) {
        rectangle = Rectangle()
        rectangle.stroke = lineColor
        rectangle.isPickOnBounds = false
        rectangle.fill = if (fillShape) lineColor else null
        startX = x
        startY = y
        rectangle.translateX = x
        rectangle.translateY = y
        rectangle.userData = NodeData(
            EntityIndex.RECTANGLE,
            AppData.generateNodeId(),
            System.currentTimeMillis()
        )
        canvasReference.children.add(rectangle)
    }

    /**
     * Called when the mouse is clicked and the ellipse/circle shapes are selected.
     * Creates an ellipse object at with the centre fixed at the given coordinates.
     */
    fun onCreateEllipse(x: Double, y: Double) {
        ellipse = Ellipse()
        ellipse.stroke = lineColor
        ellipse.isPickOnBounds = false
        ellipse.fill = if (fillShape) lineColor else null
        ellipse.centerX = x
        ellipse.centerY = y
        ellipse.userData = NodeData(
            EntityIndex.ELLIPSE,
            AppData.generateNodeId(),
            System.currentTimeMillis()
        )
        canvasReference.children.add(ellipse)
    }

    /**
     * Called when the mouse is dragged and the current shape is a rectangle or square.
     * Changes the shape to form a rectangle between the starting coordinates and the
     * current mouse position.
     *
     * If the current shape is a square, changes the shape to form a square with the
     * side length of the smallest difference between the starting coordinates and the
     * current mouse position on both axes.
     */
    fun onMoveRectangle(x: Double, y: Double, isSquare: Boolean = false) {
        var width = abs(x - startX)
        var height = abs(y - startY)
        if (isSquare) {
            width = min(width, height)
            height = width
        }

        if (x < startX) {
            rectangle.translateX = if (isSquare) startX - width else x
        } else {
            rectangle.translateX = startX
        }

        if (y < startY) {
            rectangle.translateY = if (isSquare) startY - width else y
        } else {
            rectangle.translateY = startY
        }

        rectangle.width = width
        rectangle.height = height
    }

    /**
     * Called when the mouse is dragged and the current shape is a square.
     * Calls `onMoveRectangle` with the `isSquare` parameter set to `true`.
     * @see onMoveRectangle
     */
    fun onMoveSquare(x: Double, y: Double) {
        onMoveRectangle(x, y, true)
    }

    /**
     * Called when the mouse is dragged and the current shape is an ellipse or circle.
     * Changes the shape to form an ellipse centered at the starting position and the
     * edge meeting the current mouse position.
     *
     * If the current shape is a circle, changes the shape to form a circle centered at
     * the starting position and the radius being the distance from the starting
     * position to the current mouse position.
     */
    fun onMoveEllipse(x: Double, y: Double, isCircle: Boolean = false) {
        ellipse.radiusX = abs(x - ellipse.centerX)

        if (isCircle) {
            ellipse.radiusY = abs(x - ellipse.centerX)
        } else {
            ellipse.radiusY = abs(y - ellipse.centerY)
        }
    }

    /**
     * Called when the mouse is dragged and the current shape is a circle.
     * Calls `onMoveCircle` with the `isCircle` parameter set to `true`.
     * @see onMoveEllipse
     */
    fun onMoveCircle(x: Double, y: Double) {
        onMoveEllipse(x, y, true)
    }

    /**
     * Called when the mouse is released and the current shape is a rectangle/square.
     * Freezes the current shape into place and adds it to the canvas.
     */
    fun onReleaseRectangle() {
        canvasReference.children.remove(rectangle)
        canvasReference.addDrawnNode(rectangle)
    }

    /**
     * Called when the mouse is released and the current shape is an ellipse/circle.
     * Freezes the current shape into place and adds it to the canvas.
     */
    fun onReleaseEllipse() {
        canvasReference.children.remove(ellipse)
        canvasReference.addDrawnNode(ellipse)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        onCreateShape(e.x, e.y)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        onMoveShape(e.x, e.y)
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        onReleaseShape()
    }
}
