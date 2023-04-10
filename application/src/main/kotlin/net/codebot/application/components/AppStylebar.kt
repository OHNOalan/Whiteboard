package net.codebot.application.components

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox

/**
 * Initializes style bar for various tool functionality such as color and stroke width
 */
class AppStylebar : VBox() {
    init {
        this.alignment = Pos.CENTER_LEFT
        this.padding = Insets(50.0, 50.0, 50.0, 50.0)
        this.spacing = 20.0
    }
}