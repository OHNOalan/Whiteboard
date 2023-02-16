package net.codebot.application.components.menuItems

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCodeCombination

abstract class BaseMenuItem(menu: Menu, itemName: String, hotkey: KeyCodeCombination? = null) {
    init {
        val menuItem = MenuItem(itemName)
        menu.items.add(menuItem)

        menuItem.setOnAction { e: ActionEvent ->
            onItemClicked(e)
        }

        hotkey?.let { menuItem.accelerator = hotkey }
    }

    abstract fun onItemClicked(e: ActionEvent)
}