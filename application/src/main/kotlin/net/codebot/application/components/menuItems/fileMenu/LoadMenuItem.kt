package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem


class LoadMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Load",
        KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    override fun onItemClicked() {
        val fileChooser = FileChooser()
        fileChooser.title = "Load"
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter(
                "Whiteboard",
                "*.wb"
            )
        )
        val selectedFile = fileChooser.showOpenDialog(null)
        selectedFile?.bufferedReader()?.use { out ->
            canvasReference.loadFile(out.readText())
        }
    }
}
