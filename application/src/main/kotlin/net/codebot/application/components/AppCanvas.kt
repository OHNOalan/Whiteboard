package net.codebot.application.components

import net.codebot.application.components.tools.Text
import javafx.event.EventHandler
import javafx.scene.ImageCursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ColorPicker
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Pane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import net.codebot.application.components.tools.BaseTool

class AppCanvas(borderPane: BorderPane) {
    private val canvas = Canvas(800.0, 800.0)
    private val texts: MutableList<Text> = mutableListOf()
    private val tools: MutableList<BaseTool> = mutableListOf()
    private var selectedTool: Int = 0

    init {
        val scrollPane = ScrollPane()
        val pane = Pane()
        val context: GraphicsContext = canvas.graphicsContext2D
        val lineColor = ColorPicker(Color.BLACK)
        context.stroke = lineColor.value
        canvas.onMousePressed = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMousePressed(e, context, pane)
        }
        canvas.onMouseDragged = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseDragged(e, context, pane)
        }
        canvas.onMouseReleased = EventHandler { e: MouseEvent ->
            tools[selectedTool].canvasMouseReleased(e, context, pane)
        }
        canvas.widthProperty().bind(scrollPane.widthProperty())
        canvas.heightProperty().bind(scrollPane.heightProperty())
        pane.children.add(canvas)
        scrollPane.content = pane
        borderPane.center = scrollPane
    }

    fun setTool(toolIndex: Int, cursorImage: Image) {
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
}