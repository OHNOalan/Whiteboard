package whiteboard.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import whiteboard.*
import whiteboard.models.EntityControl
import java.time.Duration
import java.util.*


suspend fun processMessage(
    receivedMessage: AppEntitiesSchema,
    operation: Int,
    usePreviousDescriptor: Boolean = false
) {
    when (operation) {
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
                    if (receivedMessage.operation == OperationIndex.UNDO) {
                        broadcastAll = true
                        responseText = null
                        val message = undoRedoStacks[123]?.popUndoMessage()
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
                            processMessage(message, operation, true)
                        }
                    } else if (receivedMessage.operation == OperationIndex.REDO) {
                        broadcastAll = true
                        responseText = null
                        val message = undoRedoStacks[123]?.popRedoMessage()
                        if (message != null) {
                            responseText = Json.encodeToString(
                                AppEntitiesSchema.serializer(),
                                message
                            )
                            processMessage(message, message.operation)
                        }
                    } else {
                        processMessage(receivedMessage, receivedMessage.operation)
                        undoRedoStacks[123]?.addToUndoStack(receivedMessage)
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




