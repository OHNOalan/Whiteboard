package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import java.net.URLEncoder

/**
 * Creates a status component that displays the currently logged-in user
 * as well as current room code and ability to join/create rooms.
 * @param sideBar The sidebar to add this status component to.
 * @param appLayout The layout component of the whiteboard.
 * @param appCanvas The main canvas of the whiteboard.
 */
class AppStatus(
    sideBar: VBox,
    appLayout: AppLayout,
    appCanvas: AppCanvas,
) {
    private var roomCodeTextField: TextField
    private val canvasReference: AppCanvas = appCanvas

    init {
        val logoutImage = ImageView(
            Image(
                "file:src/main/assets/cursors/logout.png",
                20.0,
                20.0,
                true,
                true
            )
        )
        logoutImage.maxHeight(20.0)
        val logoutButton = Button("Logout", logoutImage)
        logoutButton.onMouseReleased = EventHandler {
            appLayout.logout()
        }
        val usernameText = Text("User: " + appLayout.getUsername())
        usernameText.font = (Font.font("System", FontWeight.NORMAL, 20.0))
        val usernameContainer = HBox(
            AppUtils.createHSpacer(),
            usernameText,
            AppUtils.createHSpacer(),
            logoutButton,
            AppUtils.createHSpacer()
        )
        val roomCodeLabel = Label("Room Code:")
        roomCodeLabel.font = Font.font("System", FontWeight.NORMAL, 16.0)
        roomCodeTextField = TextField()
        val joinButton = Button("Join Room")
        val newButton = Button("New Room")
        joinButton.onMouseReleased = EventHandler {
            joinRoom()
        }
        newButton.onMouseReleased = EventHandler {
            newRoom()
        }
        val roomButtonsContainer = HBox(
            AppUtils.createHSpacer(),
            joinButton,
            newButton,
            AppUtils.createHSpacer()
        )
        roomButtonsContainer.spacing = 20.0
        val roomContainer = HBox(
            AppUtils.createHSpacer(),
            roomCodeLabel,
            roomCodeTextField,
            AppUtils.createHSpacer(),
        )
        roomContainer.spacing = 20.0
        roomCodeTextField.text = AppData.roomCode
        val statusContainer = VBox(
            usernameContainer,
            roomContainer,
            roomButtonsContainer,
        )
        statusContainer.padding = Insets(20.0, 0.0, 20.0, 0.0)
        statusContainer.spacing = 20.0
        sideBar.children.addAll(
            AppUtils.createVSpacer(),
            statusContainer,
        )
    }

    private fun requestRoom(roomCode: String) {
        val urlRoute = "/room/update"
        val urlParams = String.format(
            "roomCode=%s",
            URLEncoder.encode(roomCode, AppSettings.CHARSET),
        )
        try {
            val response = AppUtils.httpRequest(urlRoute, urlParams)

            if (response.success) {
                AppData.roomCode = response.roomCode
                roomCodeTextField.text = response.roomCode
                canvasReference.clearCanvas(false)
                AppData.broadcastJoin()
                val alert = Alert(
                    Alert.AlertType.INFORMATION,
                    "Successfully joined room '${response.roomCode}'."
                )
                alert.showAndWait()
            } else {
                roomCodeTextField.text = AppData.roomCode
                val alert = Alert(
                    Alert.AlertType.ERROR,
                    response.message
                )
                alert.showAndWait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun newRoom() {
        val alert = Alert(
            Alert.AlertType.CONFIRMATION,
            "Requesting a new room will clear your current whiteboard. Proceed?",
            ButtonType.YES, ButtonType.NO
        )
        alert.showAndWait()
        if (alert.result == ButtonType.YES) {
            requestRoom("")
        }
    }

    private fun joinRoom() {
        if (roomCodeTextField.text == AppData.roomCode) {
            val alert = Alert(
                Alert.AlertType.ERROR,
                "You are currently in the room you are trying to join."
            )
            alert.showAndWait()
            return
        }
        if (roomCodeTextField.text.isEmpty()) {
            val alert = Alert(
                Alert.AlertType.ERROR,
                "Please fill out the room code."
            )
            alert.showAndWait()
            return
        }
        requestRoom(roomCodeTextField.text)
    }
}
