package net.codebot.application.components

import kotlinx.serialization.Serializable


@Serializable
data class AppLineSchema(
    val stroke: String,
    val strokeWidth: Double,
    val points: List<Double>
)

@Serializable
data class AppRectangleSchema(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val fill: String?,
    val stroke: String
)

@Serializable
data class AppEllipseSchema(
    val x: Double,
    val y: Double,
    val radiusX: Double,
    val radiusY: Double,
    val fill: String?,
    val stroke: String
)

@Serializable
data class AppTextSchema(
    val translateX: Double,
    val translateY: Double,
    val defWidth: Double,
    val defHeight: Double,
    var htmlText: String
)

@Serializable
data class AppSegmentSchema(
    val stroke: String,
    val width: Double,
    val startX: Double,
    val startY: Double,
    val endX: Double,
    val endY: Double,
)

@Serializable
data class AppEntitySchema(
    val id: String,
    val roomId: Int,
    var descriptor: String,
    var previousDescriptor: String?,
    val type: String,
    val timestamp: Long,
)

@Serializable
data class AppEntitiesSchema(
    val entities: List<AppEntitySchema>,
    val operation: Int,
    val undoState: Int,
    val roomCode: String = ""
)

@Serializable
data class AppResponseSchema(
    val success: Boolean,
    val message: String,
    val roomCode: String
)

data class NodeData(val type: String, val id: String, val timestamp: Long)
