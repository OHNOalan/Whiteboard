package net.codebot.application.components

import javafx.beans.value.ChangeListener
import javafx.scene.web.HTMLEditor

class AppTextEditor() : HTMLEditor() {
    var previousTranslateX: Double? = null
    var previousTranslateY: Double? = null
    private lateinit var previousText: String

    init {
        val nodes = this.lookupAll(".tool-bar")
        for (node in nodes) {
            node.isVisible = false
            node.isManaged = false
        }
        this.isVisible = true
        this.isDisable = true
        this.style = "-fx-background-color: transparent;"
        this.htmlText = "Write something!"

        val editable = this.lookup(".web-view")
        val listener = ChangeListener<Boolean> { _, _, newValue ->
            if (newValue) {
                // HTMLEditor has gained focus
                previousText = this.htmlText
                showToolBar()
            } else {
                // HTMLEditor has lost focus
                hideToolBar()
                if (previousText != this.htmlText) {
                    AppData.broadcastModify(listOf(this))
                }
            }
        }
        editable.focusedProperty().addListener(listener)
    }

    private fun showToolBar() {
        val toolBar = this.lookupAll(".tool-bar")
        for (node in toolBar) {
            node.isVisible = true
            if (prefWidth >= 200.0 && prefHeight >= 100.0) node.isManaged = true
            else {
                node.translateY = prefHeight
                node.translateX = 20.0
            }
        }
        val scrollBar = this.lookupAll(".scroll-bar")
        for (node in scrollBar) {
            node.isVisible = true
            node.isManaged = true
        }
        this.isVisible = true
    }

    private fun hideToolBar() {
        val toolBar = this.lookupAll(".tool-bar")
        for (node in toolBar) {
            node.isVisible = false
            if (prefWidth >= 200.0 && prefHeight >= 100.0) node.isManaged = false
            else {
                node.translateY = -1 * prefHeight
                node.translateX = -20.0
            }
        }
        val scrollBar = this.lookupAll(".scroll-bar")
        for (node in scrollBar) {
            node.isVisible = false
            node.isManaged = false
        }
        this.isVisible = true
    }
}
