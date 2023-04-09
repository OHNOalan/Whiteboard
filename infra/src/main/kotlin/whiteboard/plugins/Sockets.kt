package whiteboard.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import whiteboard.*
import whiteboard.models.EntityControl
import java.util.*
import kotlin.collections.HashMap
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
        val undoRedoStacks = Collections.synchronizedMap<Int, UndoRedoStack>(HashMap())

        webSocket("/sync") {
            val thisConnection = ClientConnection(this, 123)
            connections += thisConnection
            if (!undoRedoStacks.containsKey(123)) {
                undoRedoStacks[123] = UndoRedoStack()
            }
            try {
                val data = Json.encodeToString(
                    AppEntitiesSchema.serializer(),
                    AppEntitiesSchema(
                        EntityControl.load(123),
                        OperationIndex.ADD,
                        UndoIndex.NONE
                    )
                )
                println("Initial:")
                println(data)
                send(data)
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    var responseText: String? = receivedText
                    var broadcastAll = false
                    println("Received:")
                    println(receivedText)
                    val receivedMessage = Json.decodeFromString(
                        AppEntitiesSchema.serializer(),
                        receivedText
                    )
                    when (receivedMessage.operation) {
                        OperationIndex.ADD -> {
                            for (entity in receivedMessage.entities) {
                                EntityControl.create(
                                    entity.id,
                                    entity.roomId,
                                    entity.descriptor,
                                    entity.type,
                                    entity.timestamp
                                )
                            }
                            undoRedoStacks[123]?.addToUndoStack(receivedMessage)
                        }

                        OperationIndex.DELETE -> {
                            for (entity in receivedMessage.entities) {
                                EntityControl.delete(
                                    entity.id
                                )
                            }
                            undoRedoStacks[123]?.addToUndoStack(receivedMessage)
                        }

                        OperationIndex.MODIFY -> {
                            for (entity in receivedMessage.entities) {
                                EntityControl.modify(
                                    entity.id,
                                    entity.descriptor
                                )
                            }
                            undoRedoStacks[123]?.addToUndoStack(receivedMessage)
                        }

                        OperationIndex.UNDO -> {
                            responseText = undoRedoStacks[123]?.popUndoMessage()
                            broadcastAll = true
                        }

                        OperationIndex.REDO -> {
                            responseText = undoRedoStacks[123]?.popRedoMessage()
                            broadcastAll = true
                        }
                    }
                    if (responseText != null) {
                        println("Response:")
                        println(responseText)
                        for (connection in connections) {
                            if (broadcastAll || connection != thisConnection) {
                                println("Sending...")
                                connection.session.send(responseText)
                            }
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




