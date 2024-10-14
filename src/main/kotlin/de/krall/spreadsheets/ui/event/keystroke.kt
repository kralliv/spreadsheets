package de.krall.spreadsheets.ui.event

import de.krall.spreadsheets.ui.OS
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke

@JvmOverloads
fun KeyStroke(
    default: String? = null,
    macos: String? = default,
    linux: String? = default,
    windows: String? = default,
): KeyStroke? {
    val platformDescriptor = when {
        OS.isWindows -> windows
        OS.isMac -> macos
        OS.isLinux -> linux
        else -> null
    }

    val descriptor = platformDescriptor ?: default ?: return null
    if (descriptor == "none") return null
    val normalizedKeystrokeDescriptor = when {
        OS.isMac -> descriptor.replace("command", "meta").replace("option", "alt")
        else -> descriptor
    }
    return KeyStroke.getKeyStroke(normalizedKeystrokeDescriptor) ?: error("invalid keystroke: $descriptor")
}

object Conditions {
    const val WHEN_FOCUSED = JComponent.WHEN_FOCUSED
    const val WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    const val WHEN_IN_FOCUSED_WINDOW = JComponent.WHEN_IN_FOCUSED_WINDOW
}

@JvmOverloads
fun JComponent.registerKeyboardAction(
    keyStroke: KeyStroke?,
    condition: Int = Conditions.WHEN_FOCUSED,
    actionListener: ActionListener,
) {
    keyStroke ?: return

    val action = SimpleAction(actionListener)
    registerKeyboardAction(action, keyStroke, condition)
}

fun JComponent.deregisterKeyboardAction(
    keyStroke: KeyStroke?,
) {
    keyStroke ?: return

    unregisterKeyboardAction(keyStroke)
}

private class SimpleAction(private val actionListener: ActionListener) : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        actionListener.actionPerformed(e)
    }
}
