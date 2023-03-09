package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import net.codebot.application.components.tools.BaseTool

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

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun addDrawnNode(node: Node) {
        undoStack.addFirst(Pair("Add", node))
        drawnItems.add(node)
        this.children.add(node)
    }

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun removeDrawnNode(node: Node) {
        undoStack.addFirst(Pair("Remove", node))
        drawnItems.remove(node)
        this.children.remove(node)
    }

    fun undo() {
        if (undoStack.size > 0) {
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
    }
}
