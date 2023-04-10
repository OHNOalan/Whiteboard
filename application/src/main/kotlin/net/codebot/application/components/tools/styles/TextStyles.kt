package net.codebot.application.components.tools.styles

import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.AppUtils

/**
 * Creates the styles for the text tool.
 *
 * Note that the text tool has no "styles" per se, since all the formatting is attached
 * to the text box itself. There is only a label as a descriptor.
 *
 * @param styleBar The style bar to add the styles to.
 */
class TextStyles(styleBar: AppStylebar) : BaseStyles(styleBar) {
    init {
        val container = HBox()
        val label = Label("Click and drag to create a text box.")
        label.font = Font(14.0)
        container.children.addAll(
            AppUtils.createHSpacer(),
            label,
            AppUtils.createHSpacer()
        )
        controls.add(container)
    }
}