package net.codebot.application.components

import io.ktor.websocket.*
import javafx.scene.Node
import javafx.scene.paint.Paint
import javafx.scene.shape.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json


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
            decoded.add(deserializeSingle(entity, null, null))
        }
        return decoded
    }

    private fun deserializeLine(line: AppLineSchema, id: String, timestamp: Long): Node {
        val decodedLine = Polyline()
        decodedLine.strokeLineCap = StrokeLineCap.ROUND
        decodedLine.stroke = Paint.valueOf(line.stroke)
        decodedLine.strokeWidth = line.strokeWidth
        decodedLine.points.addAll(line.points)
        decodedLine.userData = NodeData(EntityIndex.LINE, id,timestamp)
        return decodedLine
    }

    private fun deserializeRectangle(rectangle: AppRectangleSchema, id: String, timestamp: Long): Node {
        val decodedRectangle = Rectangle()
        decodedRectangle.translateX = rectangle.x
        decodedRectangle.translateY = rectangle.y
        decodedRectangle.width = rectangle.width
        decodedRectangle.height = rectangle.height
        decodedRectangle.fill =
            if (rectangle.fill != null) Paint.valueOf(rectangle.fill) else null
        decodedRectangle.stroke = Paint.valueOf(rectangle.stroke)
        decodedRectangle.userData = NodeData(EntityIndex.RECTANGLE, id, timestamp)
        return decodedRectangle
    }

    private fun deserializeEllipse(ellipse: AppEllipseSchema, id: String, timestamp: Long): Node {
        val decodedEllipse = Ellipse()
        decodedEllipse.centerX = ellipse.x
        decodedEllipse.centerY = ellipse.y
        decodedEllipse.radiusX = ellipse.radiusX
        decodedEllipse.radiusY = ellipse.radiusY
        decodedEllipse.fill =
            if (ellipse.fill != null) Paint.valueOf(ellipse.fill) else null
        decodedEllipse.stroke = Paint.valueOf(ellipse.stroke)
        decodedEllipse.userData = NodeData(EntityIndex.ELLIPSE, id, timestamp)
        return decodedEllipse
    }

    private fun deserializeText(text: AppTextSchema, id: String, timestamp: Long): Node {
        val decodedText = AppTextEditor(
            text.translateX,
            text.translateY,
            text.defWidth,
            text.defHeight
        )
        decodedText.htmlText = text.htmlText
        decodedText.userData = NodeData(EntityIndex.TEXT, id, timestamp)
        return decodedText
    }

    private fun deserializeSegment(segment: AppSegmentSchema, id: String, timestamp: Long): Node {
        val decodedSegment = Line()
        decodedSegment.strokeLineCap = StrokeLineCap.ROUND
        decodedSegment.stroke = Paint.valueOf(segment.stroke)
        decodedSegment.strokeWidth = segment.width
        decodedSegment.startX = segment.startX
        decodedSegment.startY = segment.startY
        decodedSegment.endX = segment.endX
        decodedSegment.endY = segment.endY
        decodedSegment.userData = NodeData(EntityIndex.SEGMENT, id, timestamp)
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
                    nodeData.timestamp
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
                    AppLineSchema.serializer(),
                    AppLineSchema(line.stroke.toString(), line.strokeWidth, line.points)
                )
            }

            EntityIndex.RECTANGLE -> {
                val rectangle = node as Rectangle
                result = Json.encodeToString(
                    AppRectangleSchema.serializer(),
                    AppRectangleSchema(
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
                    AppEllipseSchema.serializer(),
                    AppEllipseSchema(
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
                    AppTextSchema.serializer(),
                    AppTextSchema(
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
                    AppSegmentSchema.serializer(),
                    AppSegmentSchema(
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

    fun deserializeSingle(entity: AppEntitySchema, id: String?, timestamp: Long?): Node {
        val nodeId = id ?: generateNodeId()
        val nodeTimestamp = timestamp ?: System.currentTimeMillis()
        when (entity.type) {
            EntityIndex.LINE -> {
                return deserializeLine(
                    Json.decodeFromString(
                        AppLineSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp
                )
            }

            EntityIndex.RECTANGLE -> {
                return deserializeRectangle(
                    Json.decodeFromString(
                        AppRectangleSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp
                )
            }

            EntityIndex.ELLIPSE -> {
                return deserializeEllipse(
                    Json.decodeFromString(
                        AppEllipseSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp
                )
            }

            EntityIndex.TEXT -> {
                return deserializeText(
                    Json.decodeFromString(
                        AppTextSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp
                )
            }

            EntityIndex.SEGMENT -> {
                return deserializeSegment(
                    Json.decodeFromString(
                        AppSegmentSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp
                )
            }
        }
        println("Cannot deserialize unknown object.")
        return Line()
    }
}