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

    // Serializes a list of nodes into a string format
    // Use for saving canvas entities to a local file
    fun serializeLocal(nodes: List<Node>): String {
        val entities = mutableListOf<AppEntitySchema>()
        for (node in nodes) {
            val type = (node.userData as NodeData).type
            entities.add(AppEntitySchema("", 0, serializeSingle(node), type, 0))
        }
        return Json.encodeToString(
            AppEntitiesSchema.serializer(),
            AppEntitiesSchema(entities, OperationIndex.ADD, UndoIndex.NONE)
        )
    }

    fun deserializeLocal(entities: String): List<Node> {
        val appEntities =
            Json.decodeFromString(AppEntitiesSchema.serializer(), entities)
        val decoded = mutableListOf<Node>()
        for (entity in appEntities.entities) {
            decoded.add(deserializeSingle(entity, null, null, null))
        }
        return decoded
    }

    // Deserialize encoded line data into a polyline object
    private fun deserializeLine(
        line: AppLineSchema,
        id: String,
        timestamp: Long,
        node: Polyline
    ) {
        node.strokeLineCap = StrokeLineCap.ROUND
        node.stroke = Paint.valueOf(line.stroke)
        node.strokeWidth = line.strokeWidth
        node.points.clear()
        node.points.addAll(line.points)
        node.userData = NodeData(EntityIndex.LINE, id, timestamp)
    }

    private fun deserializeRectangle(
        rectangle: AppRectangleSchema,
        id: String,
        timestamp: Long,
        node: Rectangle
    ) {
        node.translateX = rectangle.x
        node.translateY = rectangle.y
        node.width = rectangle.width
        node.height = rectangle.height
        node.fill =
            if (rectangle.fill != null) Paint.valueOf(rectangle.fill) else null
        node.stroke = Paint.valueOf(rectangle.stroke)
        node.userData = NodeData(EntityIndex.RECTANGLE, id, timestamp)
    }

    private fun deserializeEllipse(
        ellipse: AppEllipseSchema,
        id: String,
        timestamp: Long,
        node: Ellipse
    ) {
        node.centerX = ellipse.x
        node.centerY = ellipse.y
        node.radiusX = ellipse.radiusX
        node.radiusY = ellipse.radiusY
        node.fill =
            if (ellipse.fill != null) Paint.valueOf(ellipse.fill) else null
        node.stroke = Paint.valueOf(ellipse.stroke)
        node.userData = NodeData(EntityIndex.ELLIPSE, id, timestamp)
    }

    private fun deserializeText(
        text: AppTextSchema,
        id: String,
        timestamp: Long,
        node: AppTextEditor
    ) {
        node.translateX = text.translateX
        node.translateY = text.translateY
        node.prefWidth = text.defWidth
        node.prefHeight = text.defHeight
        node.htmlText = text.htmlText
        node.userData = NodeData(EntityIndex.TEXT, id, timestamp)
    }

    private fun deserializeSegment(
        segment: AppSegmentSchema,
        id: String,
        timestamp: Long,
        node: Line
    ) {
        node.strokeLineCap = StrokeLineCap.ROUND
        node.stroke = Paint.valueOf(segment.stroke)
        node.strokeWidth = segment.width
        node.startX = segment.startX
        node.startY = segment.startY
        node.endX = segment.endX
        node.endY = segment.endY
        node.userData = NodeData(EntityIndex.SEGMENT, id, timestamp)
    }

    fun broadcastAdd(nodes: List<Node>) {
        broadcast(nodes, OperationIndex.ADD)
    }

    fun broadcastDelete(nodes: List<Node>) {
        broadcast(nodes, OperationIndex.DELETE)
    }

    fun broadcastModify(nodes: List<Node>) {
        broadcast(nodes, OperationIndex.MODIFY)
    }

    fun broadcastUndo() {
        broadcast(listOf(), OperationIndex.UNDO)
    }

    fun broadcastRedo() {
        broadcast(listOf(), OperationIndex.REDO)
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

    private fun broadcast(nodes: List<Node>, operation: Int) {
        val entities = mutableListOf<AppEntitySchema>()
        for (node in nodes) {
            val nodeData = node.userData as NodeData
            entities.add(
                AppEntitySchema(
                    nodeData.id,
                    123,
                    serializeSingle(node),
                    nodeData.type,
                    nodeData.timestamp
                )
            )
        }
        val data = Json.encodeToString(
            AppEntitiesSchema.serializer(),
            AppEntitiesSchema(entities, operation, UndoIndex.NONE)
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

    fun deserializeSingle(
        entity: AppEntitySchema,
        id: String?,
        timestamp: Long?,
        node: Node?
    ): Node {
        val nodeId = id ?: generateNodeId()
        val nodeTimestamp = timestamp ?: System.currentTimeMillis()
        when (entity.type) {
            EntityIndex.LINE -> {
                val target = if (node == null) Polyline() else node as Polyline
                deserializeLine(
                    Json.decodeFromString(
                        AppLineSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp, target
                )
                return target
            }

            EntityIndex.RECTANGLE -> {
                val target = if (node == null) Rectangle() else node as Rectangle
                deserializeRectangle(
                    Json.decodeFromString(
                        AppRectangleSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp, target
                )
                return target
            }

            EntityIndex.ELLIPSE -> {
                val target = if (node == null) Ellipse() else node as Ellipse
                deserializeEllipse(
                    Json.decodeFromString(
                        AppEllipseSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp, target
                )
                return target
            }

            EntityIndex.TEXT -> {
                val target =
                    if (node == null) AppTextEditor() else node as AppTextEditor
                deserializeText(
                    Json.decodeFromString(
                        AppTextSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp, target
                )
                return target
            }

            EntityIndex.SEGMENT -> {
                val target = if (node == null) Line() else node as Line
                deserializeSegment(
                    Json.decodeFromString(
                        AppSegmentSchema.serializer(),
                        entity.descriptor
                    ), nodeId, nodeTimestamp, target
                )
                return target
            }
        }
        println("Cannot deserialize unknown object.")
        return Line()
    }
}