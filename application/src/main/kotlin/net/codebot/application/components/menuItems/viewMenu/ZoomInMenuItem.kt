package net.codebot.application.components.menuItems.viewMenu

import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

/**
 * Creates the menu item to zoom in on the canvas. Shortcut: CTRL+EQUALS
 * @param menu The menu to add this item to.
 * @param canvas The main canvas of the whiteboard.
 */
class ZoomInMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(
        menu,
        "Zoom In",
        KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN)
    ) {

    private var canvasReference: AppCanvas = canvas

    /**
     * Zooms in on the canvas when the menu item is clicked.
     */
    override fun onItemClicked() {
        canvasReference.zoomIn()
    }
}
