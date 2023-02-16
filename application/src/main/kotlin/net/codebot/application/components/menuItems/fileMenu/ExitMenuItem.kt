package net.codebot.application.components.menuItems.fileMenu

import javafx.event.ActionEvent
import javafx.scene.control.Menu
import net.codebot.application.components.menuItems.BaseMenuItem
import kotlin.system.exitProcess

class ExitMenuItem(menu: Menu) : BaseMenuItem(menu, "Exit") {
    override fun onItemClicked(e: ActionEvent) {
        exitProcess(0)
    }
}
