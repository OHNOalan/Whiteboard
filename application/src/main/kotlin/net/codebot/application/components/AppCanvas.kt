package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.ImageCursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import net.codebot.application.components.tools.BaseTool
import net.codebot.application.components.tools.Text

class AppCanvas(borderPane: BorderPane): Canvas(800.0, 800.0) {
    private val texts: MutableList<Text> = mutableListOf()
    private val tools: MutableList<BaseTool> = mutableListOf()
    private var selectedTool: Int = 0
    private val scrollPane: ScrollPane = ScrollPane()
    val pane: Pane = Pane()

    var backgroundColour = ColorPicker(Color.WHITE)
    val context: GraphicsContext = this.graphicsContext2D

    init {
        pane.background = Background(BackgroundFill(backgroundColour.value, CornerRadii.EMPTY, Insets.EMPTY))

        this.onMousePressed = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMousePressed(e, context, pane)
        }
        this.onMouseDragged = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseDragged(e, context, pane)
        }
        this.onMouseReleased = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseReleased(e, context, pane)
        }
        this.onMouseMoved = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseMoved(e, context, pane)
        }
        pane.children.add(this)
        scrollPane.content = pane
        borderPane.center = scrollPane
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
        context.clearRect(0.0, 0.0, this.width, this.height)
    }
}
