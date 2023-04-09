package net.codebot.application.components

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox

class AppStylebar : VBox() {
    init {
        this.alignment = Pos.CENTER_LEFT
        this.padding = Insets(50.0, 50.0, 50.0, 50.0)
        this.spacing = 20.0
    }
}