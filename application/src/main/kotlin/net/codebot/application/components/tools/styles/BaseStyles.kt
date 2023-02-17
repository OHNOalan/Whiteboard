package net.codebot.application.components.tools.styles

import javafx.scene.Node
import net.codebot.application.components.AppStylebar

abstract class BaseStyles(private val styleBar: AppStylebar) {
    protected val controls: MutableList<Node> = mutableListOf()

    fun create() {
        for (control in controls) {
            styleBar.children.add(control)
        }
    }

    fun destroy() {
        for (control in controls) {
            styleBar.children.remove(control)
        }
    }
}