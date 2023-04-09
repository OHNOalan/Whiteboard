package net.codebot.application.components.tools.styles

import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.AppUtils
import net.codebot.application.components.tools.EraserTool
import kotlin.math.roundToInt

class EraserStyles(styleBar: AppStylebar, eraserTool: EraserTool) :
    BaseStyles(styleBar) {
    init {
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
        slider.value = 30.0
        slider.valueProperty().addListener { _, _, newVal ->
            slider.value = (newVal as Double).roundToInt().toDouble()
            eraserTool.lineWidth = slider.value
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