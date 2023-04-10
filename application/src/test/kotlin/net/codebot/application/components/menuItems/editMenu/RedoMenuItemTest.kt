package net.codebot.application.components.menuItems.editMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to redo an action. Shortcut: CTRL+Y
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class RedoMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Redo",
        KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    /**
     * Redo the latest undone action when the menu item is clicked.
     */
    override fun onItemClicked() {
        canvasReference.redo()
    }
}
