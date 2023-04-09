package net.codebot.application.components.tools.styles

import javafx.scene.Node
import net.codebot.application.components.AppStylebar

/**
 * The base style which all styles inherit from.
 * @property styleBar The style bar to add the styles to.
 */
abstract class BaseStyles(private val styleBar: AppStylebar) {
    protected val controls: MutableList<Node> = mutableListOf()

    /**
     * Adds all the styles to the style bar.
     */
    fun create() {
        for (control in controls) {
            styleBar.children.add(control)
        }
    }

    /**
     * Removes all the styles from the style bar.
     */
    fun destroy() {
        for (control in controls) {
            styleBar.children.remove(control)
        }
    }
}