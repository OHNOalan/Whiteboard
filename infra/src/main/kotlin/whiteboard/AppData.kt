package whiteboard

import kotlinx.serialization.Serializable

@Serializable
data class AppEntitiesResponse(val entities: List<Entity>, val operation: Int)
