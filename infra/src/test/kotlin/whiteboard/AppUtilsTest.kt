package whiteboard

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AppUtilsTest {
    private val appUtils = AppUtils
    private val message = "This is a message"
    private val roomCode = "ABCD1234"
    private val mockResponse =
        "{\"success\":true,\"message\":\"This is a message\",\"roomCode\":\"ABCD1234\"}"

    @Test
    fun getRandomString() {
        val length = 8
        val randomString = appUtils.getRandomString(length)
        assertEquals(8, randomString.length)
        assertTrue(randomString.contains("[A-Z0-9]{8}".toRegex()))
    }

    @Test
    fun generateResponse() {
        val response = appUtils.generateResponse(
            success = true,
            message = message,
            roomCode = roomCode
        )
        assertEquals(mockResponse, response)
    }
}
