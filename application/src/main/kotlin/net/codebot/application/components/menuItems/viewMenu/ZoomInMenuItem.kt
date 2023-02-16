package net.codebot.application.components.menuItems.viewMenu

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.codebot.application.components.AppCanvas
import net.codebot.application.components.menuItems.BaseMenuItem

class ZoomInMenuItem(menu: Menu, canvas: AppCanvas) :
    BaseMenuItem(menu, "Zoom In", KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN)) {

    private var canvasReference: AppCanvas = canvas
    
    override fun onItemClicked(e: ActionEvent) {
        // TODO implement this
    }
}
