package de.krall.spreadsheets.ui.action

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon

class SAction(
    title: String? = null,
    icon: Icon? = null,
    private val actionListener: ActionListener? = null,
) : AbstractAction(title, icon) {

    override fun actionPerformed(e: ActionEvent) {
        actionListener?.actionPerformed(e)
    }

    companion object {
        const val DEFAULT_ACTION = "defaultAction"
    }
}

var Action.isDefaultAction: Boolean
    get() = getValue(SAction.DEFAULT_ACTION) == true
    set(defaultAction) {
        putValue(SAction.DEFAULT_ACTION, if (defaultAction) true else null)
    }
