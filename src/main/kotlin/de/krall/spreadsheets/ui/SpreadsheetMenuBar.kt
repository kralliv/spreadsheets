package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.event.KeyStroke
import fernice.reflare.light.FMenu
import java.awt.event.ActionEvent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class SpreadsheetMenuBar(val spreadsheetManager: SpreadsheetManager, val window: SpreadsheetWindow?) : JMenuBar() {

    init {
        val fileMenu = FMenu("File")
        add(fileMenu)

        val newSpreadsheetItem = JMenuItem("New")
        newSpreadsheetItem.accelerator = KeyStroke("ctrl N", macos = "cmd N")
        newSpreadsheetItem.addActionListener {
            spreadsheetManager.createSpreadsheet()
        }
        fileMenu.add(newSpreadsheetItem)

        val openSpreadsheetItem = JMenuItem("Open...")
        openSpreadsheetItem.accelerator = KeyStroke("ctrl O", macos = "cmd O")
        openSpreadsheetItem.addActionListener {
            spreadsheetManager.chooseSpreadsheet(this)
        }
        fileMenu.add(openSpreadsheetItem)

        if (window != null) {
            fileMenu.addSeparator()

            val saveItem = JMenuItem("Save...")
            saveItem.accelerator = KeyStroke("ctrl S", macos = "cmd S")
            saveItem.addActionListener {
                window.save()
            }
            fileMenu.add(saveItem)

            val saveAsItem = JMenuItem("Save as...")
            saveAsItem.accelerator = KeyStroke("ctrl shift S", macos = "cmd shift S")
            saveAsItem.addActionListener {
                window.saveAs()
            }
            fileMenu.add(saveAsItem)
        }
    }

    init {
        if (window != null) {
            val editMenu = JMenu("Edit")
            add(editMenu)

            val copyItem = JMenuItem("Copy")
            copyItem.accelerator = KeyStroke("ctrl C", macos = "cmd C")
            copyItem.addActionListener { event ->
                val action = window.table.actionMap.get("copy")
                action.actionPerformed(ActionEvent(window.table, ActionEvent.ACTION_PERFORMED, "copy", event.modifiers.toLong(), event.`when`.toInt()))
            }
            editMenu.add(copyItem)

            val cutItem = JMenuItem("Cut")
            cutItem.accelerator = KeyStroke("ctrl X", macos = "cmd X")
            cutItem.addActionListener { event ->
                val action = window.table.actionMap.get("cut")
                action.actionPerformed(ActionEvent(window.table, ActionEvent.ACTION_PERFORMED, "cut", event.modifiers.toLong(), event.`when`.toInt()))
            }
            editMenu.add(cutItem)

            val pasteItem = JMenuItem("Paste")
            pasteItem.accelerator = KeyStroke("ctrl V", macos = "cmd V")
            pasteItem.addActionListener { event ->
                val action = window.table.actionMap.get("paste")
                action.actionPerformed(ActionEvent(window.table, ActionEvent.ACTION_PERFORMED, "paste", event.modifiers.toLong(), event.`when`.toInt()))
            }
            editMenu.add(pasteItem)
        }
    }
}