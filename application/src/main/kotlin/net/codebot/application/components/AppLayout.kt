package net.codebot.application.components

import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class AppLayout(stage: Stage) {
    val appCanvas: AppCanvas

    init {
        val borderPane = BorderPane()
        val scene = Scene(borderPane, 1100.0, 800.0)
        this.appCanvas = AppCanvas(borderPane)
        AppSidebar(borderPane, appCanvas)
        AppTopbar(borderPane, appCanvas)
        stage.scene = scene
    }
}
