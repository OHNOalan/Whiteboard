package net.codebot.application.components


import io.ktor.websocket.*
import javafx.scene.Node
import javafx.scene.paint.Paint
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Line
import javafx.scene.shape.Polyline
import javafx.scene.shape.Rectangle
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class AppLine(val stroke: String, val strokeWidth: Double, val points: List<Double>)

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
data class AppEntities(val lines: List<AppLine>, val rectangles: List<AppRectangle>, val ellipses: List<AppEllipse>, val texts: List<AppText>, val segments: List<AppSegment>)

@Serializable
data class AppResponseEntity(
    val id: String,
    val roomId: Int,
    val descriptor: String,
    val type: String,
    val timestamp: Long
)

@Serializable
data class AppResponse(val entities: List<AppResponseEntity>, val operation: Int)

data class NodeData(val type: String, val id: String)

object AppData {
    private var counter = 0
    private lateinit var socket: DefaultWebSocketSession
    private lateinit var appLayout: AppLayout

    fun serializeSingle(node: Node): String {
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
                val segment  = node as Line
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

    fun serialize(nodes: List<Node>): AppEntities {
        val lines = mutableListOf<AppLine>()
        val rectangles = mutableListOf<AppRectangle>()
        val ellipses = mutableListOf<AppEllipse>()
        val texts = mutableListOf<AppText>()
        val segments = mutableListOf<AppSegment>()
        for (node in nodes) {
            when ((node.userData as NodeData).type) {
                EntityIndex.LINE -> {
                    val line = node as Polyline
                    lines.add(AppLine(line.stroke.toString(), line.strokeWidth, line.points))
                }

                EntityIndex.RECTANGLE -> {
                    val rectangle = node as Rectangle
                    rectangles.add(
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
                    ellipses.add(
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
                    texts.add(
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
                    segments.add(
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
        }
        return AppEntities(lines, rectangles, ellipses, texts, segments)
    }

    fun deserialize(data: String): List<Node> {
        val nodes = Json.decodeFromString(AppEntities.serializer(), data)
        val decoded = mutableListOf<Node>()
        for (line in nodes.lines) {
            decoded.add(deserializeLine(line, generateNodeId()))
        }
        for (rectangle in nodes.rectangles) {
            decoded.add(deserializeRectangle(rectangle, generateNodeId()))
        }
        for (ellipse in nodes.ellipses) {
            decoded.add(deserializeEllipse(ellipse, generateNodeId()))
        }
        for (text in nodes.texts) {
            decoded.add(deserializeText(text, generateNodeId()))
        }
        for (segment in nodes.segments) {
            decoded.add(deserializeSegment(segment, generateNodeId()))
        }
        return decoded
    }

    fun deserializeLine(line: AppLine, id: String): Node {
        val decodedLine = Polyline()
        decodedLine.stroke = Paint.valueOf(line.stroke)
        decodedLine.strokeWidth = line.strokeWidth
        decodedLine.points.addAll(line.points)
        decodedLine.userData = NodeData(EntityIndex.LINE, id)
        return decodedLine
    }

    fun deserializeRectangle(rectangle: AppRectangle, id: String): Node {
        val decodedRectangle = Rectangle()
        decodedRectangle.translateX = rectangle.x
        decodedRectangle.translateY = rectangle.y
        decodedRectangle.width = rectangle.width
        decodedRectangle.height = rectangle.height
        decodedRectangle.fill = if (rectangle.fill != null) Paint.valueOf(rectangle.fill) else null
        decodedRectangle.stroke = Paint.valueOf(rectangle.stroke)
        decodedRectangle.userData = NodeData(EntityIndex.RECTANGLE, id)
        return decodedRectangle
    }

    fun deserializeEllipse(ellipse: AppEllipse, id: String): Node {
        val decodedEllipse = Ellipse()
        decodedEllipse.centerX = ellipse.x
        decodedEllipse.centerY = ellipse.y
        decodedEllipse.radiusX = ellipse.radiusX
        decodedEllipse.radiusY = ellipse.radiusY
        decodedEllipse.fill = if (ellipse.fill != null) Paint.valueOf(ellipse.fill) else null
        decodedEllipse.stroke = Paint.valueOf(ellipse.stroke)
        decodedEllipse.userData = NodeData(EntityIndex.ELLIPSE, id)
        return decodedEllipse
    }

    fun deserializeText(text: AppText, id: String): Node {
        val decodedText = AppTextEditor(text.translateX, text.translateY, text.defWidth, text.defHeight)
        decodedText.htmlText = text.htmlText
        decodedText.userData = NodeData(EntityIndex.TEXT, id)
        return decodedText
    }

    fun deserializeSegment(segment: AppSegment, id: String): Node {
        val decodedSegment = Line()
        decodedSegment.stroke = Paint.valueOf(segment.stroke)
        decodedSegment.strokeWidth = segment.width
        decodedSegment.startX = segment.startX
        decodedSegment.startY = segment.startY
        decodedSegment.endX = segment.endX
        decodedSegment.endY = segment.endY
        decodedSegment.userData = NodeData(EntityIndex.SEGMENT, id)
        return decodedSegment
    }

    fun broadcastCreate(nodes: List<Node>) {
        val responseEntities = mutableListOf<AppResponseEntity>()
        for (node in nodes) {
            val nodeData = node.userData as NodeData
            responseEntities.add(
                AppResponseEntity(
                    nodeData.id,
                    123,
                    serializeSingle(node),
                    nodeData.type,
                    System.currentTimeMillis()
                )
            )
        }
        runBlocking {
            launch {
                val data = Json.encodeToString(
                    AppResponse.serializer(),
                    AppResponse(responseEntities, OperationIndex.ADD)
                )
                println("Created:")
                println(data)
                socket.send(data)
            }
        }
    }

    fun broadcastDelete(nodes: List<Node>) {
        val responseEntities = mutableListOf<AppResponseEntity>()
        for (node in nodes) {
            val nodeData = node.userData as NodeData
            responseEntities.add(
                AppResponseEntity(
                    nodeData.id,
                    123,
                    "",
                    nodeData.type,
                    System.currentTimeMillis()
                )
            )
        }
        runBlocking {
            launch {
                val data = Json.encodeToString(
                    AppResponse.serializer(),
                    AppResponse(responseEntities, OperationIndex.DELETE)
                )
                println("Deleted:")
                println(data)
                socket.send(data)
            }
        }
    }

    fun broadcastModify(nodes: List<Node>) {
        val responseEntities = mutableListOf<AppResponseEntity>()
        for (node in nodes) {
            val nodeData = node.userData as NodeData
            responseEntities.add(
                AppResponseEntity(
                    nodeData.id,
                    123,
                    serializeSingle(node),
                    nodeData.type,
                    System.currentTimeMillis()
                )
            )
        }
        runBlocking {
            launch {
                val data = Json.encodeToString(
                    AppResponse.serializer(),
                    AppResponse(responseEntities, OperationIndex.MODIFY)
                )
                println("Modify:")
                println(data)
                socket.send(data)
            }
        }
    }

    fun generateNodeId(): String {
        val id = appLayout.getUsername() + "/" + System.currentTimeMillis().toString() + "-" + counter.toString()
        counter += 1
        return id
    }

    fun registerSocket(session: DefaultWebSocketSession) {
        socket = session
    }

    fun registerAppLayout(appLayoutInstance: AppLayout) {
        appLayout = appLayoutInstance
    }
}