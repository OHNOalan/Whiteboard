package net.codebot.application.components

import javafx.geometry.HPos
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection

/**
 * Utility class with functions for spacing, separations, and http requests
 */
object AppUtils {
    /**
     * Creates a horizontal spacer as a region which takes priority in growth space.
     * @return Horizontal spacer.
     */
    fun createHSpacer(): Region {
        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)
        return spacer
    }

    /**
     * Creates a vertical spacer as a region which takes priority in growth space.
     * @return Vertical spacer.
     */
    fun createVSpacer(): Region {
        val spacer = Region()
        VBox.setVgrow(spacer, Priority.ALWAYS)
        return spacer
    }

    /**
     * Creates a separator aligned in the center horizontally
     * @return Separator.
     */
    fun createSeparator(): Separator {
        val separator = Separator()
        separator.halignment = HPos.CENTER
        return separator
    }

    /**
     * Creates and sends an HTTP request to the server to log in/register account.
     *
     * @param urlRoute The route of the login page (following the host url).
     * @param urlParams The arguments of the HTTP request (username and password).
     */
    fun httpRequest(
        urlRoute: String,
        urlParams: String,
    ): AppResponseSchema {
        val urlHost = "http://${AppSettings.HOST}:${AppSettings.PORT}"
        val urlText = "$urlHost$urlRoute?$urlParams"
        val url = URL(urlText)

        val httpURLConnection: URLConnection = url.openConnection()
        httpURLConnection.doOutput = true // triggers POST
        httpURLConnection.setRequestProperty("Accept-Charset", AppSettings.CHARSET)
        httpURLConnection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded;charset=${AppSettings.CHARSET}"
        )

        try {
            httpURLConnection.getOutputStream()
                .use { output -> output.write(urlParams.encodeToByteArray()) }
            var response: AppResponseSchema
            BufferedReader(
                InputStreamReader(
                    httpURLConnection.getInputStream(),
                    AppSettings.CHARSET
                )
            ).use { reader ->
                response = Json.decodeFromString(
                    AppResponseSchema.serializer(),
                    reader.readText()
                )
            }
            return response
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return AppResponseSchema(false, "A network problem has occurred.", "")
    }
}
