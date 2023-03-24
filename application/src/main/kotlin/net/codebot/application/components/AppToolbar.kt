package net.codebot.application.components

import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import net.codebot.application.components.tools.*

class AppToolbar(sideBar: VBox, canvas: AppCanvas, stylebar: AppStylebar) {
    init {
        val lineOne = HBox()
        lineOne.spacing = 10.0
        lineOne.children.add(AppUtils.createHSpacer())
        val penTool = PenTool(lineOne, stylebar)
        canvas.registerTool(penTool)
        lineOne.children.add(AppUtils.createHSpacer())
        canvas.registerTool(EraserTool(lineOne, stylebar))
        lineOne.children.add(AppUtils.createHSpacer())

        val lineTwo = HBox()
        lineTwo.spacing = 10.0
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(TextTool(lineTwo))
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(ShapeTool(lineTwo, stylebar))
        lineTwo.children.add(AppUtils.createHSpacer())

        val lineThree = HBox()
        lineThree.spacing = 10.0
        lineThree.children.add(AppUtils.createHSpacer())
        val selectTool = SelectionTool(lineThree, stylebar)
        canvas.registerTool(selectTool)
        lineThree.children.add(AppUtils.createHSpacer())

        val toolsContainer = VBox()
        toolsContainer.padding = Insets(0.0, 20.0, 0.0, 20.0)
        toolsContainer.spacing = 20.0
        toolsContainer.children.addAll(AppUtils.createVSpacer(), lineOne, lineTwo, lineThree, AppUtils.createVSpacer())
        sideBar.children.add(toolsContainer)

        // The default tool is the pen tool, so we select it
        penTool.selectTool()
    }
}