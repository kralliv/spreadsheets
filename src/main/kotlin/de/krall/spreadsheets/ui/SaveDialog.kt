package de.krall.spreadsheets.ui

import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.components.SDirectoryField
import de.krall.spreadsheets.ui.components.SSizedLabel
import de.krall.spreadsheets.ui.components.STextField
import de.krall.spreadsheets.ui.components.SizingGroup
import de.krall.spreadsheets.ui.dialog.DialogTemplate
import de.krall.spreadsheets.ui.dialog.SResultDialogContent
import de.krall.spreadsheets.ui.layout.HorizontalLayout
import de.krall.spreadsheets.ui.layout.VerticalLayout
import java.awt.Component
import java.nio.file.Path
import javax.swing.Box

class SaveDialog : SResultDialogContent<Path>() {

    private val directoryField: SDirectoryField
    private val nameField: STextField

    init {
        title = "Save to file"
        template = DialogTemplate.SAVE_CANCEL
        layout = VerticalLayout(2)

        val sizingGroup = SizingGroup()

        directoryField = SDirectoryField()
        add(labeled("Directory", directoryField, sizingGroup))

        add(Box.createVerticalStrut(10))

        nameField = STextField()
        add(labeled("Name", nameField, sizingGroup))

        directoryField.addPropertyChangeListener("value") { updateActionState() }
        nameField.addTextChangeListener { updateActionState() }
        updateActionState()
    }

    private fun labeled(label: String, component: Component, sizingGroup: SizingGroup): Component {
        val container = SContainer()
        container.layout = HorizontalLayout(5)

        val labelComponent = SSizedLabel(sizingGroup)
        labelComponent.text = "$label:"
        container.add(labelComponent)

        container.add(component)

        return container
    }

    private fun updateActionState() {
        positiveAction?.isEnabled = directoryField.value != null && nameField.text.isNotBlank()
    }

    override fun doPositiveAction() {
        val directory = directoryField.value ?: return

        var fileName = nameField.text.ifBlank { null } ?: return
        if (fileName.substringAfterLast('.', missingDelimiterValue = "").isEmpty()) {
            fileName = "$fileName.sheet"
        }

        val path = directory.resolve(fileName.trim())

        close(path)
    }

    override val defaultFocusComponent: Component?
        get() = nameField
}
