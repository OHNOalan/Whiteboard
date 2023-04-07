package net.codebot.application.components

import javafx.beans.value.ChangeListener
import javafx.scene.web.HTMLEditor
import kotlin.math.max

class AppTextEditor(initX: Double, initY: Double, private var defWidth: Double, private var defHeight: Double) :
    HTMLEditor() {
    init {
        this.translateX = initX
        this.translateY = initY
        this.prefWidth = max(defWidth, 180.0)
        this.prefHeight = max(defHeight, 50.0)
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
                showToolBar()
            } else {
                // HTMLEditor has lost focus
                hideToolBar()
            }
        }
        editable.focusedProperty().addListener(listener)
    }

    private fun showToolBar() {
        val toolBar = this.lookupAll(".tool-bar")
        for (node in toolBar) {
            node.isVisible = true
            if (defWidth >= 200.0 && defHeight >= 100.0) node.isManaged = true
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
            if (defWidth >= 200.0 && defHeight >= 100.0) node.isManaged = false
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
