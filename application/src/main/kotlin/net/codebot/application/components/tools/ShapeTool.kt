package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Rectangle
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.tools.styles.ShapeStyles
import kotlin.math.abs

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

    fun onCreateRectangle(x: Double, y: Double) {
        rectangle = Rectangle()
        rectangle.stroke = lineColor
        rectangle.isPickOnBounds = false
        rectangle.fill = if (fillShape) lineColor else null
        startX = x
        startY = y
        rectangle.translateX = x
        rectangle.translateY = y
        rectangle.userData = EntityIndex.RECTANGLE
        canvasReference.addDrawnNode(rectangle)
    }

    fun onCreateEllipse(x: Double, y: Double) {
        ellipse = Ellipse()
        ellipse.stroke = lineColor
        ellipse.isPickOnBounds = false
        ellipse.fill = if (fillShape) lineColor else null
        ellipse.centerX = x
        ellipse.centerY = y
        ellipse.userData = EntityIndex.ELLIPSE
        canvasReference.addDrawnNode(ellipse)
    }

    fun onMoveRectangle(x: Double, y: Double) {
        if (x < startX) {
            rectangle.translateX = x
            rectangle.width = startX - x
        } else {
            rectangle.translateX = startX
            rectangle.width = x - startX
        }
        if (y < startY) {
            rectangle.translateY = y
            rectangle.height = startY - y
        } else {
            rectangle.translateY = startY
            rectangle.height = y - startY
        }
    }

    fun onMoveSquare(x: Double, y: Double) {
        if (x < startX) {
            rectangle.translateX = x
            rectangle.width = startX - x
        } else {
            rectangle.translateX = startX
            rectangle.width = x - startX
        }
        if (y < startY) {
            rectangle.translateY = y
            rectangle.height = abs(startX - x)
        } else {
            rectangle.translateY = startY
            rectangle.height = abs(startX - x)
        }
    }

    fun onMoveEllipse(x: Double, y: Double) {
        ellipse.radiusX = abs(x - ellipse.centerX)
        ellipse.radiusY = abs(y - ellipse.centerY)
    }

    fun onMoveCircle(x: Double, y: Double) {
        ellipse.radiusX = abs(x - ellipse.centerX)
        ellipse.radiusY = abs(x - ellipse.centerX)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        onCreateShape(e.x, e.y)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        onMoveShape(e.x, e.y)
    }
}
