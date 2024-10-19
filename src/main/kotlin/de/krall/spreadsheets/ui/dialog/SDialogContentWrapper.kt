package de.krall.spreadsheets.ui.dialog

import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.layout.HorizontalLayout
import fernice.reflare.classes
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JButton

class SDialogContentWrapper(val dialogContent: SDialogContent) : SContainer() {

    val defaultButton: JButton?

    val defaultFocusComponent: Component?
        get() = dialogContent.defaultFocusComponent ?: defaultButton

    init {
        layout = BorderLayout(0, 10)
        classes.add("s-dialog-content-wrapper")

        add(dialogContent, BorderLayout.CENTER)

        var defaultButton: JButton? = null

        val trailingComponents = dialogContent.createTrailingComponents()
        if (trailingComponents.isNotEmpty()) {
            val actionContainer = SContainer()
            actionContainer.layout = BorderLayout()
            add(actionContainer, BorderLayout.SOUTH)

            val trailingActionContainer = SContainer()
            trailingActionContainer.layout = HorizontalLayout(4)
            actionContainer.add(trailingActionContainer, BorderLayout.LINE_END)

            for (component in trailingComponents) {
                trailingActionContainer.add(component)

                if (component is JButton && component.getClientProperty(SDialogContent.DEFAULT_BUTTON_PROPERTY) == true) {
                    defaultButton = component
                }
            }
        }

        this.defaultButton = defaultButton
    }
}
