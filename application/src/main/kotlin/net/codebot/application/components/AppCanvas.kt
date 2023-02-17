package net.codebot.application.components

import net.codebot.application.components.tools.Text
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import net.codebot.application.components.tools.BaseTool

class AppCanvas(borderPane: BorderPane) {
    private var width = 800.0
    private var height = 800.0

    private val canvas = Canvas(width, height)
    private val texts: MutableList<Text> = mutableListOf()
    private val tools: MutableList<BaseTool> = mutableListOf()
    private var selectedTool: Int = 0
    private val scrollPane: ScrollPane = ScrollPane()
    val pane: Pane = Pane()

    var backgroundColour = ColorPicker(Color.WHITE)
    val context: GraphicsContext = canvas.graphicsContext2D

    init {
        pane.background = Background(BackgroundFill(backgroundColour.value, CornerRadii.EMPTY, Insets.EMPTY))

        canvas.onMousePressed = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMousePressed(e, context, pane)
        }
        canvas.onMouseDragged = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseDragged(e, context, pane)
        }
        canvas.onMouseReleased = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseReleased(e, context, pane)
        }
        canvas.onMouseMoved = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseMoved(e, context, pane)
        }
        canvas.widthProperty().bind(scrollPane.widthProperty())
        canvas.heightProperty().bind(scrollPane.heightProperty())
        pane.children.add(canvas)
        scrollPane.content = pane
        borderPane.center = scrollPane
    }

    fun setTool(toolIndex: Int, cursorImage: Image) {
        tools[selectedTool].deselectTool()
        selectedTool = toolIndex
        canvas.cursor = ImageCursor(
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
        context.clearRect(0.0, 0.0, canvas.width, canvas.height)
    }
}