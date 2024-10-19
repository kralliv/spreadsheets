package de.krall.spreadsheets.ui.dialog

import de.krall.spreadsheets.ui.components.SLabel
import java.awt.BorderLayout

class SMessageDialog(
    title: String,
    message: String,
    template: DialogTemplate = DialogTemplate.OK,
) : SDialogContent() {

    init {
        this.title = title
        this.template = template
        layout = BorderLayout()
        fixedWidth = 450

        val messageLabel = SLabel()
        messageLabel.text = "<html><body>$message</body></html>"
        add(messageLabel)
    }
}
