package net.codebot.application.components

import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

class AppTopbar(borderPane: BorderPane, appCanvas: AppCanvas) {
    init {
        val topBar = VBox()
        AppMenubar(topBar, appCanvas)
        borderPane.top = topBar
    }
}