package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import net.codebot.application.components.menuItems.BaseMenuItem
import kotlin.system.exitProcess

/**
 * Creates the menu item to exit the application.
 * @param menu The menu to add this item to.
 */
class ExitMenuItem(menu: Menu) : BaseMenuItem(menu, "Exit") {
    /**
     * Exit the application normally when the menu item is clicked.
     */
    override fun onItemClicked() {
        exitProcess(0)
    }
}
