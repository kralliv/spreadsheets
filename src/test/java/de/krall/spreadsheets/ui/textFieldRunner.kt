package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.value.parser.ValueParser
import fernice.reflare.FlareLookAndFeel
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.text.DefaultEditorKit

fun main() {
    FlareLookAndFeel.install()

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

    frame.contentPane = container

    frame.isVisible = true


}
