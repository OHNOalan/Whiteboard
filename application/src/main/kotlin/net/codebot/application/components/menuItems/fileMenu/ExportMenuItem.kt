package net.codebot.application.components.menuItems.fileMenu

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem
import java.io.File

class ExportMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(menu, "Export to Image", KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)) {

    private var canvasReference: AppCanvas = canvas
    private var exportPath: String? = null

    override fun onItemClicked(e: ActionEvent) {
        if (exportPath == null) {
            val fileChooser = FileChooser()
            fileChooser.title = "Save As"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Image", "*.png"))
            val selectedFile = fileChooser.showSaveDialog(null)
            if (selectedFile != null) {
                exportPath = selectedFile.path
                canvasReference.exportCanvas(exportPath.toString())
            }
        } else {
            canvasReference.exportCanvas(exportPath.toString())
        }
    }
}