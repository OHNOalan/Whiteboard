package net.codebot.application.components


import io.ktor.websocket.*
import javafx.scene.Node
import javafx.scene.paint.Paint
import javafx.scene.shape.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class AppLine(
    val stroke: String,
    val strokeWidth: Double,
    val points: List<Double>
)

@Serializable
data class AppRectangle(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val fill: String?,
    val stroke: String
)

@Serializable
data class AppEllipse(
    val x: Double,
    val y: Double,
    val radiusX: Double,
    val radiusY: Double,
    val fill: String?,
    val stroke: String
)

@Serializable
data class AppText(
    val translateX: Double,
    val translateY: Double,
    val defWidth: Double,
    val defHeight: Double,
    val htmlText: String
)

@Serializable
data class AppSegment(
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
    val descriptor: String,
    val type: String,
    val timestamp: Long,
)

@Serializable
data class AppEntitiesSchema(
    val entities: List<AppEntitySchema>, val operation: Int
)

data class NodeData(val type: String, val id: String)

object AppData {
    private var counter = 0
    private lateinit var socket: DefaultWebSocketSession
    private lateinit var appLayout: AppLayout

    fun serializeLocal(nodes: List<Node>): String {
        val entities = mutableListOf<AppEntitySchema>()
        for (node in nodes) {
            val type = (node.userData as NodeData).type
            entities.add(AppEntitySchema("", 0, serializeSingle(node), type, 0))
        }
        return Json.encodeToString(
            AppEntitiesSchema.serializer(),
            AppEntitiesSchema(entities, OperationIndex.ADD)
        )
    }

    fun deserializeLocal(entities: String): List<Node> {
        val appEntities =
            Json.decodeFromString(AppEntitiesSchema.serializer(), entities)
        val decoded = mutableListOf<Node>()
        for (entity in appEntities.entities) {
            decoded.add(deserializeSingle(entity, null))
        }
        return decoded
    }

    private fun deserializeLine(line: AppLine, id: String): Node {
        val decodedLine = Polyline()
        decodedLine.strokeLineCap = StrokeLineCap.ROUND
        decodedLine.stroke = Paint.valueOf(line.stroke)
        decodedLine.strokeWidth = line.strokeWidth
        decodedLine.points.addAll(line.points)
        decodedLine.userData = NodeData(EntityIndex.LINE, id)
        return decodedLine
    }

    private fun deserializeRectangle(rectangle: AppRectangle, id: String): Node {
        val decodedRectangle = Rectangle()
        decodedRectangle.translateX = rectangle.x
        decodedRectangle.translateY = rectangle.y
        decodedRectangle.width = rectangle.width
        decodedRectangle.height = rectangle.height
        decodedRectangle.fill =
            if (rectangle.fill != null) Paint.valueOf(rectangle.fill) else null
        decodedRectangle.stroke = Paint.valueOf(rectangle.stroke)
        decodedRectangle.userData = NodeData(EntityIndex.RECTANGLE, id)
        return decodedRectangle
    }

    private fun deserializeEllipse(ellipse: AppEllipse, id: String): Node {
        val decodedEllipse = Ellipse()
        decodedEllipse.centerX = ellipse.x
        decodedEllipse.centerY = ellipse.y
        decodedEllipse.radiusX = ellipse.radiusX
        decodedEllipse.radiusY = ellipse.radiusY
        decodedEllipse.fill =
            if (ellipse.fill != null) Paint.valueOf(ellipse.fill) else null
        decodedEllipse.stroke = Paint.valueOf(ellipse.stroke)
        decodedEllipse.userData = NodeData(EntityIndex.ELLIPSE, id)
        return decodedEllipse
    }

    private fun deserializeText(text: AppText, id: String): Node {
        val decodedText = AppTextEditor(
            text.translateX,
            text.translateY,
            text.defWidth,
            text.defHeight
        )
        decodedText.htmlText = text.htmlText
        decodedText.userData = NodeData(EntityIndex.TEXT, id)
        return decodedText
    }

    private fun deserializeSegment(segment: AppSegment, id: String): Node {
        val decodedSegment = Line()
        decodedSegment.strokeLineCap = StrokeLineCap.ROUND
        decodedSegment.stroke = Paint.valueOf(segment.stroke)
        decodedSegment.strokeWidth = segment.width
        decodedSegment.startX = segment.startX
        decodedSegment.startY = segment.startY
        decodedSegment.endX = segment.endX
        decodedSegment.endY = segment.endY
        decodedSegment.userData = NodeData(EntityIndex.SEGMENT, id)
        return decodedSegment
    }

    fun broadcastAdd(nodes: List<Node>) {
        broadcast(OperationIndex.ADD, nodes)
    }

    fun broadcastDelete(nodes: List<Node>) {
        broadcast(OperationIndex.DELETE, nodes)
    }

    fun broadcastModify(nodes: List<Node>) {
        broadcast(OperationIndex.MODIFY, nodes)
    }

    fun generateNodeId(): String {
        val id = appLayout.getUsername() + "/" + System.currentTimeMillis()
            .toString() + "-" + counter.toString()
        counter += 1
        return id
    }

    fun registerSocket(session: DefaultWebSocketSession) {
        socket = session
    }

    fun registerAppLayout(appLayoutInstance: AppLayout) {
        appLayout = appLayoutInstance
    }

    private fun broadcast(operation: Int, nodes: List<Node>) {
        val entities = mutableListOf<AppEntitySchema>()
        for (node in nodes) {
            val nodeData = node.userData as NodeData
            entities.add(
                AppEntitySchema(
                    nodeData.id,
                    123,
                    if (operation == OperationIndex.DELETE) "" else serializeSingle(node),
                    nodeData.type,
                    System.currentTimeMillis()
                )
            )
        }
        val data = Json.encodeToString(
            AppEntitiesSchema.serializer(),
            AppEntitiesSchema(entities, operation)
        )
        when (operation) {
            OperationIndex.ADD -> {
                println("Add:")
            }

            OperationIndex.DELETE -> {
                println("Delete:")
            }

            OperationIndex.MODIFY -> {
                println("Modify:")
            }
        }
        println(data)
        runBlocking {
            launch {
                socket.send(data)
            }
        }
    }

    private fun serializeSingle(node: Node): String {
        var result = ""
        when ((node.userData as NodeData).type) {
            EntityIndex.LINE -> {
                val line = node as Polyline
                result = Json.encodeToString(
                    AppLine.serializer(),
                    AppLine(line.stroke.toString(), line.strokeWidth, line.points)
                )
            }

            EntityIndex.RECTANGLE -> {
                val rectangle = node as Rectangle
                result = Json.encodeToString(
                    AppRectangle.serializer(),
                    AppRectangle(
                        rectangle.translateX,
                        rectangle.translateY,
                        rectangle.width,
                        rectangle.height,
                        if (rectangle.fill != null) rectangle.fill.toString() else null,
                        rectangle.stroke.toString()
                    )
                )
            }

            EntityIndex.ELLIPSE -> {
                val ellipse = node as Ellipse
                result = Json.encodeToString(
                    AppEllipse.serializer(),
                    AppEllipse(
                        ellipse.centerX,
                        ellipse.centerY,
                        ellipse.radiusX,
                        ellipse.radiusY,
                        if (ellipse.fill != null) ellipse.fill.toString() else null,
                        ellipse.stroke.toString()
                    )
                )
            }

            EntityIndex.TEXT -> {
                val text = node as AppTextEditor
                result = Json.encodeToString(
                    AppText.serializer(),
                    AppText(
                        text.translateX,
                        text.translateY,
                        text.prefWidth,
                        text.prefHeight,
                        text.htmlText
                    )
                )
            }

            EntityIndex.SEGMENT -> {
                val segment = node as Line
                result = Json.encodeToString(
                    AppSegment.serializer(),
                    AppSegment(
                        segment.stroke.toString(),
                        segment.strokeWidth,
                        segment.startX,
                        segment.startY,
                        segment.endX,
                        segment.endY
                    )
                )
            }
        }
        return result
    }

    fun deserializeSingle(entity: AppEntitySchema, id: String?): Node {
        val nodeId = id ?: generateNodeId()
        when (entity.type) {
            EntityIndex.LINE -> {
                return deserializeLine(
                    Json.decodeFromString(
                        AppLine.serializer(),
                        entity.descriptor
                    ), nodeId
                )
            }

            EntityIndex.RECTANGLE -> {
                return deserializeRectangle(
                    Json.decodeFromString(
                        AppRectangle.serializer(),
                        entity.descriptor
                    ), nodeId
                )
            }

            EntityIndex.ELLIPSE -> {
                return deserializeEllipse(
                    Json.decodeFromString(
                        AppEllipse.serializer(),
                        entity.descriptor
                    ), nodeId
                )
            }

            EntityIndex.TEXT -> {
                return deserializeText(
                    Json.decodeFromString(
                        AppText.serializer(),
                        entity.descriptor
                    ), nodeId
                )
            }

            EntityIndex.SEGMENT -> {
                return deserializeSegment(
                    Json.decodeFromString(
                        AppSegment.serializer(),
                        entity.descriptor
                    ), nodeId
                )
            }
        }
        println("Cannot deserialize unknown object.")
        return Line()
    }
}