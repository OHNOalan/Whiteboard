package net.codebot.application.components.tools.styles

import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.AppUtils
import net.codebot.application.components.tools.PenTool
import kotlin.math.roundToInt


class PenStyles(styleBar: AppStylebar, penTool: PenTool) : BaseStyles(styleBar) {
    init {
        val colorPickerContainer = HBox()
        val colorPicker = ColorPicker()
        val colorPickerLabel = Label("Color:")
        colorPicker.value = Color.valueOf("black")
        colorPicker.setOnAction {
            penTool.lineColor = colorPicker.value
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

        val sliderContainer = HBox()
        val slider = Slider(0.0, 1.0, 0.5)
        val sliderLabel = Label("Thickness:")
        sliderLabel.font = Font(18.0)
        slider.isShowTickMarks = true
        slider.isShowTickLabels = true
        slider.min = 1.0
        slider.max = 41.0
        slider.majorTickUnit = 5.0
        slider.blockIncrement = 1.0
        slider.valueProperty().addListener { _, _, newVal ->
            slider.value = (newVal as Double).roundToInt().toDouble()
            penTool.lineWidth = slider.value
        }
        sliderContainer.children.addAll(
            AppUtils.createHSpacer(),
            sliderLabel,
            AppUtils.createHSpacer(),
            slider,
            AppUtils.createHSpacer()
        )
        controls.add(sliderContainer)
    }
}