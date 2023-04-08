package whiteboard

import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import whiteboard.models.Entity

@Serializable
data class AppEntitiesSchema(val entities: List<Entity>, val operation: Int)

@Serializable
data class AppResponseSchema(val success: Boolean, val message: String)

class ClientConnection(val session: DefaultWebSocketSession)
