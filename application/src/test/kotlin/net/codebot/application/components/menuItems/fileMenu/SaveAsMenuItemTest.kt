package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to save the canvas as a whiteboard file with a new name.
 * Shortcut: CTRL+A
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 * @param saveMenuItem The menu item containing the main saving logic.
 */
class SaveAsMenuItem(menu: Menu, canvas: AppCanvas, saveMenuItem: SaveMenuItem) :
    BaseMenuItem(
        menu,
        "Save As",
        KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas
    private var saveMenuItemReference: SaveMenuItem = saveMenuItem

    /**
     * Opens the dialogue to select a file name to save to when the menu item is
     * clicked.
     */
    override fun onItemClicked() {
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
            saveMenuItemReference.savePath = selectedFile.path
        }
    }
}
