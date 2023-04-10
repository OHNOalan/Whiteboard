package net.codebot.application.components.tools

import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Line
import javafx.scene.shape.Polyline
import javafx.scene.shape.Rectangle
import net.codebot.application.components.*
import net.codebot.application.components.tools.styles.SelectStyles
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The set of properties for the selection tool.
 * @param container The container to add the tool to.
 * @param styleBar The style bar for this tool where all customization options are
 * displayed.
 */
class SelectTool(container: HBox, styleBar: AppStylebar) : BaseTool(
    container,
    "file:src/main/assets/cursors/selection.png",
    "file:src/main/assets/cursors/selection.png",
    "Select",
    ToolIndex.SELECT,
) {
    override val stylesControl = SelectStyles(styleBar)

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
    private val selectedNodePreviousSchemas = mutableMapOf<Node, AppEntitySchema>()

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
        textControlContainer.style =
            ("-fx-padding: 10;" + "-fx-border-style: solid inside;"
                    + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                    + "-fx-border-radius: 5;" + "-fx-border-color: blue;")
    }

    val onCreateShape: (Double, Double) -> Unit =
        { x, y ->
            selectionRectangle =
                createBoundingRectangle(x, y, selectionLineColor, strokeWidth = 1.0)
        }

    val onResizeShape: (Double, Double) -> Unit =
        { x, y -> resizeBoundingRectangle(selectionRectangle, x, y) }

    /**
     * Creates a bounding rectangle to use while the mouse is being dragged.
     * Shows the area that objects are being selected in.
     * @return A rectangle object to display while the mouse is being dragged.
     */
    private fun createBoundingRectangle(
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

    /**
     * Changes the size of the bounding rectangle while the mouse is being dragged.
     * @see createBoundingRectangle
     */
    private fun resizeBoundingRectangle(rectangle: Rectangle, x: Double, y: Double) {
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

    /**
     * Detects if the mouse click is within the area that is selected.
     */
    private fun isClickInSelectionBox(x: Double, y: Double): Boolean {
        val bounds = selectedRectangle.boundsInParent

        return (x >= bounds.minX && x <= bounds.maxX) &&
                (y >= bounds.minY && y <= bounds.maxY)
    }

    /**
     * Deselects any selected items.
     *
     * This function may be used in case other actions are occurring outside the
     * selection tool that might cause the selected items to change, such as undo/redo
     * and clearing the canvas in the `AppCanvas` class.
     *
     * This function is safe to call even if nothing is selected.
     */
    fun deselect() {
        if (itemIsSelected) {
            canvasReference.children.remove(selectedRectangle)
            corners.forEach { canvasReference.children.remove(it) }

            // re-enable text editor nodes
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
                    if (node != selectionRectangle && node.boundsInParent.intersects(
                            selectionRectangle.boundsInParent
                        ) && node is AppTextEditor
                    ) {
                        selectedNodes.add(node)
                    }
                }
                // get the topmost text and make if modifiable
                if (selectedNodes.isNotEmpty()) {
                    editNode = selectedNodes.last()
                    editNode.isDisable = false
                    editNode.style =
                        "-fx-background-color: " + selectionLineColor.toString()
                            .replace("0x", "#") + ";"
                    editing = true
                }
            } else {
                var minX = Double.POSITIVE_INFINITY
                var minY = Double.POSITIVE_INFINITY
                var maxX = 0.0
                var maxY = 0.0

                for (node in canvasReference.children) {
                    if (node != selectionRectangle && node.boundsInParent.intersects(
                            selectionRectangle.boundsInParent
                        )
                    ) {
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
                    selectedNodePreviousSchemas.clear()
                    for (node in selectedNodes) {
                        selectedNodePreviousSchemas[node] =
                            AppData.nodeToAppEntitySchema(node)
                    }

                    itemIsSelected = true
                    selectedRectangle = createBoundingRectangle(
                        minX - lineWidth / 2,
                        minY - lineWidth / 2,
                        selectedLineColor
                    )
                    resizeBoundingRectangle(
                        selectedRectangle,
                        maxX + lineWidth / 2,
                        maxY + lineWidth / 2
                    )

                    val topLeftCorner =
                        createBoundingRectangle(
                            minX - cornerRectangleSize / 2,
                            minY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    resizeBoundingRectangle(
                        topLeftCorner,
                        minX + cornerRectangleSize / 2,
                        minY + cornerRectangleSize / 2
                    )

                    val topRightCorner =
                        createBoundingRectangle(
                            maxX - cornerRectangleSize / 2,
                            minY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    resizeBoundingRectangle(
                        topRightCorner,
                        maxX + cornerRectangleSize / 2,
                        minY + cornerRectangleSize / 2
                    )

                    val bottomRightCorner =
                        createBoundingRectangle(
                            maxX - cornerRectangleSize / 2,
                            maxY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    resizeBoundingRectangle(
                        bottomRightCorner,
                        maxX + cornerRectangleSize / 2,
                        maxY + cornerRectangleSize / 2
                    )

                    val bottomLeftCorner =
                        createBoundingRectangle(
                            minX - cornerRectangleSize / 2,
                            maxY - cornerRectangleSize / 2,
                            cornerLineColor,
                            fill = cornerLineFill
                        )
                    resizeBoundingRectangle(
                        bottomLeftCorner,
                        minX + cornerRectangleSize / 2,
                        maxY + cornerRectangleSize / 2
                    )

                    corners = listOf(
                        topLeftCorner,
                        topRightCorner,
                        bottomRightCorner,
                        bottomLeftCorner
                    )
                }
            }
        }
        canvasReference.children.remove(selectionRectangle)
        if (selectedNodes.isNotEmpty()) {
            val modifiedMessages = mutableListOf<AppEntitySchema>()
            for (node in selectedNodes) {
                if (node.translateX == 0.0 && node.translateY == 0.0) {
                    continue
                }
                if (node is AppTextEditor) {
                    if (node.previousTranslateX == node.translateX &&
                        node.previousTranslateY == node.translateY) {
                        continue
                    }
                    node.previousTranslateX = node.translateX
                    node.previousTranslateY = node.translateY
                }
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

                    EntityIndex.SEGMENT -> {
                        val segment = node as Line
                        segment.startX += segment.translateX
                        segment.startY += segment.translateY
                        segment.endX += segment.translateX
                        segment.endY += segment.translateY
                        segment.translateX = 0.0
                        segment.translateY = 0.0
                    }
                }
                val nodePreviousSchema = selectedNodePreviousSchemas[node]
                if (nodePreviousSchema != null) {
                    nodePreviousSchema.previousDescriptor =
                        nodePreviousSchema.descriptor
                    nodePreviousSchema.descriptor = AppData.serializeSingle(node)
                    if (nodePreviousSchema.descriptor != nodePreviousSchema.previousDescriptor) {
                        modifiedMessages.add(nodePreviousSchema)
                    }
                } else {
                    println("Cannot determine previous schema.")
                }
            }
            if (modifiedMessages.size > 0) {
                AppData.broadcastModify(modifiedMessages)
            }
        }
    }

    override fun onDeselectTool() {
        deselect()
    }
}
