package net.codebot.application.components

import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

/**
 * Initializes sidebar for the application as a vertical box
 *
 * Includes all tools and logout button
 *
 * @param borderPane The main pane where everything is attached to.
 * @param appCanvas The main canvas of the whiteboard.
 * @param appLayout The main layout where scenes are organized
 */
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
