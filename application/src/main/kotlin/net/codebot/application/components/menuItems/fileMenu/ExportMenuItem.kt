package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to export the canvas as an image. Shortcut: CTRL+E
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class ExportMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Export to Image",
        KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas
    private var exportPath: String? = null

    /**
     * Export the entire whiteboard as an image when the menu item is clicked.
     */
    override fun onItemClicked() {
        if (exportPath == null) {
            val fileChooser = FileChooser()
            fileChooser.title = "Export As"
            fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter(
                    "PNG",
                    "*.png"
                )
            )
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
