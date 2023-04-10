package net.codebot.application.components.tools.styles

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.AppUtils
import net.codebot.application.components.tools.ShapeTool

/**
 * Creates the styles for the shape tool.
 * @param styleBar The style bar to add the styles to.
 * @param shapeTool A reference to the shape tool.
 */
class ShapeStyles(styleBar: AppStylebar, shapeTool: ShapeTool) : BaseStyles(styleBar) {
    init {
        val colorPickerContainer = HBox()
        val colorPicker = ColorPicker()
        val colorPickerLabel = Label("Color:")
        colorPicker.value = Color.valueOf("black")
        colorPicker.setOnAction {
            shapeTool.lineColor = colorPicker.value
        }
        colorPickerLabel.font = Font(18.0)
        colorPickerContainer.children.addAll(
            AppUtils.createHSpacer(),
            colorPickerLabel,
            AppUtils.createHSpacer(),
            colorPicker,
            AppUtils.createHSpacer()
        )
        controls.add(colorPickerContainer)

        val fillContainer = HBox()
        val fillCheckbox = CheckBox("Fill Shape")
        fillCheckbox.setOnAction {
            shapeTool.fillShape = fillCheckbox.isSelected
        }
        fillContainer.children.addAll(
            AppUtils.createHSpacer(),
            fillCheckbox,
            AppUtils.createHSpacer(),
        )
        controls.add(fillContainer)

        val buttonContainer = HBox()
        val rectangleButton = Button("Rectangle")
        rectangleButton.onMouseClicked = EventHandler {
            shapeTool.onCreateShape = { x, y -> shapeTool.onCreateRectangle(x, y) }
            shapeTool.onMoveShape = { x, y -> shapeTool.onMoveRectangle(x, y) }
            shapeTool.onReleaseShape = { shapeTool.onReleaseRectangle() }
        }
        val squareButton = Button("Square")
        squareButton.onMouseClicked = EventHandler {
            shapeTool.onCreateShape = { x, y -> shapeTool.onCreateRectangle(x, y) }
            shapeTool.onMoveShape = { x, y -> shapeTool.onMoveSquare(x, y) }
            shapeTool.onReleaseShape = { shapeTool.onReleaseRectangle() }
        }
        val ellipseButton = Button("Ellipse")
        ellipseButton.onMouseClicked = EventHandler {
            shapeTool.onCreateShape = { x, y -> shapeTool.onCreateEllipse(x, y) }
            shapeTool.onMoveShape = { x, y -> shapeTool.onMoveEllipse(x, y) }
            shapeTool.onReleaseShape = { shapeTool.onReleaseEllipse() }
        }
        val circleButton = Button("Circle")
        circleButton.onMouseClicked = EventHandler {
            shapeTool.onCreateShape = { x, y -> shapeTool.onCreateEllipse(x, y) }
            shapeTool.onMoveShape = { x, y -> shapeTool.onMoveCircle(x, y) }
            shapeTool.onReleaseShape = { shapeTool.onReleaseEllipse() }
        }

        buttonContainer.children.addAll(
            AppUtils.createHSpacer(),
            rectangleButton,
            AppUtils.createHSpacer(),
            squareButton,
            AppUtils.createHSpacer(),
            ellipseButton,
            AppUtils.createHSpacer(),
            circleButton,
            AppUtils.createHSpacer()
        )
        controls.add(buttonContainer)
    }
}