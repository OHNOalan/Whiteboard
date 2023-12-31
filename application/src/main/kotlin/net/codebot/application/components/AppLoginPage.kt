package net.codebot.application.components

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import java.net.URLEncoder
import java.util.prefs.Preferences


/**
 * Creates the login page for the whiteboard app.
 *
 * @property layoutReference Reference to the layout object for the app.
 */
class AppLoginPage(
    private val layoutReference: AppLayout,
) : GridPane() {
    init {
        this.alignment = Pos.CENTER
        this.hgap = 10.0
        this.vgap = 10.0
        this.padding = Insets(30.0)
        val loginHeader = Text("Login to Whiteboard")
        loginHeader.font = (Font.font("System", FontWeight.NORMAL, 25.0))
        this.add(loginHeader, 0, 0, 2, 1)

        val loginError = Label("")
        loginError.textFill = Color.RED
        this.add(loginError, 1, 1)

        val registerError = Label("")
        registerError.textFill = Color.RED
        this.add(registerError, 1, 1)

        val token = Preferences.userRoot().get("token", "Token")

        if (token != "Token") {
            try {
                val response = AppUtils.httpRequest(
                    "/user/autologin",
                    String.format(
                        "token=%s",
                        URLEncoder.encode(token, AppSettings.CHARSET)
                    )
                )

                if (response.success) {
                    loginError.opacity = 0.0
                    registerError.opacity = 0.0
                    AppData.roomCode = response.roomCode
                    layoutReference.setUsername(response.message)
                } else {
                    Preferences.userRoot().clear()
                }
            } catch (e: Exception) {
                Preferences.userRoot().clear()
                e.printStackTrace()
            }
        }

        val usernameLabel = Label("Username:")
        this.add(usernameLabel, 0, 2)

        val usernameTextField = TextField()
        this.add(usernameTextField, 1, 2)

        val passwordLabel = Label("Password:")
        this.add(passwordLabel, 0, 3)

        val passwordBox = PasswordField()
        this.add(passwordBox, 1, 3)

        val rememberMeCheckbox = CheckBox("Remember Me")
        val hbRememberMeCheckbox = HBox(1.0)
        hbRememberMeCheckbox.alignment = Pos.BOTTOM_RIGHT
        hbRememberMeCheckbox.children.add(rememberMeCheckbox)
        this.add(hbRememberMeCheckbox, 1, 4)

        val registerButton = Button("Register")
        val hbRegisterButton = HBox(1.0)
        hbRegisterButton.alignment = Pos.BOTTOM_LEFT
        hbRegisterButton.children.add(registerButton)
        this.add(hbRegisterButton, 0, 5)
        registerButton.onMouseClicked = EventHandler {
            val urlRoute = "/user/create"
            val username = usernameTextField.text
            val password = passwordBox.text
            val urlParams = String.format(
                "username=%s&password=%s",
                URLEncoder.encode(username, AppSettings.CHARSET),
                URLEncoder.encode(password, AppSettings.CHARSET)
            )
            try {
                val response = AppUtils.httpRequest(urlRoute, urlParams)

                if (response.success) {
                    if (rememberMeCheckbox.isSelected) {
                        val pref: Preferences = Preferences.userRoot()
                        pref.put("token", response.message)
                    }
                    loginError.opacity = 0.0
                    registerError.opacity = 0.0
                    AppData.roomCode = response.roomCode
                    layoutReference.setUsername(username)
                } else {
                    registerError.text = response.message
                    registerError.opacity = 1.0
                    loginError.opacity = 0.0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val loginButton = Button("Login")
        val hbLoginButton = HBox(1.0)
        hbLoginButton.alignment = Pos.BOTTOM_RIGHT
        hbLoginButton.children.add(loginButton)
        loginButton.onMouseClicked = EventHandler {
            val urlRoute = "/user/login"
            val username = usernameTextField.text
            val password = passwordBox.text
            val urlParams = String.format(
                "username=%s&password=%s",
                URLEncoder.encode(username, AppSettings.CHARSET),
                URLEncoder.encode(password, AppSettings.CHARSET)
            )
            try {
                val response = AppUtils.httpRequest(urlRoute, urlParams)

                if (response.success) {
                    if (rememberMeCheckbox.isSelected) {
                        val pref: Preferences = Preferences.userRoot()
                        pref.put("token", response.message)
                    }
                    loginError.opacity = 0.0
                    registerError.opacity = 0.0
                    AppData.roomCode = response.roomCode
                    layoutReference.setUsername(username)
                } else {
                    loginError.text = response.message
                    loginError.opacity = 1.0
                    registerError.opacity = 0.0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        this.add(hbLoginButton, 1, 5)
    }
}
