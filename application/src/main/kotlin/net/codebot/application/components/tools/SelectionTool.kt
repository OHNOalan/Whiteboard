package net.codebot.application.components.tools

import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Polyline
import javafx.scene.shape.Rectangle
import net.codebot.application.components.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SelectionTool(container: HBox, var stylebar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/selection.png",
    "file:src/main/assets/cursors/selection.png",
    "Select",
    ToolIndex.SELECTION,
) {
    // selectionRectangle is for making the selection
    // selectedRectangle is for displaying the resulting selection
    private lateinit var selectionRectangle: Rectangle
    private lateinit var selectedRectangle: Rectangle
    private var itemIsSelected = false
    private var moving = false
    private var editing = false
    private var corners = listOf<Rectangle>()
    private val selectedNodes = mutableListOf<Node>()
    private lateinit var editNode: Node
    private var textControlContainer: HBox = HBox()

    private var startX = 0.0
    private var startY = 0.0
    private var moveX = 0.0
    private var moveY = 0.0

    private val selectionLineColor: Color = Color.DODGERBLUE
    private val selectedLineColor: Color = Color.DEEPSKYBLUE
    private val cornerLineColor: Color = selectedLineColor
    private val cornerLineFill: Color = Color.WHITE

    private val lineWidth: Double = 2.0
    private val cornerRectangleSize: Double = 11.0

    init {
        textControlContainer.prefWidth = 200.0
        textControlContainer.prefHeight = 100.0
        textControlContainer.style = ("-fx-padding: 10;" + "-fx-border-style: solid inside;"
                + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: blue;")
    }

    val onCreateShape: (Double, Double) -> Unit =
        { x, y -> selectionRectangle = onCreateRectangle(x, y, selectionLineColor, strokeWidth = 1.0) }
    val onResizeShape: (Double, Double) -> Unit = { x, y -> onResizeRectangle(selectionRectangle, x, y) }

    private fun onCreateRectangle(
        x: Double,
        y: Double,
        stroke: Color,
        strokeWidth: Double = lineWidth,
        fill: Color? = null
    ): Rectangle {
        val rectangle = Rectangle()
        rectangle.stroke = stroke
        rectangle.strokeWidth = strokeWidth
        rectangle.isPickOnBounds = false
        rectangle.fill = fill
        startX = x
        startY = y
        rectangle.translateX = x
        rectangle.translateY = y
        canvasReference.children.add(rectangle)
        return rectangle
    }

    private fun onResizeRectangle(rectangle: Rectangle, x: Double, y: Double) {
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

    private fun isClickInSelectionBox(x: Double, y: Double): Boolean {
        val bounds = selectedRectangle.boundsInParent

        return (x >= bounds.minX && x <= bounds.maxX) &&
                (y >= bounds.minY && y <= bounds.maxY)
    }

    // Safe to call even if nothing is selected
    // May be called by the AppCanvas on undo/redo
    fun deselect() {
        if (itemIsSelected) {
            canvasReference.children.remove(selectedRectangle)
            corners.forEach { canvasReference.children.remove(it) }

            // re-enable texteditor nodes
            if (editing) {
                selectedNodes.map {
                    if (it is AppTextEditor) {
                        it.isDisable = true
                    }
                }
                editing = false
            }

            selectedNodes.clear()
            itemIsSelected = false
        }
    }

    private fun deleteSelection() {
        deselect()
        selectedNodes.forEach { canvasReference.children.remove(it) }
    }

    override fun canvasMousePressed(e: MouseEvent) {
        // if click is inside selection then we are now moving the selection
        if (itemIsSelected && isClickInSelectionBox(e.x, e.y)) {
            moving = true
            moveX = e.x
            moveY = e.y
        } else {
            if (itemIsSelected) {
                deselect()
            }
            // make new selection
            onCreateShape(e.x, e.y)
        }
    }

    override fun canvasMouseDragged(e: MouseEvent) {
        if (moving) {
            // translate all selected nodes by difference between
            // last mouse location and current one
            val translateX = e.x - moveX
            val translateY = e.y - moveY

            selectedRectangle.translateX += translateX
            selectedRectangle.translateY += translateY

            corners.map {
                it.translateX += translateX
                it.translateY += translateY
            }

            selectedNodes.map {
                it.translateX += translateX
                it.translateY += translateY
            }

            moveX = e.x
            moveY = e.y
        } else if (!editing) {
            onResizeShape(e.x, e.y)
        }
    }

    override fun canvasMouseReleased(e: MouseEvent) {
        // only create selected box if we're not moving a selection already
        // when not moving, if selecting area is too small,
        // we start editing state
        if (moving) {
            moving = false
        }
        // if editing, when clicking everywhere else, stop editing
        else if (editing) {
            editNode.isDisable = true
            editNode.style = "-fx-background-color: transparent;"
            editing = false
            selectedNodes.clear()
        } else {
            // if not editing determine whether edit or selecting
            if (abs(e.x - startX) < 10 && abs(e.y - startY) < 10) {
                for (node in canvasReference.children) {
                    // only able to edit Text for now
                    if (node != selectionRectangle && node.boundsInParent.intersects(selectionRectangle.boundsInParent) && node is AppTextEditor) {
                        selectedNodes.add(node)
                    }
                }
                // get the topmost text and make if modifiable
                if (selectedNodes.isNotEmpty()) {
                    editNode = selectedNodes.last()
                    editNode.isDisable = false
                    editNode.style = "-fx-background-color: " + selectionLineColor + ";"
                    editing = true
                }
            } else {
                var minX = Double.POSITIVE_INFINITY
                var minY = Double.POSITIVE_INFINITY
                var maxX = 0.0
                var maxY = 0.0

                for (node in canvasReference.children) {
                    if (node != selectionRectangle && node.boundsInParent.intersects(selectionRectangle.boundsInParent)) {
                        selectedNodes.add(node)
                        if (node is AppTextEditor) {
                            node.isDisable = true
                        }

                        val nodeBounds = node.boundsInParent

                        minX = min(minX, nodeBounds.minX)
                        minY = min(minY, nodeBounds.minY)
                        maxX = max(maxX, nodeBounds.maxX)
                        maxY = max(maxY, nodeBounds.maxY)
                    }
                }

                if (selectedNodes.isNotEmpty()) {
                    itemIsSelected = true
                    selectedRectangle = onCreateRectangle(minX - lineWidth / 2, minY - lineWidth / 2, selectedLineColor)
                    onResizeRectangle(selectedRectangle, maxX + lineWidth / 2, maxY + lineWidth / 2)

                    val topLeftCorner =
                        onCreateRectangle(
                            minX - cornerRectangleSize / 2,
                            minY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    onResizeRectangle(topLeftCorner, minX + cornerRectangleSize / 2, minY + cornerRectangleSize / 2)

                    val topRightCorner =
                        onCreateRectangle(
                            maxX - cornerRectangleSize / 2,
                            minY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    onResizeRectangle(topRightCorner, maxX + cornerRectangleSize / 2, minY + cornerRectangleSize / 2)

                    val bottomRightCorner =
                        onCreateRectangle(
                            maxX - cornerRectangleSize / 2,
                            maxY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    onResizeRectangle(bottomRightCorner, maxX + cornerRectangleSize / 2, maxY + cornerRectangleSize / 2)

                    val bottomLeftCorner =
                        onCreateRectangle(
                            minX - cornerRectangleSize / 2,
                            maxY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    onResizeRectangle(bottomLeftCorner, minX + cornerRectangleSize / 2, maxY + cornerRectangleSize / 2)

                    corners = listOf(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner)
                }
            }
        }
        canvasReference.children.remove(selectionRectangle)
        if (selectedNodes.isNotEmpty()) {
            for (node in selectedNodes) {
                when ((node.userData as NodeData).type) {
                    EntityIndex.LINE -> {
                        val line = node as Polyline
                        for (i in line.points.indices) {
                            if (i % 2 == 0) {
                                line.points[i] += line.translateX
                            } else {
                                line.points[i] += line.translateY
                            }
                        }
                        line.translateX = 0.0
                        line.translateY = 0.0
                    }

                    EntityIndex.ELLIPSE -> {
                        val ellipse = node as Ellipse
                        ellipse.centerX += ellipse.translateX
                        ellipse.centerY += ellipse.translateY
                        ellipse.translateX = 0.0
                        ellipse.translateY = 0.0
                    }
                }
            }
            AppData.broadcastModify(selectedNodes)
        }
    }

    override fun onDeselectTool() {
        deselect()
    }
}
