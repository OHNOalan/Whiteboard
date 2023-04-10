package net.codebot.application.components.tools.styles

import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.stage.Stage
import junit.framework.TestCase.*
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.tools.EraserTool
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EraserStylesTest {
    private lateinit var stage: Stage
    private lateinit var mockHBox: HBox
    private lateinit var mockEraserTool: EraserTool
    private lateinit var mockStylebar: AppStylebar
    private lateinit var eraserStyles: EraserStyles

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
        mockHBox = HBox()
        mockStylebar = AppStylebar()
        mockEraserTool = EraserTool(mockHBox, mockStylebar)
        eraserStyles = EraserStyles(mockStylebar, mockEraserTool)
    }

    @Test
    fun create() {
        eraserStyles.create()
        assertFalse(mockStylebar.children.isEmpty())
        assertEquals(1, mockStylebar.children.size)
    }

    @Test
    fun destroy() {
        eraserStyles.create()
        eraserStyles.destroy()
        assertTrue(mockStylebar.children.isEmpty())
    }
}