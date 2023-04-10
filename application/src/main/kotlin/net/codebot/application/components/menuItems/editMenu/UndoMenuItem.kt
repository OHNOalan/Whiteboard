package net.codebot.application.components.menuItems.editMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to undo an action. Shortcut: CTRL+Z
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class UndoMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Undo",
        KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    /**
     * Undo the latest action when the menu item is clicked.
     */
    override fun onItemClicked() {
        canvasReference.undo()
    }
}
