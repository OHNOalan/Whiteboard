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


object AppUtils {
    fun createHSpacer(): Region {
        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)
        return spacer
    }

    fun createVSpacer(): Region {
        val spacer = Region()
        VBox.setVgrow(spacer, Priority.ALWAYS)
        return spacer
    }

    fun createSeparator(): Separator {
        val separator = Separator()
        separator.halignment = HPos.CENTER
        return separator
    }

    fun httpRequest(
        urlRoute: String,
        urlParams: String
    ): AppResponseSchema {
        val urlHost = "http://127.0.0.1:8080"
        val urlText = "$urlHost$urlRoute?$urlParams"
        val url = URL(urlText)

        val httpURLConnection: URLConnection = url.openConnection()
        httpURLConnection.doOutput = true // triggers POST
        httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8")
        httpURLConnection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded;charset=UTF-8"
        )

        try {
            httpURLConnection.getOutputStream()
                .use { output -> output.write(urlParams.encodeToByteArray()) }
            var response: AppResponseSchema
            BufferedReader(
                InputStreamReader(
                    httpURLConnection.getInputStream(),
                    "UTF-8"
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