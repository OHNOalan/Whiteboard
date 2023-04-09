package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem
import java.io.File


class SaveMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Save",
        KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas
    var savePath: String? = null

    override fun onItemClicked() {
        if (savePath == null) {
            val fileChooser = FileChooser()
            fileChooser.title = "Save As"
            fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter(
                    "Whiteboard",
                    "*.wb"
                )
            )
            val selectedFile = fileChooser.showSaveDialog(null)
            if (selectedFile != null) {
                selectedFile.bufferedWriter().use { out ->
                    out.write(canvasReference.saveFile())
                }
                savePath = selectedFile.path
            }
        } else {
            File(savePath).bufferedWriter().use { out ->
                out.write(canvasReference.saveFile())
            }
        }
    }
}
