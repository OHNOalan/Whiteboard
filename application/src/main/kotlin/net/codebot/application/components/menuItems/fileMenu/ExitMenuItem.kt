package net.codebot.application.components.menuItems.fileMenu

import javafx.scene.control.Menu
import net.codebot.application.components.menuItems.BaseMenuItem
import kotlin.system.exitProcess

class ExitMenuItem(menu: Menu) : BaseMenuItem(menu, "Exit") {
    override fun onItemClicked() {
        exitProcess(0)
    }
}
