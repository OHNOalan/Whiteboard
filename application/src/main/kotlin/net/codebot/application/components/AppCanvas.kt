package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.image.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
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
    private val drawnItems: MutableMap<String, Node> = mutableMapOf()
    private var selectedTool: Int = 0
    private val scrollPane: ScrollPane = ScrollPane()
    private var backgroundColor = ColorPicker(Color.WHITE)

    init {
        this.prefHeight = 1200.0
        this.prefWidth = 1600.0
        this.background = Background(
            BackgroundFill(
                backgroundColor.value,
                CornerRadii.EMPTY,
                Insets.EMPTY
            )
        )
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
        val nodeData = node.userData as NodeData
        drawnItems[nodeData.id] = node
        var insertionPoint = children.size
        for (i in children.indices) {
            val child = children[i]
            if (child.userData is NodeData) {
                if ((child.userData as NodeData).timestamp > nodeData.timestamp) {
                    insertionPoint = i
                    break
                }
            }
        }
        this.children.add(insertionPoint, node)
        if (broadcast) {
            AppData.broadcastAdd(listOf(node))
        }
    }

    // Use this function only to add user drawn entities
    // Do not use this for things like pointer or preview elements
    fun removeDrawnNode(node: Node, broadcast: Boolean = true) {
        drawnItems.remove((node.userData as NodeData).id)
        this.children.remove(node)
        if (broadcast) {
            AppData.broadcastDelete(listOf(node))
        }
    }

    // Deselect items in undo redo or when delete is synchronized
    private fun deselectItemIfSelected() {
        val selectionTool = tools[ToolIndex.SELECTION] as SelectionTool
        selectionTool.deselect()
    }

    fun exportCanvas(fileName: String) {
        try {
            val writableImage =
                WritableImage(this.prefWidth.toInt(), this.prefHeight.toInt())
            this.snapshot(null, writableImage)

            val pixelReader: PixelReader = writableImage.pixelReader
            val buffer: IntBuffer =
                IntBuffer.allocate(writableImage.width.toInt() * writableImage.height.toInt())
            val pixelFormat: WritablePixelFormat<IntBuffer> =
                PixelFormat.getIntArgbInstance()
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
                BufferedImage(
                    writableImage.width.toInt(),
                    writableImage.height.toInt(),
                    BufferedImage.TYPE_INT_ARGB
                )
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
        return AppData.serializeLocal(drawnItems.values.toList())
    }

    fun loadFile(data: String) {
        clearCanvas()
        val entities = AppData.deserializeLocal(data)
        for (entity in entities) {
            drawnItems[(entity.userData as NodeData).id] = entity
        }
        this.children.addAll(entities)
        AppData.broadcastAdd(entities)
    }

    fun undo() {
        deselectItemIfSelected()
        AppData.broadcastUndo()
    }

    fun redo() {
        deselectItemIfSelected()
        AppData.broadcastRedo()
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
        if (drawnItems.values.isNotEmpty()) {
            this.children.removeAll(drawnItems.values)
            deselectItemIfSelected()
            AppData.broadcastDelete(drawnItems.values.toList())
            drawnItems.clear()
        }
    }

    private fun applyUpdateMessage(updateMessage: AppEntitiesSchema, operation: Int) {
        when (operation) {
            OperationIndex.ADD -> {
                for (node in updateMessage.entities.map {
                    AppData.deserializeSingle(it, it.id, it.timestamp, null)
                }) {
                    addDrawnNode(node, false)
                }
            }

            OperationIndex.DELETE -> {
                deselectItemIfSelected()
                for (entity in updateMessage.entities) {
                    drawnItems[entity.id]?.let { removeDrawnNode(it, false) }
                }
            }

            OperationIndex.MODIFY -> {
                // TODO check what happens if move and select
                for (entity in updateMessage.entities) {
                    drawnItems[entity.id]?.let {
                        AppData.deserializeSingle(
                            entity,
                            entity.id,
                            entity.timestamp,
                            it,
                        )
                    }
                }
            }
        }
    }

    // This function is called whenever the server has a 
    // change that needs to be propagated to the clients.
    fun webUpdateCallback(update: String) {
        val updateMessage =
            Json.decodeFromString(AppEntitiesSchema.serializer(), update)
        if (updateMessage.undoState == UndoIndex.NONE) {
            applyUpdateMessage(updateMessage, updateMessage.operation)
        } else {
            when (updateMessage.undoState) {
                UndoIndex.UNDO -> {
                    var operation = updateMessage.operation
                    if (updateMessage.operation == OperationIndex.ADD) {
                        operation = OperationIndex.DELETE
                    } else if (updateMessage.operation == OperationIndex.DELETE) {
                        operation = OperationIndex.ADD
                    }
                    applyUpdateMessage(updateMessage, operation)
                }

                UndoIndex.REDO -> {
                    applyUpdateMessage(updateMessage, updateMessage.operation)
                }
            }
        }
    }
}
