package net.codebot.application.components

import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox


class AppUtils {
    companion object {
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
    }
}