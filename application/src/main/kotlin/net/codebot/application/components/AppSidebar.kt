package net.codebot.application.components

import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox


class AppSidebar(borderPane: BorderPane, appCanvas: AppCanvas, appLayout: AppLayout) {
    init {
        val sideBar = VBox()
        val styleBar = AppStylebar()

        AppStatus(sideBar, appLayout, appCanvas)
        sideBar.children.addAll(
            AppUtils.createVSpacer(),
            AppUtils.createSeparator(),
            AppUtils.createVSpacer()
        )

        AppToolbar(sideBar, appCanvas, styleBar)
        sideBar.children.addAll(
            AppUtils.createVSpacer(),
            AppUtils.createSeparator(),
            AppUtils.createVSpacer(),
            styleBar
        )

        sideBar.children.add(AppUtils.createVSpacer())
        borderPane.left = sideBar
    }
}
