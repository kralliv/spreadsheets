package de.krall.spreadsheets.ui.event

import java.util.EventListener
import javax.swing.SwingUtilities
import javax.swing.event.EventListenerList

inline fun invokeLater(crossinline block: () -> Unit) {
    SwingUtilities.invokeLater { block() }
}

inline fun <reified T : EventListener> EventListenerList.add(listener: T) {
    add(T::class.java, listener)
}

inline fun <reified T : EventListener> EventListenerList.remove(listener: T) {
    remove(T::class.java, listener)
}

inline fun <reified T : EventListener> EventListenerList.forEach(block: (listener: T) -> Unit) {
    val listeners = getListeners(T::class.java)
    for (listener in listeners) block(listener)
}
