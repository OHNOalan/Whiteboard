package net.codebot.application.components.tools.styles

import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.stage.Stage
import junit.framework.TestCase.*
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.tools.PenTool
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PenStylesTest {
    private lateinit var stage: Stage
    private lateinit var mockHBox: HBox
    private lateinit var mockPenTool: PenTool
    private lateinit var mockStylebar: AppStylebar
    private lateinit var penStyles: PenStyles

    // We need to initialize the Toolkit once and only once.
    companion object {
        @JvmStatic
        private val latch = CountDownLatch(1)

        @BeforeClass
        @JvmStatic
        fun initToolkit() {
            Thread {
                Platform.startup {}
                latch.countDown()
                Platform.runLater {}
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
        mockPenTool = PenTool(mockHBox, mockStylebar)
        penStyles = PenStyles(mockStylebar, mockPenTool)
    }

    @Test
    fun create() {
        penStyles.create()
        assertFalse(mockStylebar.children.isEmpty())
        assertEquals(2, mockStylebar.children.size)
    }

    @Test
    fun destroy() {
        penStyles.create()
        penStyles.destroy()
        assertTrue(mockStylebar.children.isEmpty())
    }
}