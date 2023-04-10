package net.codebot.application.components

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.shape.Polyline
import javafx.stage.Stage
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AppDataTest {
    private lateinit var stage: Stage
    private lateinit var mockBorderPane: BorderPane
    private lateinit var appData: AppData
    private lateinit var mockNode: Node
    private lateinit var mockNodeList: MutableList<Node>
    private lateinit var mockAppLayout: AppLayout

    private val mockNodeSaveData =
        "{\"stroke\":\"0x000000ff\",\"strokeWidth\":1.0,\"points\":[]}"
    private val mockNodeListSaveData =
        "{\"entities\":[{\"id\":\"\",\"roomId\":0,\"descriptor\":\"{\\\"stroke\\\":\\\"0x000000ff\\\",\\\"strokeWidth\\\":1.0,\\\"points\\\":[]}\",\"previousDescriptor\":null,\"type\":\"LINE\",\"timestamp\":0},{\"id\":\"\",\"roomId\":0,\"descriptor\":\"{\\\"stroke\\\":\\\"0x000000ff\\\",\\\"strokeWidth\\\":1.0,\\\"points\\\":[]}\",\"previousDescriptor\":null,\"type\":\"LINE\",\"timestamp\":0},{\"id\":\"\",\"roomId\":0,\"descriptor\":\"{\\\"stroke\\\":\\\"0x000000ff\\\",\\\"strokeWidth\\\":1.0,\\\"points\\\":[]}\",\"previousDescriptor\":null,\"type\":\"LINE\",\"timestamp\":0}],\"operation\":1,\"undoState\":0}"

    // We need to initialize the Toolkit once and only once.
    companion object {
        @JvmStatic
        private val latch = CountDownLatch(1)

        @BeforeClass
        @JvmStatic
        fun initToolkit() {
            Thread {
                Platform.startup{}
                latch.countDown()
                Platform.runLater{}
            }.start()
            // wait for initialization to finish
            latch.await(1, TimeUnit.SECONDS)
        }
    }

    @Before
    fun setup() {
        Platform.runLater {
            stage = Stage()
            mockAppLayout = AppLayout(stage)
        }
        mockBorderPane = BorderPane()
        appData = AppData
        mockNode = Polyline()
        mockNode.userData = NodeData(
            EntityIndex.LINE, "qwerty", 123456789
        )

        mockNodeList = mutableListOf()
        mockNodeList.add(mockNode)
        mockNodeList.add(mockNode)
        mockNodeList.add(mockNode)
    }

    @Test
    fun serializeSingle() {
        val nodeData = appData.serializeSingle(mockNode)
        assertEquals(mockNodeSaveData, nodeData)
    }

    @Test
    fun serializeLocal() {
        val nodeData = appData.serializeLocal(mockNodeList)
        assertEquals(mockNodeListSaveData, nodeData)
    }

    @Test
    fun deserializeLocal() {
        // Need to wait for mockAppLayout to finish initialization
        Platform.runLater {
            appData.registerAppLayout(mockAppLayout)
            val newNodes = appData.deserializeLocal(mockNodeListSaveData)
            assertEquals(mockNodeList, newNodes)
        }
    }
}
