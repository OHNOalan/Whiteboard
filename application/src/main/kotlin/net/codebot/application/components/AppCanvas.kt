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
import net.codebot.application.components.tools.SelectTool
import net.codebot.application.components.tools.ToolIndex
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.IntBuffer
import javax.imageio.ImageIO

/**
 * Contains the main logic for all the interactive items on the whiteboard, including
 * processing/sending server updates, saving/loading, changing tools, and undo/redo.
 *
 * @param borderPane The main pane where everything is attached to.
 */
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

    /**
     * Increases the zoom level of the whiteboard.
     */
    fun zoomIn() {
        this.scaleX += 0.1
        this.scaleY += 0.1
    }

    /**
     * Decreases the zoom level of the whiteboard.
     */
    fun zoomOut() {
        this.scaleX -= 0.1
        this.scaleY -= 0.1
    }

    /**
     * Resets the zoom level of the whiteboard to 1.0.
     */
    fun resetZoom() {
        this.scaleX = 1.0
        this.scaleY = 1.0
    }

    /**
     * Adds a node (drawn item) to the whiteboard, regardless of its origin.
     * If the node was created locally, it will be broadcast to the server.
     *
     * This function does not handle UI items like the pointer or preview elements.
     *
     * @param node The node to display.
     * @param broadcast Whether this node was created locally.
     */
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

    /**
     * Removes a node (drawn item) from the whiteboard, regardless of its origin.
     * If the node was removed locally, it will be broadcast to the server.
     *
     * This function does not handle UI items like the pointer or preview elements.
     *
     * @param node The node to remove.
     * @param broadcast Whether this node was removed locally.
     */
    fun removeDrawnNode(node: Node, broadcast: Boolean = true) {
        drawnItems.remove((node.userData as NodeData).id)
        this.children.remove(node)
        if (broadcast) {
            AppData.broadcastDelete(listOf(node))
        }
    }

    /**
     * Deselects the currently selected item(s), if they are selected. Used for
     * undo/redo and clearing the canvas.
     */
    private fun deselectItemIfSelected() {
        val selectionTool = tools[ToolIndex.SELECT] as SelectTool
        selectionTool.deselect()
    }

    /**
     * Exports the entire whiteboard as an image to be saved locally. All drawn items
     * are saved.
     *
     * The image file is not loadable as a whiteboard after saving.
     *
     * @param fileName The name of the saved image.
     * @see saveFile
     */
    fun exportCanvas(fileName: String) {
        try {
            val writableImage =
                WritableImage(this.prefWidth.toInt(), this.prefHeight.toInt())
            this.snapshot(null, writableImage)

            val pixelReader: PixelReader = writableImage.pixelReader
            val buffer: IntBuffer =
                IntBuffer.allocate(
                    writableImage.width.toInt() * writableImage.height.toInt()
                )
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

    /**
     * Saves the state of the whiteboard to a JSON string. Meant for loading in
     * later.
     *
     * This does not create a user viewable image.
     *
     * @see exportCanvas
     * @see loadFile
     * @return String representing all the data in the whiteboard.
     */
    fun saveFile(): String {
        return AppData.serializeLocal(drawnItems.values.toList())
    }

    /**
     * Loads and parses JSON string data into items on the whiteboard, and displays all
     * the items.
     *
     * The load item is broadcast and all previous items on the whiteboard are
     * overwritten for every user.
     *
     * @see saveFile
     * @param data A JSON string containing whiteboard node data.
     */
    fun loadFile(data: String) {
        clearCanvas()
        val entities = AppData.deserializeLocal(data)
        for (entity in entities) {
            drawnItems[(entity.userData as NodeData).id] = entity
        }
        this.children.addAll(entities)
        AppData.broadcastAdd(entities)
    }

    /**
     * Broadcasts an undo action to the server. The latest action performed by any user
     * is undone.
     */
    fun undo() {
        deselectItemIfSelected()
        AppData.broadcastUndo()
    }

    /**
     * Broadcasts a redo action to the server. The latest undo action performed by any
     * user is redone.
     */
    fun redo() {
        deselectItemIfSelected()
        AppData.broadcastRedo()
    }

    /**
     * Changes the currently selected tool.
     * @param toolIndex The index of the selected tool in the list of tools.
     * @param cursorImage The image to change the cursor to.
     */
    fun setTool(toolIndex: Int, cursorImage: Image) {
        tools[selectedTool].deselectTool()
        selectedTool = toolIndex
        this.cursor = ImageCursor(
            cursorImage,
            0.0,
            cursorImage.height
        )
    }

    /**
     * Adds a tool to the toolbar. Used when initially creating the tools on startup.
     * @param tool The tool to be added.
     */
    fun registerTool(tool: BaseTool) {
        tool.registerCanvas(this)
        tools.add(tool)
    }

    /**
     * Clears the entire canvas. Deletes every item and broadcasts all the deletes to
     * the server, so every user's canvas is also cleared.
     *
     * @param broadcast Whether this node was created locally.
     */
    fun clearCanvas(broadcast: Boolean = true) {
        if (drawnItems.values.isNotEmpty()) {
            this.children.removeAll(drawnItems.values.toSet())
            deselectItemIfSelected()
            if (broadcast) {
                AppData.broadcastDelete(drawnItems.values.toList())
            }
            drawnItems.clear()
        }
    }

    /**
     * After receiving an update message from the server, it is processed here and the
     * appropriate action is applied (adding, deleting, or modifying a node).
     *
     * @param updateMessage The entity/entities that are being changed.
     * @param operation Whether the operation is an add, delete, or modify.
     * @param usePreviousDescriptor Only used for a modify action. Used for undo/redo
     * to track the previous state of the item.
     */
    private fun applyUpdateMessage(
        updateMessage: AppEntitiesSchema,
        operation: Int,
        usePreviousDescriptor: Boolean = false
    ) {
        when (operation) {
            OperationIndex.ADD -> {
                for (node in updateMessage.entities.map {
                    AppData.deserializeSingle(
                        it,
                        it.id,
                        it.timestamp,
                        null,
                        it.descriptor
                    )
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
                deselectItemIfSelected()
                for (entity in updateMessage.entities) {
                    var descriptor = entity.descriptor
                    if (usePreviousDescriptor && entity.previousDescriptor != null) {
                        descriptor = entity.previousDescriptor!!
                    }
                    drawnItems[entity.id]?.let {
                        AppData.deserializeSingle(
                            entity,
                            entity.id,
                            entity.timestamp,
                            it,
                            descriptor
                        )
                    }
                }
            }
        }
    }

    /**
     * Called whenever the server has a change that needs to be propagated to the
     * users. Parses the update string and calls `applyUpdateMessage` as needed.
     * @param update JSON string of all the changes to occur.
     * @see applyUpdateMessage
     */
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
                    applyUpdateMessage(updateMessage, operation, true)
                }

                UndoIndex.REDO -> {
                    applyUpdateMessage(updateMessage, updateMessage.operation)
                }
            }
        }
    }
}
