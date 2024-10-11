package de.krall.spreadsheets.ui

import fernice.reflare.FlareLookAndFeel
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

fun main() {
    FlareLookAndFeel.install()

    val frame = JFrame()
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.size = Dimension(500, 500)
    frame.setLocationRelativeTo(null)

    val container = JPanel()

    val textfield = ValueField()
    container.add(textfield)

    frame.contentPane = container

    frame.isVisible = true


}
