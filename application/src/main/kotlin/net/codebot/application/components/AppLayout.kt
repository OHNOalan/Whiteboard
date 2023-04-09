package net.codebot.application.components

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import javafx.application.Platform
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.prefs.Preferences

class AppLayout(stage: Stage) {
    private var username: String = ""
    private val stageReference: Stage = stage
    private var whiteboard: BorderPane = BorderPane()
    private val loginPage: GridPane
    private val sceneReference: Scene = Scene(Pane(), 640.0, 480.0)
    private val appCanvas: AppCanvas = AppCanvas(whiteboard)
    private lateinit var webSocketSession: WebSocketSession

    init {
        stage.scene = sceneReference

        loginPage = AppLoginPage(this)
        // if there's no token load login page
        val token = Preferences.userRoot().get("token", "Token")
        if (token == "Token") {
            setScene(SceneIndex.LOGIN_PAGE)
        }
    }

    private fun startSocketConnection() {
        // set up Websocket client
        val client = HttpClient(CIO) {
            install(WebSockets)
        }

        val webSocketThread = GlobalScope.launch {
            runBlocking {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = "0.0.0.0",
                    path = "/sync",
                    port = 8080,
                    request = {
                        // Set WebSocket headers or options if needed
                    }
                ) {
                    webSocketSession = this
                    AppData.registerSocket(this)
                    // Called when a message is received from the WebSocket
                    for (frame in incoming) {
                        val update = (frame as Frame.Text).readText()
                        println(update)
                        Platform.runLater {
                            appCanvas.webUpdateCallback(update)
                        }
                    }
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // When the app is about to close, stop the other thread
                webSocketThread.cancel()
            }
        })
    }

    private fun closeSocketConnection() {
        GlobalScope.launch {
            webSocketSession.close()
        }
    }

    private fun setScene(sceneIndex: SceneIndex) {
        when (sceneIndex) {
            SceneIndex.WHITEBOARD -> {
                stageReference.width = 1100.0
                stageReference.height = 800.0

                AppSidebar(whiteboard, appCanvas, this)
                AppTopbar(whiteboard, appCanvas)
                sceneReference.root = whiteboard
                centerStage()
            }

            SceneIndex.LOGIN_PAGE -> {
                stageReference.width = 640.0
                stageReference.height = 480.0
                sceneReference.root = loginPage
                centerStage()
            }
        }
    }

    fun setUsername(user: String) {
        username = user

        if (user.isEmpty()) {
            setScene(SceneIndex.LOGIN_PAGE)

            // Close socket connection when user logs out
            closeSocketConnection()
        } else {
            setScene(SceneIndex.WHITEBOARD)

            // Start socket only when user is authenticated
            startSocketConnection()
        }
    }

    fun getUsername(): String {
        return username
    }

    fun logout() {
        Preferences.userRoot().clear()
        setUsername("")
    }

    private fun centerStage() {
        val bounds: Rectangle2D = Screen.getPrimary().visualBounds
        val centerX: Double = bounds.minX + (bounds.width - stageReference.width) * (1.0f / 2)
        val centerY: Double = bounds.minY + (bounds.height - stageReference.height) * (1.0f / 3)

        stageReference.x = centerX
        stageReference.y = centerY
    }
}
