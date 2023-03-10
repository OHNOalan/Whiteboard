package net.codebot.application.components.menuItems.fileMenu

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem
import java.nio.charset.StandardCharsets
import java.nio.file.Files


class LoadMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(menu, "Load", KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN)) {

    private var canvasReference: AppCanvas = canvas
    
    override fun onItemClicked(e: ActionEvent) {
        val fileChooser = FileChooser()
        fileChooser.title = "Load"
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Whiteboard", "*.wb"))
        val selectedFile = fileChooser.showOpenDialog(null)
        selectedFile?.bufferedReader()?.use { out ->
            canvasReference.loadFile(out.readText())
        }
    }
}
