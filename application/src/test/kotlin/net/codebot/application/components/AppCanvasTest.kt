package net.codebot.application.components

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.shape.Polyline
import javafx.stage.Stage
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AppCanvasTest {
    private lateinit var stage: Stage
    private lateinit var mockBorderPane: BorderPane
    private lateinit var appCanvas: AppCanvas
    private lateinit var mockNode: Node

    private val emptySaveData = "{\"entities\":[],\"operation\":1,\"undoState\":0}"
    private val mockNodeSaveData =
        "{\"entities\":[{\"id\":\"\",\"roomId\":0,\"descriptor\":\"{\\\"stroke\\\":\\\"0x000000ff\\\",\\\"strokeWidth\\\":1.0,\\\"points\\\":[]}\",\"previousDescriptor\":null,\"type\":\"LINE\",\"timestamp\":0}],\"operation\":1,\"undoState\":0}"

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
        }
        mockBorderPane = BorderPane()
        appCanvas = AppCanvas(mockBorderPane)
        mockNode = Polyline()
        mockNode.userData = NodeData(
            EntityIndex.LINE, "qwerty", 123456789
        )
    }

    @Test
    fun addDrawnNode() {
        appCanvas.addDrawnNode(mockNode, broadcast = false)
        assertFalse(appCanvas.children.isEmpty())
        assertEquals(1, appCanvas.children.size)
    }

    @Test
    fun removeDrawnNode() {
        appCanvas.addDrawnNode(mockNode, broadcast = false)
        appCanvas.removeDrawnNode(mockNode, broadcast = false)
        assertTrue(appCanvas.children.isEmpty())
        assertEquals(0, appCanvas.children.size)
    }

    @Test
    fun saveFileEmpty() {
        val saveData = appCanvas.saveFile()
        assertEquals(emptySaveData, saveData)
    }

    @Test
    fun saveFileWithNode() {
        appCanvas.addDrawnNode(mockNode, broadcast = false)
        val saveData = appCanvas.saveFile()
        assertEquals(mockNodeSaveData, saveData)
    }
}
