package net.codebot.application.components


import io.ktor.websocket.*
import javafx.scene.Node
import javafx.scene.paint.Paint
import javafx.scene.shape.Ellipse
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
data class AppEntities(val lines: List<AppLine>, val rectangles: List<AppRectangle>, val ellipses: List<AppEllipse>)

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
        }
        return result
    }

    fun serialize(nodes: List<Node>): AppEntities {
        val lines = mutableListOf<AppLine>()
        val rectangles = mutableListOf<AppRectangle>()
        val ellipses = mutableListOf<AppEllipse>()
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
            }
        }
        return AppEntities(lines, rectangles, ellipses)
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
        val id = "terry" + "/" + System.currentTimeMillis().toString() + "-" + counter.toString()
        counter += 1
        return id
    }

    fun registerSocket(session: DefaultWebSocketSession) {
        socket = session
    }
}