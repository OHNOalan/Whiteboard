package whiteboard.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.json.Json
import whiteboard.AppEntitiesResponse
import whiteboard.EntityControl
import whiteboard.OperationIndex
import whiteboard.UserControl
import java.util.*
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/sync") {
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                val data = Json.encodeToString(
                    AppEntitiesResponse.serializer(),
                    AppEntitiesResponse(EntityControl.load(123), OperationIndex.ADD)
                )
                println(data)
                send(data)
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    println(receivedText)
                    val response = Json.decodeFromString(AppEntitiesResponse.serializer(), receivedText)
                    when (response.operation) {
                        OperationIndex.ADD -> {
                            for (entity in response.entities) {
                                EntityControl.create(
                                    entity.id, entity.roomId, entity.descriptor, entity.type, entity.timestamp
                                )
                            }
                        }

                        OperationIndex.DELETE -> {
                            for (entity in response.entities) {
                                EntityControl.delete(
                                    entity.id
                                )
                            }
                        }

                        OperationIndex.MODIFY -> {
                            for (entity in response.entities) {
                                EntityControl.modify(
                                    entity.id,
                                    entity.descriptor
                                )
                            }
                        }
                    }
                    for (connection in connections) {
                        if (connection != thisConnection) {
                            println("Sending...")
                            connection.session.send(receivedText)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "An exception occurred, closing connection"))
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }

        // API calls below

        get("/") {
            call.respondText { "Server online!" }
        }
        post("/user/create") {
            val formParameters = call.receiveParameters()
            val username = formParameters.getOrFail<String>("username")
            val password = formParameters.getOrFail<String>("password")
            if (username.length < 5 || password.length < 5) {
                call.respondText { "-Length of username or password is too short." }
            } else {
                val user = UserControl.create(username, password)
                if (user != null) {
                    call.respondText { "+" + user.token() }
                } else {
                    call.respondText { "-" + "Failed to create new user" }
                }
            }
        }
        post("/user/login") {
            val formParameters = call.receiveParameters()
            val user = UserControl.login(
                formParameters.getOrFail<String>("username"),
                formParameters.getOrFail<String>("password")
            )
            if (user != null) {
                call.respondText { "+" + user.token() }
            } else {
                call.respondText { "-" + "Failed to login" }
            }
        }
        post("/user/autologin") {
            val formParameters = call.receiveParameters()
            val user = UserControl.loginWithToken(formParameters.getOrFail<String>("token"))
            if (user != null) {
                call.respondText { "+" + user.username }
            } else {
                call.respondText { "-" + "Failed to login" }
            }
        }
        post("/entity/create") {
            val formParameters = call.receiveParameters()
            val entity = EntityControl.create(
                formParameters.getOrFail<String>("id"),
                formParameters.getOrFail<Int>("roomId"),
                formParameters.getOrFail<String>("descriptor"),
                formParameters.getOrFail<String>("type"),
                formParameters.getOrFail<Long>("timestamp")
            )
            if (entity != null) {
                connections.forEach {
                    it.session.send(
                        Json.encodeToString(
                            AppEntitiesResponse.serializer(),
                            AppEntitiesResponse(
                                listOf(entity),
                                OperationIndex.ADD
                            )
                        )
                    )
                }
                call.respondText { "+" }
            } else {
                call.respondText { "-" }
            }
        }
        post("/entity/load") {
            val formParameters = call.receiveParameters()
            call.respondText {
                Json.encodeToString(
                    AppEntitiesResponse.serializer(),
                    AppEntitiesResponse(EntityControl.load(formParameters.getOrFail<Int>("roomId")), OperationIndex.ADD)
                )
            }
        }
        post("/entity/delete") {
            val formParameters = call.receiveParameters()
            call.respondText { if (EntityControl.delete(formParameters.getOrFail<String>("id"))) "+" else "-" }
        }
    }
}

class Connection(val session: DefaultWebSocketSession)



