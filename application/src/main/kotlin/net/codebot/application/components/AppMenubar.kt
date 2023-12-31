package net.codebot.application.components

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.layout.VBox
import net.codebot.application.components.menuItems.editMenu.ClearMenuItem
import net.codebot.application.components.menuItems.editMenu.RedoMenuItem
import net.codebot.application.components.menuItems.editMenu.UndoMenuItem
import net.codebot.application.components.menuItems.fileMenu.*
import net.codebot.application.components.menuItems.viewMenu.ResetZoomMenuItem
import net.codebot.application.components.menuItems.viewMenu.ZoomInMenuItem
import net.codebot.application.components.menuItems.viewMenu.ZoomOutMenuItem

/**
 * Initializes menu dropdowns with their respective items on the top bar of the application
 * @param topBar A reference to the stage of the entire whiteboard app.
 * @param canvas The main canvas of the whiteboard.
 */
class AppMenubar(topBar: VBox, canvas: AppCanvas) {
    init {
        val menuBar = MenuBar()

        val fileMenu = Menu("File")
        val saveMenuItem = SaveMenuItem(fileMenu, canvas)
        SaveAsMenuItem(fileMenu, canvas, saveMenuItem)
        LoadMenuItem(fileMenu, canvas)
        ExportMenuItem(fileMenu, canvas)
        ExitMenuItem(fileMenu)

        menuBar.menus.add(fileMenu)

        val editMenu = Menu("Edit")
        UndoMenuItem(editMenu, canvas)
        RedoMenuItem(editMenu, canvas)
        ClearMenuItem(editMenu, canvas)

        menuBar.menus.add(editMenu)

        val viewMenu = Menu("View")
        ZoomInMenuItem(viewMenu, canvas)
        ZoomOutMenuItem(viewMenu, canvas)
        ResetZoomMenuItem(viewMenu, canvas)

        menuBar.menus.add(viewMenu)

        topBar.children.add(menuBar)
    }
}
