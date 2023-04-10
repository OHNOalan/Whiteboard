package whiteboard.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import whiteboard.*
import whiteboard.models.EntityControl
import whiteboard.models.RoomControl
import java.time.Duration
import java.util.*

/**
 * Process a list of entities and serialize those entities to the database.
 * @param receivedMessage The serialized information of entities list.
 * @param operation The type of operation for entities.
 * @param roomId The roomId that the transaction will operate on.
 * @param usePreviousDescriptor Only used for a modify action. Used for undo/redo
 * to track the previous state of the item.
 */
suspend fun processMessage(
    receivedMessage: AppEntitiesSchema,
    operation: Int,
    roomId: Int,
    usePreviousDescriptor: Boolean = false
) {
    when (operation) {
        OperationIndex.ADD -> {
            for (entity in receivedMessage.entities) {
                EntityControl.create(
                    entity.id,
                    roomId,
                    entity.descriptor,
                    entity.type,
                    entity.timestamp
                )
            }
        }

        OperationIndex.DELETE -> {
            for (entity in receivedMessage.entities) {
                EntityControl.delete(
                    entity.id
                )
            }
        }

        OperationIndex.MODIFY -> {
            for (entity in receivedMessage.entities) {
                var descriptor = entity.descriptor
                if (usePreviousDescriptor && entity.previousDescriptor != null) {
                    descriptor = entity.previousDescriptor
                }
                EntityControl.modify(
                    entity.id,
                    descriptor
                )
            }
        }
    }
}


/**
 * The endpoint for handling real-time synchronization and saving changes to the database.
 */
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
            val thisConnection = ClientConnection(this)
            connections += thisConnection
            try {
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
                    if (receivedMessage.operation == OperationIndex.JOIN) {
                        val roomId = RoomControl.getRoomId(receivedMessage.roomCode)
                        if (roomId != null) {
                            thisConnection.setRoomId(roomId)
                            if (!undoRedoStacks.containsKey(roomId)) {
                                undoRedoStacks[roomId] = UndoRedoStack()
                            }
                            val data = Json.encodeToString(
                                AppEntitiesSchema.serializer(),
                                AppEntitiesSchema(
                                    EntityControl.load(roomId),
                                    OperationIndex.ADD,
                                    UndoIndex.NONE
                                )
                            )
                            println("Joined Room:")
                            println(data)
                            send(data)
                        }
                        continue
                    } else if (receivedMessage.operation == OperationIndex.UNDO) {
                        broadcastAll = true
                        responseText = null
                        val message =
                            undoRedoStacks[thisConnection.getRoomId()]?.popUndoMessage()
                        if (message != null) {
                            responseText = Json.encodeToString(
                                AppEntitiesSchema.serializer(),
                                message
                            )
                            var operation = OperationIndex.MODIFY
                            if (message.operation == OperationIndex.ADD) {
                                operation = OperationIndex.DELETE
                            } else if (message.operation == OperationIndex.DELETE) {
                                operation = OperationIndex.ADD
                            }
                            processMessage(
                                message,
                                operation,
                                thisConnection.getRoomId(),
                                true
                            )
                        }
                    } else if (receivedMessage.operation == OperationIndex.REDO) {
                        broadcastAll = true
                        responseText = null
                        val message =
                            undoRedoStacks[thisConnection.getRoomId()]?.popRedoMessage()
                        if (message != null) {
                            responseText = Json.encodeToString(
                                AppEntitiesSchema.serializer(),
                                message
                            )
                            processMessage(
                                message,
                                message.operation,
                                thisConnection.getRoomId(),
                            )
                        }
                    } else {
                        processMessage(
                            receivedMessage,
                            receivedMessage.operation,
                            thisConnection.getRoomId(),
                        )
                        undoRedoStacks[thisConnection.getRoomId()]?.addToUndoStack(
                            receivedMessage
                        )
                    }
                    if (responseText != null) {
                        println("Response:")
                        println(responseText)
                        for (connection in connections) {
                            if (connection.getRoomId() == thisConnection.getRoomId()) {
                                if (broadcastAll || connection != thisConnection) {
                                    println("Sending...")
                                    connection.session.send(responseText)
                                }
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




