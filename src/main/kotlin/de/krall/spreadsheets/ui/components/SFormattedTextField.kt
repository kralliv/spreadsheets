package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.ui.event.add
import de.krall.spreadsheets.ui.event.remove
import fernice.reflare.light.FFormattedTextField

open class SFormattedTextField : FFormattedTextField(), STextComponent {

    protected val helper = TextComponentHelper(this)

    init {
        columns = 20
    }

    override fun addTextChangeListener(listener: TextChangeListener) = listenerList.add(listener)
    override fun removeTextChangeListener(listener: TextChangeListener) = listenerList.remove(listener)
}
