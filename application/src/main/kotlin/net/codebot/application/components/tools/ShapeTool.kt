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

class ShapeTool(container: HBox, stylebar: AppStylebar) : BaseTool (
    container,
    "file:src/main/assets/cursors/shapes.png",
    "file:src/main/assets/cursors/shapes.png",
    "Shape",
    ToolIndex.SHAPE,
) {
    override val stylesControl = ShapeStyles(stylebar, this)
    private lateinit var rectangle : Rectangle
    private lateinit var ellipse : Ellipse
    private var startX = 0.0
    private var startY = 0.0
    var lineColor: Color = Color.BLACK
    var fillShape = false
    var onCreateShape: (Double, Double) -> Unit = { x, y -> onCreateRectangle(x, y) }
    var onMoveShape: (Double, Double) -> Unit = { x, y -> onMoveRectangle(x, y) }
    var onReleaseShape: () -> Unit = { onReleaseRectangle() }

    fun onCreateRectangle(x: Double, y: Double) {
        rectangle = Rectangle()
        rectangle.stroke = lineColor
        rectangle.isPickOnBounds = false
        rectangle.fill = if (fillShape) lineColor else null
        startX = x
        startY = y
        rectangle.translateX = x
        rectangle.translateY = y
        rectangle.userData = NodeData(EntityIndex.RECTANGLE, AppData.generateNodeId(), System.currentTimeMillis())
        canvasReference.children.add(rectangle)
    }

    fun onCreateEllipse(x: Double, y: Double) {
        ellipse = Ellipse()
        ellipse.stroke = lineColor
        ellipse.isPickOnBounds = false
        ellipse.fill = if (fillShape) lineColor else null
        ellipse.centerX = x
        ellipse.centerY = y
        ellipse.userData = NodeData(EntityIndex.ELLIPSE, AppData.generateNodeId(), System.currentTimeMillis())
        canvasReference.children.add(ellipse)
    }

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

    fun onMoveSquare(x: Double, y: Double) {
        onMoveRectangle(x, y, true)
    }

    fun onMoveEllipse(x: Double, y: Double, isCircle: Boolean = false) {
        ellipse.radiusX = abs(x - ellipse.centerX)

        if (isCircle) {
            ellipse.radiusY = abs(x - ellipse.centerX)
        } else {
            ellipse.radiusY = abs(y - ellipse.centerY)
        }
    }

    fun onMoveCircle(x: Double, y: Double) {
        onMoveEllipse(x, y, true)
    }

    fun onReleaseRectangle() {
        canvasReference.children.remove(rectangle)
        canvasReference.addDrawnNode(rectangle)
    }

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
