package de.krall.spreadsheets.ui.event

import de.krall.spreadsheets.ui.env.OS
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.ComponentInputMap
import javax.swing.InputMap
import javax.swing.JComponent
import javax.swing.KeyStroke
import kotlin.collections.addAll

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
        OS.isMac -> descriptor.replace("command", "meta").replace("cmd", "meta").replace("option", "alt")
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

    for (condition in Conditions.WHEN_FOCUSED..Conditions.WHEN_IN_FOCUSED_WINDOW) {
        val inputMap = getInputMap(condition)
        if (inputMap !is RemoveAwareInputMap) {
            setInputMap(condition, RemoveAwareInputMap(this, inputMap))
        }
    }

    unregisterKeyboardAction(keyStroke)
}

private class SimpleAction(private val actionListener: ActionListener) : AbstractAction() {
    override fun actionPerformed(e: ActionEvent) {
        actionListener.actionPerformed(e)
    }
}

class RemoveAwareInputMap(component: JComponent, private val delegate: InputMap) : ComponentInputMap(component) {

    private val removed = mutableSetOf<KeyStroke>()

    override fun get(keyStroke: KeyStroke): Any? {
        if (keyStroke in removed) return null

        return delegate.get(keyStroke)
    }

    override fun put(keyStroke: KeyStroke, actionMapKey: Any?) {
        removed.remove(keyStroke)

        delegate.put(keyStroke, actionMapKey)
    }

    override fun remove(keyStroke: KeyStroke) {
        removed.add(keyStroke)

        delegate.remove(keyStroke)
    }

    override fun clear() {
        removed.addAll(keys())

        delegate.clear()
    }
}
