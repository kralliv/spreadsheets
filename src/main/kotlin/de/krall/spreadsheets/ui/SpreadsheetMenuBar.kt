package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.event.KeyStroke
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class SpreadsheetMenuBar(val spreadsheetManager: SpreadsheetManager, val window: SpreadsheetWindow?) : JMenuBar() {

    init {
        val fileMenu = JMenu("File")
        add(fileMenu)

        val newSpreadsheetItem = JMenuItem("New Spreadsheet")
        newSpreadsheetItem.accelerator = KeyStroke("ctrl N", macos = "cmd N")
        newSpreadsheetItem.addActionListener {
            spreadsheetManager.createSpreadsheet()
        }
        fileMenu.add(newSpreadsheetItem)

        val openSpreadsheetItem = JMenuItem("Open Spreadsheet...")
        openSpreadsheetItem.accelerator = KeyStroke("ctrl O", macos = "cmd O")
        openSpreadsheetItem.addActionListener {
            spreadsheetManager.chooseSpreadsheet(this)
        }
        fileMenu.add(openSpreadsheetItem)

        if (window != null) {
            val closeItem = JMenuItem("Close Spreadsheet")
            closeItem.accelerator = KeyStroke("ctrl W", macos = "cmd W")
            closeItem.addActionListener {
                window.close()
            }
            fileMenu.add(closeItem)

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

        if (window != null) {
            val editMenu = JMenu("Edit")
            add(editMenu)

            val copyItem = JMenuItem("Copy")
            copyItem.accelerator = KeyStroke("ctrl C", macos = "cmd C")
            copyItem.addActionListener { event ->
                window.table.invokeAction("copy", event)
            }
            editMenu.add(copyItem)

            val cutItem = JMenuItem("Cut")
            cutItem.accelerator = KeyStroke("ctrl X", macos = "cmd X")
            cutItem.addActionListener { event ->
                window.table.invokeAction("cut", event)
            }
            editMenu.add(cutItem)

            val pasteItem = JMenuItem("Paste")
            pasteItem.accelerator = KeyStroke("ctrl V", macos = "cmd V")
            pasteItem.addActionListener { event ->
                window.table.invokeAction("paste", event)
            }
            editMenu.add(pasteItem)

            editMenu.addSeparator()

            val undoItem = JMenuItem("Undo")
            undoItem.accelerator = KeyStroke("ctrl Z", macos = "cmd Z")
            undoItem.addActionListener { event ->
                window.table.undo()
            }
            editMenu.add(undoItem)

            val redoItem = JMenuItem("Redo")
            redoItem.accelerator = KeyStroke("ctrl shift Z", macos = "cmd shift Z")
            redoItem.addActionListener { event ->
                window.table.redo()
            }
            editMenu.add(redoItem)
        }

        if (window != null) {
            val insertMenu = JMenu("Insert")
            add(insertMenu)

            val insertColumnsItem = JMenuItem("Blank Columns...")
            insertColumnsItem.addActionListener {
                val number = InsertRowsColumnsDialog(InsertRowsColumnsDialog.Axis.Column).showAndGet(this)
                if (number != null) {
                    window.table.addColumns(number)
                }
            }
            insertMenu.add(insertColumnsItem)

            val insertRowsItem = JMenuItem("Blank Rows...")
            insertRowsItem.addActionListener {
                val number = InsertRowsColumnsDialog(InsertRowsColumnsDialog.Axis.Row).showAndGet(this)
                if (number != null) {
                    window.table.addRows(number)
                }
            }
            insertMenu.add(insertRowsItem)
        }
    }

    private fun Component.invokeAction(name: String, event: ActionEvent) {
        val action = actionMap.get(name)
        action?.actionPerformed(event.derive(component, name))
    }

    private fun ActionEvent.derive(source: Any, command: String): ActionEvent {
        return ActionEvent(source, ActionEvent.ACTION_PERFORMED, command, modifiers.toLong(), `when`.toInt())
    }
}
