package de.krall.spreadsheets.ui

import de.krall.spreadsheets.sheet.value.parser.ValueParser
import de.krall.spreadsheets.ui.components.SButton
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.WindowConstants

fun main() {
    initializeGraphicalEnvironment()

    val frame = JFrame()
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.size = Dimension(500, 500)
    frame.setLocationRelativeTo(null)

    val container = JPanel()

    val valueField = ValueField(ValueParser())
    container.add(valueField)

    val textField = JTextField()
    textField.columns = 10
    container.add(textField)

    val button = SButton(SButton.Precedence.HIGH)
    button.text = "Test"
    button.isEnabled = false
    container.add(button)

    frame.contentPane = container

    frame.isVisible = true


}
