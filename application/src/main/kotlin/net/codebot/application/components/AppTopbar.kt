package net.codebot.application.components

import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

/**
 * Initializes top bar for the application as a vertical box
 *
 * @param borderPane The main pane where everything is attached to.
 * @param appCanvas The main canvas of the whiteboard.
 */
class AppTopbar(borderPane: BorderPane, appCanvas: AppCanvas) {
    init {
        val topBar = VBox()
        AppMenubar(topBar, appCanvas)
        borderPane.top = topBar
    }
}