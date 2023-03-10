package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Polyline
import javafx.scene.shape.Rectangle
import net.codebot.application.components.tools.BaseTool
import net.codebot.application.components.tools.SelectionTool
import net.codebot.application.components.tools.ToolIndex
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.IntBuffer
import javax.imageio.ImageIO

class AppCanvas(borderPane: BorderPane) : Pane() {
    private val tools: MutableList<BaseTool> = mutableListOf()
    private val undoStack: ArrayDeque<Pair<String, Node>> = ArrayDeque()
    private val redoStack: ArrayDeque<Pair<String, Node>> = ArrayDeque()
    private val drawnItems: MutableList<Node> = mutableListOf()
    private var selectedTool: Int = 0
    private val scrollPane: ScrollPane = ScrollPane()
    private var backgroundColor = ColorPicker(Color.WHITE)

    init {
        this.prefHeight = 1200.0
        this.prefWidth = 1600.0
        this.background = Background(BackgroundFill(backgroundColor.value, CornerRadii.EMPTY, Insets.EMPTY))
        this.onMousePressed = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMousePressed(e)
        }
        this.onMouseDragged = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseDragged(e)
        }
        this.onMouseReleased = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseReleased(e)
        }
        this.onMouseMoved = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseMoved(e)
        }
        scrollPane.content = this
        borderPane.center = scrollPane
    }

    private fun addToUndoStack(action: Pair<String, Node>) {
        // clear redo stack - overwriting old undone changes
        redoStack.clear()
        undoStack.addFirst(action)
    }

    fun zoomIn() {
        this.scaleX += 0.1
        this.scaleY += 0.1
    }

    fun zoomOut() {
        this.scaleX -= 0.1
        this.scaleY -= 0.1
    }

    fun resetZoom() {
        this.scaleX = 1.0
        this.scaleY = 1.0
    }

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun addDrawnNode(node: Node) {
        addToUndoStack(Pair("Add", node))
        drawnItems.add(node)
        this.children.add(node)
    }

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun removeDrawnNode(node: Node) {
        addToUndoStack(Pair("Remove", node))
        drawnItems.remove(node)
        this.children.remove(node)
    }

    // Upon undo or redo, if an item is selected, it should be deselected
    // This fixes a bug where if an item is selected and its creation is undone,
    // the selection box will remain and can still be interacted with.
    // TODO can be more sophisticated - can add check in SelectionTool if the undo/redo item matches the selected item
    private fun deselectItemIfSelected() {
        val selectionTool = tools[ToolIndex.SELECTION] as SelectionTool
        selectionTool.deselect()
    }

    fun exportCanvas(fileName: String) {
        try {
            val writableImage = WritableImage(this.prefWidth.toInt(), this.prefHeight.toInt())
            this.snapshot(null, writableImage)

            val pixelReader: PixelReader = writableImage.pixelReader
            val buffer: IntBuffer = IntBuffer.allocate(writableImage.width.toInt() * writableImage.height.toInt())
            val pixelFormat: WritablePixelFormat<IntBuffer> = PixelFormat.getIntArgbInstance()
            pixelReader.getPixels(0, 0, writableImage.width.toInt(), writableImage.height.toInt(), pixelFormat, buffer, writableImage.width.toInt())

            val bufferedImage = BufferedImage(writableImage.width.toInt(), writableImage.height.toInt(), BufferedImage.TYPE_INT_ARGB)
            bufferedImage.setRGB(0 ,0, writableImage.width.toInt(), writableImage.height.toInt(), buffer.array(), 0, writableImage.width.toInt())

            ImageIO.write(bufferedImage, "png", File(fileName))
        } catch (ex: IOException) {
            ex.printStackTrace()
            println("Error while trying to export canvas as an image")
        }
    }

    fun saveFile(): String {
        var data = ""
        for (item in drawnItems) {
            var current = ""
            if (item.userData == EntityIndex.LINE) {
                val line = item as Polyline
                current += EntityIndex.LINE + "|"
                current += line.stroke.toString() + "|"
                current += line.strokeWidth.toString() + "|"
                for (point in line.points) {
                    current += "$point|"
                }
            } else if (item.userData == EntityIndex.RECTANGLE) {
                val rectangle = item as Rectangle
                current += EntityIndex.RECTANGLE + "|"
                current += rectangle.translateX.toString() + "|"
                current += rectangle.translateY.toString() + "|"
                current += rectangle.width.toString() + "|"
                current += rectangle.height.toString() + "|"
                current += (if (rectangle.fill != null) rectangle.fill.toString() else "null") + "|"
                current += rectangle.stroke.toString() + "|"
            } else if (item.userData == EntityIndex.ELLIPSE) {
                val ellipse = item as Ellipse
                current += EntityIndex.ELLIPSE + "|"
                current += ellipse.centerX.toString() + "|"
                current += ellipse.centerY.toString() + "|"
                current += ellipse.radiusX.toString() + "|"
                current += ellipse.radiusY.toString() + "|"
                current += (if (ellipse.fill != null) ellipse.fill.toString() else "null") + "|"
                current += ellipse.stroke.toString() + "|"
            }
            data += "$current~"
        }
        return data
    }

    fun loadFile(data: String) {
        clearCanvas()
        for (entity in data.split("~").filter {
            it.isNotEmpty()
        }) {
            val parts = entity.split("|").filter {
                it.isNotEmpty()
            }
            lateinit var node: Node
            if (parts[0] == EntityIndex.LINE) {
                val line = Polyline()
                line.stroke = Paint.valueOf(parts[1])
                line.strokeWidth = parts[2].toDouble()
                for (part in parts.drop(3)) {
                    line.points.add(part.toDouble())
                }
                node = line
            } else if (parts[0] == EntityIndex.RECTANGLE) {
                val rectangle = Rectangle()
                rectangle.translateX = parts[1].toDouble()
                rectangle.translateY = parts[2].toDouble()
                rectangle.width = parts[3].toDouble()
                rectangle.height = parts[4].toDouble()
                rectangle.fill = if (parts[5] == "null") null else Paint.valueOf(parts[5])
                rectangle.stroke = Paint.valueOf(parts[6])
                node = rectangle
            } else if (parts[0] == EntityIndex.ELLIPSE) {
                val ellipse = Ellipse()
                ellipse.centerX = parts[1].toDouble()
                ellipse.centerY = parts[2].toDouble()
                ellipse.radiusX = parts[3].toDouble()
                ellipse.radiusY = parts[4].toDouble()
                ellipse.fill = if (parts[5] == "null") null else Paint.valueOf(parts[5])
                ellipse.stroke = Paint.valueOf(parts[6])
                node = ellipse
            }
            node.userData = parts[0]
            drawnItems.add(node)
            this.children.add(node)
        }
    }

    fun undo() {
        if (undoStack.size > 0) {
            deselectItemIfSelected()
            val (undoType, node) = undoStack.removeFirst()
            if (undoType == "Add") {
                drawnItems.remove(node)
                this.children.remove(node)
            } else {
                drawnItems.add(node)
                this.children.add(node)
            }
            redoStack.addFirst(Pair(undoType, node))
        }
    }

    fun redo() {
        if (redoStack.size > 0) {
            deselectItemIfSelected()
            val (redoType, node) = redoStack.removeFirst()
            if (redoType == "Add") {
                drawnItems.add(node)
                this.children.add(node)
            } else {
                drawnItems.remove(node)
                this.children.remove(node)
            }
            undoStack.addFirst(Pair(redoType, node))
        }
    }

    fun setTool(toolIndex: Int, cursorImage: Image) {
        tools[selectedTool].deselectTool()
        selectedTool = toolIndex
        this.cursor = ImageCursor(
            cursorImage,
            0.0,
            cursorImage.height
        )
    }

    fun registerTool(tool: BaseTool) {
        tool.registerCanvas(this)
        tools.add(tool)
    }

    fun clearCanvas() {
        this.children.removeAll(drawnItems)
        drawnItems.clear()
        undoStack.clear()
        redoStack.clear()
        deselectItemIfSelected()
    }
}
