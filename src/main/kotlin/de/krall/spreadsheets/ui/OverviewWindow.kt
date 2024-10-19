package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.components.SItemButton
import de.krall.spreadsheets.ui.components.SLabel
import de.krall.spreadsheets.ui.icon.Icons
import de.krall.spreadsheets.ui.layout.VerticalLayout
import fernice.reflare.style
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingConstants

class OverviewWindow(val spreadsheetManager: SpreadsheetManager) : JFrame() {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Spreadsheets"
        isResizable = false

        if (OS.isMac) {
            jMenuBar = SpreadsheetMenuBar(spreadsheetManager, window = null)
        }

        val container = SContainer()
        container.style = "padding: 20px"
        container.layout = VerticalLayout(10)

        val createButton = SItemButton()
        createButton.icon = Icons.Actions.Add
        createButton.text = "Create Spreadsheet"
        container.add(createButton)

        val orLabel = SLabel()
        orLabel.text = "or"
        orLabel.horizontalAlignment = SwingConstants.CENTER
        container.add(orLabel)

        val openButton = SItemButton()
        openButton.icon = Icons.Actions.Open
        openButton.text = "Open Spreadsheet"
        container.add(openButton)

        contentPane = container

        createButton.addActionListener {
            spreadsheetManager.createSpreadsheet()
        }
        openButton.addActionListener {
            spreadsheetManager.chooseSpreadsheet(this)
        }

        pack()
        size = Dimension(300, height)
        setLocationRelativeTo(null)
    }
}
