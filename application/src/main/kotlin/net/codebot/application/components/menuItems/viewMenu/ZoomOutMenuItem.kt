package net.codebot.application.components.menuItems.viewMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to zoom out of the canvas. Shortcut: CTRL+MINUS
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class ZoomOutMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Zoom Out",
        KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    /**
     * Zooms out of the canvas when the menu item is clicked.
     */
    override fun onItemClicked() {
        canvasReference.zoomOut()
    }
}
