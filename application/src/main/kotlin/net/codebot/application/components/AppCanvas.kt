package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import kotlinx.serialization.json.Json
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
    private val drawnItems: MutableMap<String, Node> = mutableMapOf()
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
        this.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (event.code == KeyCode.SPACE) {
                event.consume()
            }
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

    // TODO selection tool needs undo and redo
    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun addDrawnNode(node: Node, broadcast: Boolean = true) {
        addToUndoStack(Pair("Add", node))
        drawnItems[(node.userData as NodeData).id] = node
        this.children.add(node)
        if (broadcast) {
            AppData.broadcastCreate(listOf(node))
        }
    }

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun removeDrawnNode(node: Node, broadcast: Boolean = true) {
        addToUndoStack(Pair("Remove", node))
        drawnItems.remove((node.userData as NodeData).id)
        this.children.remove(node)
        if (broadcast) {
            AppData.broadcastDelete(listOf(node))
        }
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
            pixelReader.getPixels(
                0,
                0,
                writableImage.width.toInt(),
                writableImage.height.toInt(),
                pixelFormat,
                buffer,
                writableImage.width.toInt()
            )

            val bufferedImage =
                BufferedImage(writableImage.width.toInt(), writableImage.height.toInt(), BufferedImage.TYPE_INT_ARGB)
            bufferedImage.setRGB(
                0,
                0,
                writableImage.width.toInt(),
                writableImage.height.toInt(),
                buffer.array(),
                0,
                writableImage.width.toInt()
            )

            ImageIO.write(bufferedImage, "png", File(fileName))
        } catch (ex: IOException) {
            ex.printStackTrace()
            println("Error while trying to export canvas as an image")
        }
    }

    fun saveFile(): String {
        return Json.encodeToString(AppEntities.serializer(), AppData.serialize(drawnItems.values.toList()))
    }

    fun loadFile(data: String) {
        clearCanvas()
        val entities = AppData.deserialize(data)
        for (entity in entities) {
            drawnItems[(entity.userData as NodeData).id] = entity
        }
        this.children.addAll(entities)
        AppData.broadcastCreate(entities)
    }

    fun undo() {
        if (undoStack.size > 0) {
            deselectItemIfSelected()
            val (undoType, node) = undoStack.removeFirst()
            if (undoType == "Add") {
                drawnItems.remove((node.userData as NodeData).id)
                this.children.remove(node)
            } else {
                drawnItems[(node.userData as NodeData).id] = node
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
                drawnItems[(node.userData as NodeData).id] = node
                this.children.add(node)
            } else {
                drawnItems.remove((node.userData as NodeData).id)
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
        this.children.removeAll(drawnItems.values)
        undoStack.clear()
        redoStack.clear()
        deselectItemIfSelected()
        AppData.broadcastDelete(drawnItems.values.toList())
        drawnItems.clear()
    }

    // TODO potential bug where one user deletes one node but that node still exists on the undo/redo stack
    // This function is called whenever the server has a 
    // change that needs to be propagated to the clients.
    fun webUpdateCallback(update: String) {
        val response = Json.decodeFromString(AppResponse.serializer(), update)
        when (response.operation) {
            OperationIndex.ADD -> {
                for (entity in response.entities) {
                    when (entity.type) {
                        EntityIndex.LINE -> {
                            addDrawnNode(
                                AppData.deserializeLine(
                                    Json.decodeFromString(
                                        AppLine.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.RECTANGLE -> {
                            addDrawnNode(
                                AppData.deserializeRectangle(
                                    Json.decodeFromString(
                                        AppRectangle.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.ELLIPSE -> {
                            addDrawnNode(
                                AppData.deserializeEllipse(
                                    Json.decodeFromString(
                                        AppEllipse.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.TEXT -> {
                            addDrawnNode(
                                AppData.deserializeText(
                                    Json.decodeFromString(
                                        AppText.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.SEGMENT -> {
                            addDrawnNode(
                                AppData.deserializeSegment(
                                    Json.decodeFromString(
                                        AppSegment.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }
                    }
                }
            }

            OperationIndex.DELETE -> {
                for (entity in response.entities) {
                    drawnItems[entity.id]?.let { removeDrawnNode(it, false) }
                }
            }

            OperationIndex.MODIFY -> {
                for (entity in response.entities) {
                    drawnItems[entity.id]?.let { removeDrawnNode(it, false) }
                }
                for (entity in response.entities) {
                    when (entity.type) {
                        EntityIndex.LINE -> {
                            addDrawnNode(
                                AppData.deserializeLine(
                                    Json.decodeFromString(
                                        AppLine.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.RECTANGLE -> {
                            addDrawnNode(
                                AppData.deserializeRectangle(
                                    Json.decodeFromString(
                                        AppRectangle.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.ELLIPSE -> {
                            addDrawnNode(
                                AppData.deserializeEllipse(
                                    Json.decodeFromString(
                                        AppEllipse.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.TEXT -> {
                            addDrawnNode(
                                AppData.deserializeText(
                                    Json.decodeFromString(
                                        AppText.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }

                        EntityIndex.SEGMENT -> {
                            addDrawnNode(
                                AppData.deserializeSegment(
                                    Json.decodeFromString(
                                        AppSegment.serializer(),
                                        entity.descriptor
                                    ), entity.id
                                ), false
                            )
                        }
                    }
                }
            }
        }
    }
}
