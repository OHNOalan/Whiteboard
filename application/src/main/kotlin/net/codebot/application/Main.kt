package net.codebot.application

import javafx.application.Application
import javafx.stage.Stage
import net.codebot.application.components.AppLayout
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors


class Main : Application() {
    override fun start(stage: Stage) {
        // set up Websocket client
        val client = HttpClient(CIO) {
            install(WebSockets)
        }

        stage.isResizable = true
        stage.title = "Whiteboard"
        stage.minHeight= 480.0
        stage.minWidth= 640.0
        stage.maxHeight = 1200.0
        stage.maxWidth = 1600.0

        val appLayout = AppLayout(stage)
        // TODO update host and path accordingly
        val webSocketThread = GlobalScope.launch {
            runBlocking {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = "0.0.0.0",
                    path = "/chat",
                    port = 8080,
                    request = {
                        // Set WebSocket headers or options if needed
                    }
                ) {
                    // Called when a message is received from the WebSocket
                    for (message in incoming) {
                        println("Received message")
                        println(message.toString())
                        appLayout.appCanvas.webUpdateCallback(message.toString())
                    }
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("shutdown hook called")
                // When the app is about to close, stop the other thread
                webSocketThread.cancel()
            }
        })

        stage.show()
    }
}