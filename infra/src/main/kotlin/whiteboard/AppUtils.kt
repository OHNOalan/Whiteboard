package whiteboard

import kotlinx.serialization.json.Json
import java.security.MessageDigest


object AppUtils {
    fun hashString(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getSecret(): String {
        return "1278489we7f6sd4f8x4e"
    }

    fun generateResponse(
        success: Boolean,
        message: String,
        roomCode: String = ""
    ): String {
        return Json.encodeToString(
            AppResponseSchema.serializer(),
            AppResponseSchema(success, message, roomCode)
        )
    }
}