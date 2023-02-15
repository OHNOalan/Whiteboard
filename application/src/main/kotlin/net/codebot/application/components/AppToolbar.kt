package net.codebot.application.components

import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import net.codebot.application.components.tools.PenTool
import net.codebot.application.components.tools.EraserTool
import net.codebot.application.components.tools.ShapeTool
import net.codebot.application.components.tools.TextTool

class AppToolbar(sideBar: VBox, canvas: AppCanvas) {
    init {
        val lineOne = HBox()
        lineOne.spacing = 10.0
        lineOne.children.add(AppUtils.createHSpacer())
        val penTool =PenTool(lineOne)
        canvas.registerTool(penTool)
        // The default tool is the pen tool, so we select it
        penTool.selectTool()
        lineOne.children.add(AppUtils.createHSpacer())
        canvas.registerTool(EraserTool(lineOne))
        lineOne.children.add(AppUtils.createHSpacer())

        val lineTwo = HBox()
        lineTwo.spacing = 10.0
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(TextTool(lineTwo))
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(ShapeTool(lineTwo))
        lineTwo.children.add(AppUtils.createHSpacer())

        val toolsContainer = VBox()
        toolsContainer.padding = Insets(0.0, 20.0, 0.0, 20.0)
        toolsContainer.spacing = 20.0
        toolsContainer.children.addAll(AppUtils.createVSpacer(), lineOne, lineTwo, AppUtils.createVSpacer())
        sideBar.children.add(toolsContainer)
    }
}