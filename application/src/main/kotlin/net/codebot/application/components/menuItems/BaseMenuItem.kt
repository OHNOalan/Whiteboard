package net.codebot.application.components.menuItems

import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCodeCombination

/**
 * The base menu item which all menu items inherit from.
 * @param menu The menu to add this item to.
 * @param itemName The name of this menu item.
 * @param hotkey An optional keyboard shortcut to activate the menu item.
 */
abstract class BaseMenuItem(
    menu: Menu,
    itemName: String,
    hotkey: KeyCodeCombination? = null
) {
    init {
        val menuItem = MenuItem(itemName)
        menu.items.add(menuItem)

        menuItem.setOnAction {
            onItemClicked()
        }

        hotkey?.let { menuItem.accelerator = hotkey }
    }

    /**
     * Called when the menu item is clicked. Must be overwritten.
     */
    abstract fun onItemClicked()
}