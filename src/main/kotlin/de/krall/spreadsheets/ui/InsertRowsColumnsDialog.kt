package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.components.SIntField
import de.krall.spreadsheets.ui.components.SLabel
import de.krall.spreadsheets.ui.dialog.DialogTemplate
import de.krall.spreadsheets.ui.dialog.SResultDialogContent
import de.krall.spreadsheets.ui.layout.HorizontalLayout

class InsertRowsColumnsDialog(axis: Axis) : SResultDialogContent<Int>() {

    enum class Axis {
        Row,
        Column,
    }

    private val numberField: SIntField

    init {
        title = when (axis) {
            Axis.Row -> "Insert rows"
            Axis.Column -> "Insert columns"
        }
        template = DialogTemplate.INSERT_CANCEL
        layout = HorizontalLayout(5)

        val messageLabel = SLabel()
        messageLabel.text = when (axis) {
            Axis.Row -> "Number of rows to add:"
            Axis.Column -> "Number of columns to add:"
        }
        add(messageLabel)

        numberField = SIntField()
        numberField.columns = 5
        numberField.value = when (axis) {
            Axis.Row -> 100
            Axis.Column -> 10
        }
        add(numberField)

        numberField.addPropertyChangeListener("value") { updateActionState() }
        updateActionState()
    }

    private fun updateActionState() {
        val value = numberField.value
        positiveAction?.isEnabled = value != null && value in 0..1000
    }

    override fun doPositiveAction() {
        val number = numberField.value ?: return
        closeWithResult(number)
    }
}
