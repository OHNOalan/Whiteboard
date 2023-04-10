package net.codebot.application.components.menuItems.editMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to clear the canvas. Shortcut: CTRL+BACKSPACE
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class ClearMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Clear",
        KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    /**
     * Clears the canvas when the menu item is clicked.
     */
    override fun onItemClicked() {
        canvasReference.clearCanvas()
    }
}
