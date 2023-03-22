package net.codebot.application.components.tools

import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polyline
import javafx.scene.shape.StrokeLineCap
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.EntityIndex
import net.codebot.application.components.tools.styles.PenStyles


class PenTool(container: HBox, stylebar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/pen.png",
    "file:src/main/assets/cursors/pen.png",
    "Pen",
    ToolIndex.PEN,
) {
    private val radiusOffset: Double = 1.0
    private val maxSampling = 2
    var lineColor: Color = Color.BLACK
        set(value) {
            field = value
            pointer.stroke = value
        }
    var lineWidth = 1.0
        set(value) {
            field = value
            pointer.radius = value + radiusOffset
        }
    private val pointer = Circle(-100.0, -100.0, lineWidth + radiusOffset)
    override val stylesControl = PenStyles(stylebar, this)
    private lateinit var currentPolyline: Polyline
    private var currentSampling = maxSampling

    init {
        pointer.fill = null
        pointer.stroke = lineColor
        pointer.isPickOnBounds = false
    }

    override fun onSelectTool() {
        canvasReference.children.add(pointer)
    }

    override fun onDeselectTool() {
        canvasReference.children.remove(pointer)
    }

    override fun canvasMousePressed(e: MouseEvent) {
        currentPolyline = Polyline()
        currentPolyline.stroke = lineColor
        currentPolyline.strokeWidth = lineWidth
        currentPolyline.strokeLineCap = StrokeLineCap.ROUND
        currentPolyline.points.addAll(
            *arrayOf(
                e.x, e.y
            )
        )
        currentPolyline.userData = EntityIndex.LINE
        // We let the current line be a preview only and will only commit if mouse is released
        canvasReference.children.add(currentPolyline)
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        currentSampling--
        if (currentSampling <= 0) {
            currentSampling= maxSampling
            currentPolyline.points.addAll(
            *arrayOf(
                e.x, e.y
            )
            )
        }
        pointer.centerX = e.x
        pointer.centerY = e.y
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        currentPolyline.points.addAll(
            *arrayOf(
                e.x, e.y
            )
        )
        // Commit the current line by removing its preview element and adding the drawn node
        canvasReference.children.remove(currentPolyline)
        canvasReference.addDrawnNode(currentPolyline)
    }

    override fun canvasMouseMoved(e: MouseEvent) {
        pointer.centerX = e.x
        pointer.centerY = e.y
    }
}
