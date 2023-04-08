package whiteboard.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import whiteboard.AppEntitiesSchema
import whiteboard.ClientConnection
import whiteboard.models.EntityControl
import whiteboard.OperationIndex
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
        val connections =
            Collections.synchronizedSet<ClientConnection?>(LinkedHashSet())
        webSocket("/sync") {
            val thisConnection = ClientConnection(this)
            connections += thisConnection
            try {
                val data = Json.encodeToString(
                    AppEntitiesSchema.serializer(),
                    AppEntitiesSchema(EntityControl.load(123), OperationIndex.ADD)
                )
                println("Initial:")
                println(data)
                send(data)
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    println("Received:")
                    println(receivedText)
                    val response = Json.decodeFromString(
                        AppEntitiesSchema.serializer(),
                        receivedText
                    )
                    when (response.operation) {
                        OperationIndex.ADD -> {
                            for (entity in response.entities) {
                                EntityControl.create(
                                    entity.id,
                                    entity.roomId,
                                    entity.descriptor,
                                    entity.type,
                                    entity.timestamp
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
                close(
                    CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR,
                        "An exception has occurred. Closing connection..."
                    )
                )
            } finally {
                connections -= thisConnection
            }
        }
    }
}




