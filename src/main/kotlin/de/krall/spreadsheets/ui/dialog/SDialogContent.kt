package de.krall.spreadsheets.ui.dialog

import de.krall.spreadsheets.ui.OS
import de.krall.spreadsheets.ui.action.SAction
import de.krall.spreadsheets.ui.action.isDefaultAction
import de.krall.spreadsheets.ui.components.SButton
import de.krall.spreadsheets.ui.components.SContainer
import de.krall.spreadsheets.ui.event.Conditions
import de.krall.spreadsheets.ui.event.KeyStroke
import de.krall.spreadsheets.ui.event.registerKeyboardAction
import de.krall.spreadsheets.ui.util.carrierWindow
import de.krall.spreadsheets.ui.util.invokeLater
import de.krall.spreadsheets.ui.util.window
import fernice.reflare.util.preferredHeight
import java.awt.Component
import java.awt.Dialog.ModalityType
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.PropertyChangeListener
import javax.swing.Action
import javax.swing.JDialog
import javax.swing.WindowConstants

abstract class SDialogContent : SContainer() {

    var title: String = ""
        set(title) {
            val previousTitle = field
            if (title != previousTitle) {
                field = title

                firePropertyChange("title", previousTitle, title)
            }
        }

    var positiveAction: Action? = null
    var neutralAction: Action? = null
    var negativeAction: Action? = null

    var template: DialogTemplate? = null
        set(template) {
            field = template

            if (template != null) {
                positiveAction = template.createPositiveAction(this)
                neutralAction = template.createNeutralAction(this)
                negativeAction = template.createNegativeAction(this)
            }
        }

    fun createPrimaryActions(): List<Action> {
        if (positiveAction == null && neutralAction == null && negativeAction == null) {
            error("dialog content has no actions: consider setting a template")
        }

        if (positiveAction?.isDefaultAction != true
            && neutralAction?.isDefaultAction != true
            && negativeAction?.isDefaultAction != true
        ) {
            val defaultAction = positiveAction ?: negativeAction ?: neutralAction

            defaultAction?.isDefaultAction = true
        }

        return buildList {
            if (OS.isMac) {
                negativeAction?.let { add(it) }
                neutralAction?.let { add(it) }
                positiveAction?.let { add(it) }
            } else {
                positiveAction?.let { add(it) }
                negativeAction?.let { add(it) }
                neutralAction?.let { add(it) }
            }
        }
    }

    fun createTrailingComponents(): List<Component> {
        return buildList {
            for (action in createPrimaryActions()) {
                val component = SButton(action)
                add(component)

                if (action.isDefaultAction) {
                    component.putClientProperty(DEFAULT_BUTTON_PROPERTY, true)
                }
            }
        }
    }

    open val defaultFocusComponent: Component?
        get() = null

    var fixedWidth: Int = -1

    private var dialog: JDialog? = null
    private var dialogOutcome: Int? = null

    val outcome: Int
        get() = dialogOutcome ?: error("dialog has not been closed yet")

    fun showAndWait(invoker: Component?): Boolean {
        show(invoker)

        return outcome == POSITIVE
    }

    fun showAndRetrieve(invoker: Component?): Int {
        show(invoker)

        return outcome
    }

    fun show(invoker: Component?) {
        val owner = invoker?.window
        val carrier = owner ?: carrierWindow

        val dialogContentWrapper = SDialogContentWrapper(this)

        val dialog = JDialog(owner, ModalityType.APPLICATION_MODAL)
        dialog.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        dialog.iconImages = carrier?.iconImages
        dialog.title = title
        dialog.contentPane = dialogContentWrapper

        dialog.addNotify()

        val dialogInsets = dialog.insets

        val preferredSize = if (fixedWidth > 0) {
            Dimension(fixedWidth, dialogContentWrapper.preferredHeight(fixedWidth))
        } else {
            dialogContentWrapper.preferredSize
        }

        preferredSize.width += dialogInsets.left + dialogInsets.right
        preferredSize.height += dialogInsets.top + dialogInsets.bottom

        dialog.size = preferredSize
        dialog.setLocationRelativeTo(carrier)

        dialog.rootPane.defaultButton = dialogContentWrapper.defaultButton
        dialog.rootPane.registerKeyboardAction(KeyStroke("ESCAPE"), Conditions.WHEN_IN_FOCUSED_WINDOW) { dismiss() }

        val windowListener = object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                invokeLater {
                    dialog.toFront()
                    dialogContentWrapper.defaultFocusComponent?.requestFocusInWindow()
                }
            }

            override fun windowClosing(e: WindowEvent) {
                dismiss()
            }
        }

        val propertyChangeListener = PropertyChangeListener { event ->
            when (event.propertyName) {
                "title" -> dialog.title = title
            }
        }

        dialog.addWindowListener(windowListener)
        addPropertyChangeListener(propertyChangeListener)

        this.dialog = dialog
        this.dialogOutcome = null

        dialog.isVisible = true

        dialog.removeWindowListener(windowListener)
        removePropertyChangeListener(propertyChangeListener)
    }

    fun close(outcome: Int) {
        val dialog = dialog ?: return

        this.dialog = null
        this.dialogOutcome = outcome

        dialog.dispose()
    }

    fun dismiss() {
        val dismissiveAction = neutralAction ?: positiveAction ?: negativeAction ?: return

        val event = ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dismiss", System.nanoTime(), 0)
        dismissiveAction.actionPerformed(event)
    }

    open fun doPositiveAction() {
        close(POSITIVE)
    }

    open fun doNeutralAction() {
        close(NEUTRAL)
    }

    open fun doNegativeAction() {
        close(NEGATIVE)
    }

    companion object {
        const val POSITIVE = 1
        const val NEUTRAL = 0
        const val NEGATIVE = -1

        const val DEFAULT_BUTTON_PROPERTY = "dialogContent.defaultButton"
    }
}

class DialogTemplate(
    val positive: String? = null,
    val negative: String? = null,
    val neutral: String? = null,
) {
    fun createPositiveAction(dialog: SDialogContent): Action? {
        if (positive == null) return null
        return SAction(positive) { dialog.doPositiveAction() }
    }

    fun createNeutralAction(dialog: SDialogContent): Action? {
        if (neutral == null) return null
        return SAction(neutral) { dialog.doNeutralAction() }
    }

    fun createNegativeAction(dialog: SDialogContent): Action? {
        if (negative == null) return null
        return SAction(negative) { dialog.doNegativeAction() }
    }

    companion object {
        val OK = DialogTemplate(positive = "Ok")
        val SAVE_CANCEL = DialogTemplate(positive = "Save", neutral = "Cancel")
        val SAVE_DISCARD_CANCEL = DialogTemplate(positive = "Save", negative = "Discard", neutral = "Cancel")
        val OPEN_CANCEL = DialogTemplate(positive = "Open", neutral = "Cancel")
    }
}
