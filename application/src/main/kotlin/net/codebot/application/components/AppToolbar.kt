package net.codebot.application.components

import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import net.codebot.application.components.tools.*

class AppToolbar(sideBar: VBox, canvas: AppCanvas, styleBar: AppStylebar) {
    init {
        val lineOne = HBox()
        lineOne.spacing = 10.0
        lineOne.children.add(AppUtils.createHSpacer())
        val penTool = PenTool(lineOne, styleBar)
        canvas.registerTool(penTool)
        lineOne.children.add(AppUtils.createHSpacer())
        canvas.registerTool(EraserTool(lineOne, styleBar))
        lineOne.children.add(AppUtils.createHSpacer())

        val lineTwo = HBox()
        lineTwo.spacing = 10.0
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(TextTool(lineTwo, styleBar))
        lineTwo.children.add(AppUtils.createHSpacer())
        canvas.registerTool(ShapeTool(lineTwo, styleBar))
        lineTwo.children.add(AppUtils.createHSpacer())

        val lineThree = HBox()
        lineThree.spacing = 10.0
        lineThree.children.add(AppUtils.createHSpacer())
        canvas.registerTool(LineTool(lineThree, styleBar))
        lineThree.children.add(AppUtils.createHSpacer())
        canvas.registerTool(SelectTool(lineThree, styleBar))
        lineThree.children.add(AppUtils.createHSpacer())

        val toolsContainer = VBox()
        toolsContainer.padding = Insets(0.0, 20.0, 0.0, 20.0)
        toolsContainer.spacing = 20.0
        toolsContainer.children.addAll(
            AppUtils.createVSpacer(),
            lineOne,
            lineTwo,
            lineThree,
            AppUtils.createVSpacer()
        )
        sideBar.children.add(toolsContainer)

        // The default tool is the pen tool, so we select it
        penTool.selectTool()
    }
}
