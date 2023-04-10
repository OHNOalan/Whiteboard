package net.codebot.application.components.tools.styles

import javafx.application.Platform
import javafx.scene.layout.HBox
import javafx.stage.Stage
import junit.framework.TestCase.*
import net.codebot.application.components.AppStylebar
import net.codebot.application.components.tools.ShapeTool
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ShapeStylesTest {
    private lateinit var stage: Stage
    private lateinit var mockHBox: HBox
    private lateinit var mockShapeTool: ShapeTool
    private lateinit var mockStylebar: AppStylebar
    private lateinit var shapeStyles: ShapeStyles

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
        mockShapeTool = ShapeTool(mockHBox, mockStylebar)
        shapeStyles = ShapeStyles(mockStylebar, mockShapeTool)
    }

    @Test
    fun create() {
        shapeStyles.create()
        assertFalse(mockStylebar.children.isEmpty())
        assertEquals(3, mockStylebar.children.size)
    }

    @Test
    fun destroy() {
        shapeStyles.create()
        shapeStyles.destroy()
        assertTrue(mockStylebar.children.isEmpty())
    }
}