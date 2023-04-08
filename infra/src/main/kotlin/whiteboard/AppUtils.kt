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

    fun getSecret(): String {
        return "1278489we7f6sd4f8x4e"
    }

    fun generateResponse(success: Boolean, message: String): String {
        return Json.encodeToString(
            AppResponseSchema.serializer(),
            AppResponseSchema(success, message)
        )
    }
}