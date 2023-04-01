package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Separator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

class AppSidebar(borderPane: BorderPane, appCanvas: AppCanvas, appLayout: AppLayout) {
    init {
        val sideBar = VBox()
        val styleBar = AppStylebar(sideBar)

        val logoutImage = ImageView(Image("file:src/main/assets/cursors/logout.png", 20.0, 20.0, true, true))
        logoutImage.maxHeight(20.0)
        val logoutButton = Button("Logout", logoutImage)
        logoutButton.onMouseReleased = EventHandler {
            appLayout.logout()
        }
        val usernameText = Text(appLayout.getUsername())
        usernameText.font = (Font.font("System", FontWeight.NORMAL, 20.0))
        val usernameContainer = HBox(usernameText, logoutButton)
        usernameContainer.padding = Insets(15.0, 0.0, 0.0, 35.0)
        usernameContainer.spacing = 20.0
        sideBar.children.add(usernameContainer)

        AppToolbar(sideBar, appCanvas, styleBar)
        val separator = Separator()
        separator.halignment = HPos.CENTER
        sideBar.children.addAll(AppUtils.createVSpacer(), separator, AppUtils.createVSpacer(), styleBar)
        sideBar.children.add(AppUtils.createVSpacer())
        borderPane.left = sideBar
    }
}
