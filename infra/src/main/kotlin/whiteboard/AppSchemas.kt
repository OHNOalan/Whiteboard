package whiteboard

import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import whiteboard.models.Entity
import java.util.*

@Serializable
data class AppEntitiesSchema(
    val entities: List<Entity>,
    val operation: Int,
    var undoState: Int
)

@Serializable
data class AppResponseSchema(val success: Boolean, val message: String)

class ClientConnection(val session: DefaultWebSocketSession, val roomId: Int)

class UndoRedoStack() {
    private val undoStack =
        Collections.synchronizedList<AppEntitiesSchema>(LinkedList())
    private val redoStack =
        Collections.synchronizedList<AppEntitiesSchema>(LinkedList())

    fun addToUndoStack(action: AppEntitiesSchema) {
        redoStack.clear()
        undoStack.add(action)
    }

    fun popUndoMessage(): String? {
        val message = undoStack.removeLastOrNull()
        if (message != null) {
            redoStack.add(message)
        }
        message?.undoState = UndoIndex.UNDO
        return message?.let { Json.encodeToString(AppEntitiesSchema.serializer(), it) }
    }

    fun popRedoMessage(): String? {
        val message = redoStack.removeLastOrNull()
        if (message != null) {
            undoStack.add(message)
        }
        message?.undoState = UndoIndex.REDO
        return message?.let { Json.encodeToString(AppEntitiesSchema.serializer(), it) }
    }
}
