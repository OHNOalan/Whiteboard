package net.codebot.application.components.tools

import javafx.scene.control.TextArea
import javafx.scene.transform.Translate
class Text(var posx: Double = 0.0, var posy: Double = 0.0, var text: String = "Text") {
    public val teatarea: TextArea = TextArea(text)
    init {
//        teatarea.translateX(posx)
//        textarea.translateY(posy)
    }
    fun getX() : Double = posx
    fun getY() : Double= posy
//    fun trans() : TextArea {
//        text.translateX(posx)
//        text.translateX(posy)
//    }
}