package net.codebot.application

import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage
import net.codebot.application.components.AppData
import net.codebot.application.components.AppLayout

class Main : Application() {
    override fun start(stage: Stage) {
        stage.isResizable = true
        stage.title = "Whiteboard"
        stage.minHeight = 480.0
        stage.minWidth = 640.0
        stage.maxHeight = 1200.0
        stage.maxWidth = 1600.0
        stage.icons.add(Image("file:src/main/assets/cursors/app.png"));

        val appLayout = AppLayout(stage)
        AppData.registerAppLayout(appLayout)

        stage.show()
    }
}
