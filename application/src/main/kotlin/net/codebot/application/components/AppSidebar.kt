package net.codebot.application.components

import javafx.geometry.HPos
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

class AppSidebar(borderPane: BorderPane, appCanvas: AppCanvas) {
    init {
        val sideBar = VBox()
        val styleBar = AppStylebar(sideBar)
        sideBar.children.add(AppUtils.createVSpacer())
        AppToolbar(sideBar, appCanvas, styleBar)
        val separator = Separator()
        separator.halignment = HPos.CENTER
        sideBar.children.addAll(AppUtils.createVSpacer(), separator, AppUtils.createVSpacer(), styleBar)
        sideBar.children.add(AppUtils.createVSpacer())
        borderPane.left = sideBar
    }
}
