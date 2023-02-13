package net.codebot.application.components

import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

class AppSidebar(borderPane: BorderPane, appCanvas: AppCanvas) {
    init {
        val sideBar = VBox()
        sideBar.children.add(AppUtils.createVSpacer())
        AppToolbar(sideBar, appCanvas)
        sideBar.children.add(AppUtils.createVSpacer())
        borderPane.left = sideBar
    }
}